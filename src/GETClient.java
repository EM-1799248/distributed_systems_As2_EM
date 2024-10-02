/*
Sends GET requests to the aggregation server, receives the response, and prints it.

- client contacts aggregation server through RESTful API
- can have multiple clients
- displays the received data
- starts up, reads command line for server name, port number (url format) and optional station ID
- send GET req to Ag server for weather data
- data stripped of JSON format, and displayed one at a time with attribute and value
- main method
- Possible formats for the server name and port number include "http://servername.domain.domain:portnumber", "http://servername:portnumber" (with implicit domain information) and "servername:portnumber" (with implicit domain and protocol information).
- output does not need hyperlinks
- maintains a lamport clock
 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.*;
import java.util.Map;

public class GETClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4567;
    private static LamportClock clientLamportClock = new LamportClock();  // Initialize Lamport clock

    public static void main(String[] args) {

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











            /*

            // Read the response from the server
            String responseLine;
            int receivedClock = -1;
            while ((responseLine = in.readLine()) != null) {
                if (!responseLine.isEmpty()) {
                    System.out.println("Server Response: " + responseLine);
                    if (responseLine.startsWith("Lamport-Clock:")) {
                        receivedClock = Integer.parseInt(responseLine.split(": ")[1]);
                    }
                }
            }

//            // Read response status line
//            String statusLine = in.readLine();
//            System.out.println("Server Response: " + statusLine);
//
//            // print
//            String line;
//            int receivedClock = -1;
//            while ((line = in.readLine()) != null) {
//                if (!line.isEmpty()) {
//                    System.out.println("Server Response: " + line);
//                    if (line.startsWith("Lamport-Clock:")) {
//                        receivedClock = Integer.parseInt(line.split(": ")[1]);
//                    }
//                }
//            }

            // update clock
            clientLamportClock.update(receivedClock);

            // Read the JSON response body
            StringBuilder jsonResponse = new StringBuilder();
            String jsonLine;
            while ((jsonLine = in.readLine()) != null) {
                jsonResponse.append(jsonLine);
            }

            // Parse JSON data
            Gson gson = new Gson();
//            Map<String, String> data = gson.fromJson(jsonResponse.toString(), new TypeToken<Map<String, String>>() {}.getType());
            JsonObject jsonObject = gson.fromJson(String.valueOf(jsonResponse), JsonObject.class);

             */



            // Print the data line by line
            if (jsonObject != null) {
//                for (Map.Entry<String, String> entry : data.entrySet()) {
//                    System.out.println(entry.getKey() + ": " + entry.getValue());
//                }
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
}
