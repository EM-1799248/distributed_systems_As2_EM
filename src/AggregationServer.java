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
                                    "Hello, Client! This is the server's response."; // Body content

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
