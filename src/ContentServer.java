/*
Reads data from a local file, converts it into JSON using Gson, and sends it to the aggregation server via a PUT request.

- makes PUT requests to ag server and uploads new data to it (replaces old data)
- can have multiple content servers
- start up, read command line for server name and port number, and also location of a file in system local to server that contains fields to be assembled into JSON and uploaded to Ag server
- maintains a lamport clock
 */

import java.io.*;
import java.net.*;

public class ContentServer {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4567;

    public static void main(String[] args) {
        // Sample JSON data to send in the PUT request
        String jsonData = "{ \"id\": 1, \"name\": \"Weather Data\", \"temperature\": \"25.6\", \"humidity\": \"40\" }";

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Create the PUT request
            String httpRequest = "PUT /data HTTP/1.1\r\n" +
                    "Host: " + SERVER_ADDRESS + "\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + jsonData.length() + "\r\n" +
                    "Connection: close\r\n\r\n" +
                    jsonData;

            // Send the HTTP PUT request
            out.print(httpRequest);
            out.flush(); // Ensure the request is sent immediately

            // Read and print the server's response
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println("Server Response: " + responseLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
