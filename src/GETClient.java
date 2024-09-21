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
