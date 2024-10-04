/*
Sends GET requests to the aggregation server, receives the response, and prints it.

- client contacts aggregation server through RESTful API
- can have multiple clients
- displays the received data
- starts up, reads command line for server name, port number (url format) and optional station ID
- send GET req to Ag server for weather data
- data stripped of JSON format, and displayed one at a time with attribute and value
- main method
- Multiple possible formats for the server name and port number
- output does not need hyperlinks
- maintains a lamport clock
 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;

public class GETClient {
    private static final String DEFAULT_SERVER = "localhost";
    private static final int DEFAULT_PORT = 4567;
    private static final LamportClock clientLamportClock = new LamportClock();  // Initialise Lamport clock

    public static void main(String[] args) {

        int SERVER_PORT = DEFAULT_PORT; // Initialise server port as the default port
        String SERVER_ADDRESS = DEFAULT_SERVER;

        try {
            if (args.length > 0) {
                // Parse the server address (it can include or omit the port)
                String[] serverDetails = parseServerAddress(args[0]);
                SERVER_ADDRESS = serverDetails[0];
                SERVER_PORT = Integer.parseInt(serverDetails[1]);
            }

            System.out.println("Connecting to server: " + SERVER_ADDRESS + " on port: " + SERVER_PORT);

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println("400 Bad request - port does not match");
            return;
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Increment the Lamport clock before sending the PUT request
            clientLamportClock.tick();
            int currentClock = clientLamportClock.getTime();

            // Building the HTTP GET request string
            String httpRequest =
                    "GET / HTTP/1.1\r\n" + // Request method and resource path
                            "Host: localhost\r\n" + // Host header, required in HTTP/1.1
                            "Connection: close\r\n" + // Indicate to close connection after response
                            "Lamport-Clock: " + currentClock + "\r\n" +  // Send the Lamport clock with the request
                            "\r\n"; // Empty line to indicate end of headers

            // Send the HTTP request to the server
            out.print(httpRequest);
            out.flush(); // Ensure all data is sent to the server

            // Accumulate the entire server response
            StringBuilder fullResponse = new StringBuilder();
            String responseLine;
            int receivedClock = -1;

            while ((responseLine = in.readLine()) != null) {
                fullResponse.append(responseLine).append("\n");  // Append each line to the full response

                // Check for Lamport-Clock header
                if (responseLine.startsWith("Lamport-Clock:")) {
                    receivedClock = Integer.parseInt(responseLine.split(": ")[1]);
                }
            }

            // Print the full raw response immediately for comparison
            System.out.println("Full Raw Server Response:");
            System.out.println(fullResponse.toString());

            // Update the client Lamport clock
            clientLamportClock.update(receivedClock);

            // Extract JSON from the response body (assuming it's after the headers)
            String jsonResponse = fullResponse.substring(fullResponse.indexOf("{"));  // Find and extract the JSON portion

            // Parse JSON data
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            // Print the data line by line
            if (jsonObject != null) {
                for (String key : jsonObject.keySet()) {
                    System.out.println(key + ": " + jsonObject.get(key).getAsString());
                }
            } else {
                System.out.println("No data received.");
            }

        } catch (IOException e) {
            System.err.println("Client Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String[] parseServerAddress(String input) throws IllegalArgumentException {
        String serverName = DEFAULT_SERVER;
        int port = DEFAULT_PORT;

        // Trim input to remove any leading/trailing whitespace
        input = input.trim();

        // Remove prefix if present
        if (input.startsWith("http://")) {
            input = input.substring(7);
        } else if (input.startsWith("https://")) {
            input = input.substring(8);
        }

        // Check if there is a colon in the input (indicating both server and port)
        if (input.contains(":")) {
            // Split the input by the colon
            String[] parts = input.split(":");

            if (parts.length == 2) {
                // If there are two parts, the first is the server name and the second is the port
                if (!parts[0].isEmpty()) {
                    serverName = parts[0];
                }
                try {
                    port = Integer.parseInt(parts[1]);
                    System.out.println("Server port: " + port);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid port number format.");
                }
            } else {
                throw new IllegalArgumentException("Invalid server address format.");
            }
        } else {
            // If there's no colon, it's just the server name with the default port
            serverName = input;
        }

        return new String[]{serverName, String.valueOf(port)};
    }

}
