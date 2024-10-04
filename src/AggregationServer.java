/*
AggregationServer.java: Manages incoming GET and PUT requests.
It uses the Lamport Clock to track the order of requests and stores data in a dataStore.
JSON data is parsed using Gson.

- Checks data
- distributes to clients as they request it
- accepts updates from content servers
- stores data persistently until content server no longer in contact, or has not been in contact for 30 seconds
- can have multiple clients sent GET requests
- removes content from server that has not been updated in 30 sec (be efficient)
- when storage file is created, returns '201-HTTP_CREATED'
- when updates, returns '200' if unsuccessful, '201' if successful
- any request other than GET and PUT returns '400'
- sending no content to server returns '204'
- incorrect JSON returns '500'
- default port is 4567, but accepts single command line arg for port number
- contains the ag server's main method
- maintains a lamport clock
 */

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


public class AggregationServer {
    private static final int DEFAULT_PORT = 4567;
    private static final LamportClock clock = new LamportClock();
    private static Timer timer = new Timer();

    // In-memory data store to hold the updated data
    private static Map<String, String> localData = new HashMap<>();

    public static void main(String[] args) {
        // Get the port from command-line arguments or use default
        int SERVER_PORT = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started and listening on port " + SERVER_PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        System.out.println("Entity connected: " + clientSocket.getInetAddress());

        // Read the first line of the HTTP request to check the request type
        String requestLine = in.readLine();

        if (requestLine != null && !requestLine.isEmpty()) {
            System.out.println("Received: " + requestLine);

            // Check for the type of request (GET/PUT)
            if (requestLine.contains("GET")) {
                handleGetRequest(out);
            } else if (requestLine.contains("PUT")) {
                handlePutRequest(in, out);
            } else {
                sendResponse(out, "400 Bad Request", "Invalid request type", -1);
            }
        } else {
            // Handle null or empty request line, possibly returning a bad request
            sendResponse(out, "400 Bad Request", "Empty request line", -1);
        }
    }

    private static void handleGetRequest(OutputStream out) throws IOException {
        clock.tick(); // Lamport Clock tick for GET

        // Convert localData to JSON format
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(localData);

        sendResponse(out, "200 OK", jsonResponse, clock.getTime());
    }

    private static void handlePutRequest(BufferedReader in, OutputStream out) throws IOException {
        clock.tick(); // Lamport Clock tick for GET
        int receivedClock = -1;

        sendResponse(out, "200 OK", "PUT jsonResponse", clock.getTime());

        String contentLengthHeader = "";
        int contentLength = 0;

        // Read the headers
        String headerLine;
        while (!(headerLine = in.readLine()).isEmpty()) {
            System.out.println("Header: " + headerLine);
            if (headerLine.startsWith("Content-Length:")) {
                contentLengthHeader = headerLine;
            } else if (headerLine.startsWith("Lamport-Clock:")) {
                // Extract the Lamport clock value from the request headers
                receivedClock = Integer.parseInt(headerLine.split(": ")[1]);
            }
        }

        // Update the local Lamport Clock with the received clock
        if (receivedClock != -1) {
            clock.update(receivedClock);
        }

        // Extract the Content-Length value
        if (!contentLengthHeader.isEmpty()) {
            contentLength = Integer.parseInt(contentLengthHeader.split(":")[1].trim());
        }

        // Read the JSON payload
        char[] content = new char[contentLength];
        in.read(content, 0, contentLength);
        String jsonData = new String(content);

        // Parse the JSON data using TypeToken for type safety
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> data = gson.fromJson(jsonData, mapType);

        // Check if localData is empty before updating
        boolean wasEmpty = localData.isEmpty();

        // Write the received data to the local file
        updateLocalData(data);
        // Reset the expiration timer
        resetExpirationTimer();

        // Send a success response
        clock.tick();
        if (wasEmpty) {
            // Creating data file
            sendResponse(out, "201 Created", "Data successfully updated", clock.getTime());
        } else {
            // Updating data file
            sendResponse(out, "200 OK", "Data successfully updated", clock.getTime());
        }
    }

    private static void sendResponse(OutputStream out, String status, String message, int time) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n" +
                "Lamport-Clock: " + time + "\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;
        out.write(response.getBytes());
        out.flush(); // Ensure the response is sent immediately
    }

    // Method to write the updated data to a local file
    private static void updateLocalData(Map<String, String> data) throws IOException {
        localData.putAll(data);
        System.out.println("Local data updated successfully.");
    }

    // Method to convert structured data from a list format into a JSON object format
    private static JsonObject convertListToJsonObject(List<String[]> data) {
        JsonObject jsonObject = new JsonObject();
        for (String[] entry : data) { // Iterate through each String array in the list
            if (entry.length == 2) {
                // Add the first element as a key and the second as its value in the JsonObject
                jsonObject.addProperty(entry[0], entry[1]);
            }
        }
        return jsonObject;
    }

    // Resets the expiration timer after each PUT request
    public static void resetExpirationTimer() {
        // Cancel any existing task (if there is one)
        timer.cancel();
        timer = new Timer(); // Create a new Timer

        // Schedule a new task to clear data after 30 seconds
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearData();
            }
        }, 30 * 1000); // 30 seconds (30 * 1000 milliseconds)

        System.out.println("Expiration timer reset. Data will be cleared in 30 seconds.");
    }

    // Clear the stored data after the timer expires
    public static void clearData() {
        localData.clear();  // Clear the data
        System.out.println("Data has been cleared due to expiration.");
    }
}
