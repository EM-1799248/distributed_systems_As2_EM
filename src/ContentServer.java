/*
Reads data from a local file, converts it into JSON using Gson, and sends it to the aggregation server via a PUT request.

- makes PUT requests to ag server and uploads new data to it (replaces old data)
- can have multiple content servers
- start up, read command line for server name and port number, and also location of a file in system local to server that contains fields to be assembled into JSON and uploaded to Ag server
- maintains a lamport clock
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContentServer {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 4567;
    private static final LamportClock contentLamportClock = new LamportClock();  // Initialize Lamport clock

    public static void main(String[] args) throws IOException {
        int SERVER_PORT = DEFAULT_PORT; // Initialise server port as the default port

        try {
            if (args.length > 0) {
                // Extract port number from the URL provided in the arguments
                URL url = new URL(args[0]);
                SERVER_PORT = url.getPort();  // Extract the port number from the URL
            }
            System.out.println("Opened on port: " + SERVER_PORT);

        } catch (MalformedURLException e) {
            System.err.println("Invalid URL format: " + args[0]);
            e.printStackTrace();
        }

        // Read the file
        String filePath = "data.txt";
        Map<String, String> data = readDataFile(filePath);

        // Convert the data to JSON
        String jsonData = convertToJSON(data);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Increment the Lamport clock before sending the PUT request
            contentLamportClock.tick();
            int currentClock = contentLamportClock.getTime();

            // Create the PUT request
            String httpRequest = "PUT /data HTTP/1.1\r\n" +
                    "Host: " + SERVER_ADDRESS + "\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + jsonData.length() + "\r\n" +
                    "Lamport-Clock: " + currentClock + "\r\n" +
                    "Connection: close\r\n\r\n" +
                    jsonData;

            // Send the HTTP PUT request
            out.print(httpRequest);
            out.flush(); // Ensure the request is sent immediately

            // Read and print the server's response
            String responseLine;
            int receivedClock = -1;
            while ((responseLine = in.readLine()) != null) {
                System.out.println("Server Response: " + responseLine);
                if (responseLine.startsWith("Lamport-Clock")) {
                    receivedClock = Integer.parseInt(responseLine.split(": ")[1]);
                }
            }

            // Update the Lamport clock with the received time
            if (receivedClock != -1) {
                contentLamportClock.update(receivedClock);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to convert the map to a JSON string
    public static String convertToJSON(Map<String, String> data) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(data);
    }

    // Method to read data.txt and parse it into a map
    public static Map<String, String> readDataFile(String filePath) throws IOException {
        Map<String, String> data = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                data.put(parts[0], parts[1].trim());
            }
        }
        reader.close();
        return data;
    }

}
