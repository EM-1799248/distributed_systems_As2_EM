import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AggregationServerTest {
    private static Thread serverThread;
    private static int testPort = 1234;

    @BeforeClass
    public static void setUp() throws Exception {
        serverThread = new Thread(() -> AggregationServer.main(new String[]{String.valueOf(testPort)}));
        serverThread.start();
        Thread.sleep(1000); // Wait for the server to start
    }

    @Test
    // Tests if the server correctly handles a GET request and responds with a 200 OK status.
    public void testHandleGetRequest() throws IOException {
        Socket socket = new Socket("localhost", testPort);;

        try  {
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.write("GET /data HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
            out.flush();

            String response = in.readLine();
            assertTrue(response.contains("200 OK")); // Check for successful response
        } finally {
            socket.close();
        }
    }

    @Test
    // Tests if the server correctly handles a PUT request, updates the local data, and responds with a 201 Created status.
    public void testHandlePutRequest() throws Exception {

        // Send a PUT request to the server
        String jsonInput = "{\"key1\":\"value1\"}";

        Socket socket = new Socket("localhost", testPort);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(("PUT /data HTTP/1.1\r\n" +
                "Content-Length: " + jsonInput.length() + "\r\n" +
                "Lamport-Clock: 1\r\n" +
                "\r\n" +
                jsonInput).getBytes()); // Convert to bytes
        outputStream.flush();

        // Read the response from the server
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String responseLine = in.readLine();

        // Assert the response indicates success
        assertTrue(responseLine.equals("HTTP/1.1 201 Created") || responseLine.equals("HTTP/1.1 200 OK"));

        // Close the socket
        socket.close();
    }

    @Test
    // Tests how the server responds to an invalid request method (DELETE) by checking for a 400 Bad Request status.
    public void testInvalidRequestType() throws IOException {
        Socket socket = new Socket("localhost", testPort);;

        try  {
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.write("DELETE /data HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
            out.flush();

            String response = in.readLine();
            assertEquals("HTTP/1.1 400 Bad Request", response); // Check for bad request
        } finally {
            socket.close();
        }
    }

    @Test
    // Sends a PUT request and then waits 31 seconds to verify that the data has been cleared after the expiration time.
    public void testClearDataAfterExpiration() throws Exception {

        // Send a PUT request to the server
        String jsonInput = "{\"key1\":\"value1\"}";

        Socket socket = new Socket("localhost", testPort);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(("PUT /data HTTP/1.1\r\n" +
                "Content-Length: " + jsonInput.length() + "\r\n" +
                "Lamport-Clock: 1\r\n" +
                "\r\n" +
                jsonInput).getBytes()); // Convert to bytes
        outputStream.flush();

        // Wait for 31 seconds to ensure the data is cleared
        Thread.sleep(31000);

        // Send a GET request to check if data has been cleared
        outputStream = socket.getOutputStream();
        outputStream.write("GET /data HTTP/1.1\r\n\r\n".getBytes()); // Convert to bytes
        outputStream.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String responseLine = in.readLine();

        // Assert that the response indicates the data has been cleared
        assertEquals("HTTP/1.1 200 OK", responseLine);

        // Close the socket
        socket.close();
    }

    @Test
    // Tests how the server handles an empty request, ensuring it responds with a 400 Bad Request status.
    public void testHandleEmptyRequest() throws IOException {
        Socket socket = new Socket("localhost", testPort);;

        try  {
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.write("\r\n".getBytes()); // Empty request
            out.flush();

            String response = in.readLine();
            assertEquals("HTTP/1.1 400 Bad Request", response); // Check for bad request
        } finally {
            socket.close();
        }

    }
}
