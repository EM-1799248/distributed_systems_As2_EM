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

import java.io.*;
import java.net.*;

public class AggregationServer {
    private static final int PORT = 4567;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started and listening on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    // Read and print the HTTP request
                    String requestLine;
                    while ((requestLine = in.readLine()) != null && !requestLine.isEmpty()) {
                        System.out.println("Received: " + requestLine);
                    }

                    // Send a basic HTTP response back to the client
                    String httpResponse =
                            "HTTP/1.1 200 OK\r\n" + // Response status line
                                    "Content-Type: text/plain\r\n" + // Content type header
                                    "Connection: close\r\n" + // Connection header
                                    "\r\n" + // End of headers
                                    "Hello! This is the server's response."; // Body content

                    out.print(httpResponse);
                    out.flush(); // Ensure the response is sent

                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
