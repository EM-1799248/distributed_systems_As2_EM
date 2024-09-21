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

import java.io.*;
import java.net.*;

public class GETClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4567;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Building the HTTP GET request string
            String httpRequest =
                    "GET / HTTP/1.1\r\n" + // Request method and resource path
                            "Host: localhost\r\n" + // Host header, required in HTTP/1.1
                            "Connection: close\r\n" + // Indicate to close connection after response
                            "\r\n"; // Empty line to indicate end of headers

            // Send the HTTP request to the server
            out.print(httpRequest);
            out.flush(); // Ensure all data is sent to the server

            // Read the response from the server
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println("Response: " + responseLine);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
