import static org.junit.Assert.*;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class GETClientTest {
    private GETClient getClient;
    private final String testFilePath = "testData.txt"; // Adjust the path for the response file
    private final String serverAddress = "localhost";
    private final int serverPort = 4567;

    @Before
    public void setUp() throws Exception {
        // Create a test data file for the server response
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFilePath));
        writer.write("{\"key1\":\"value1\",\"key2\":\"value2\"}");
        writer.close();

        // Initialize GETClient (constructor modification may be necessary)
        getClient = new GETClient();
    }

    @Test
    public void testParseServerAddress() {
        String[] expected = {"localhost", "4567"};
        String[] actual = getClient.parseServerAddress("http://localhost:4567");
        assertArrayEquals(expected, actual);

        expected = new String[]{"localhost", "4567"};
        actual = getClient.parseServerAddress("localhost:4567");
        assertArrayEquals(expected, actual);

        expected = new String[]{"localhost", "4567"};
        actual = getClient.parseServerAddress("localhost");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSendGETRequest() {
        // Mock Socket connection and PrintWriter
        try {
            Socket mockSocket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(mockSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(mockSocket.getInputStream()));

            // Simulate sending a GET request
            String httpRequest =
                    "GET / HTTP/1.1\r\n" +
                            "Host: " + serverAddress + "\r\n" +
                            "Connection: close\r\n" +
                            "Lamport-Clock: 1\r\n\r\n";

            out.print(httpRequest);
            out.flush();

            // Mock response from server
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Lamport-Clock: 2\r\n" +
                    "Content-Length: " + "{\"key1\":\"value1\",\"key2\":\"value2\"}".length() + "\r\n" +
                    "\r\n" +
                    "{\"key1\":\"value1\",\"key2\":\"value2\"}";

            // Simulate reading the response
            in.readLine(); // Read the status line
            int receivedClock = 2; // Simulated received Lamport clock
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBody.append(line).append("\n");
            }

            // Print and process the response
            System.out.println("Full Raw Server Response:");
            System.out.println(response);

            // Validate the received clock
            assertEquals(2, receivedClock);

            // Parse JSON response
            String jsonResponse = responseBody.substring(responseBody.indexOf("{")); // Extract JSON from response
            JsonObject jsonObject = new Gson().fromJson(jsonResponse, JsonObject.class);

            // Assert that the JSON object contains the expected data
            assertNotNull(jsonObject);
            assertEquals("value1", jsonObject.get("key1").getAsString());
            assertEquals("value2", jsonObject.get("key2").getAsString());

            // Close the sockets
            out.close();
            in.close();
            mockSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception thrown during test: " + e.getMessage());
        }
    }
}
