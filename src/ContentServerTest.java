import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ContentServerTest {
    private ContentServer contentServer;
    private final String testFilePath = "testData.txt";

    @Before
    public void setUp() throws Exception {
        // Create a test data file
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFilePath));
        writer.write("key1:value1\n");
        writer.write("key2:value2\n");
        writer.close();

        // Initialize ContentServer with the test data file
        contentServer = new ContentServer();
    }

    @Test
    public void testReadDataFile() throws IOException {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("key1", "value1");
        expectedData.put("key2", "value2");

        // Read the test data file
        Map<String, String> actualData = contentServer.readDataFile(testFilePath);

        // Assert that the actual data matches the expected data
        assertEquals(expectedData, actualData);
    }

    @Test
    public void testConvertToJSON() {
        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");

        // Convert map to JSON string
        String jsonData = contentServer.convertToJSON(data);

        // Assert that the JSON string is as expected
        String expectedJson = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        assertEquals(expectedJson, jsonData);
    }

    @Test
    public void testSendPUTRequest() {
        // Mock Socket connection and PrintWriter
        try {
            Socket mockSocket = new Socket("localhost", 4567); // use the actual port of your Aggregation Server
            PrintWriter out = new PrintWriter(mockSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(mockSocket.getInputStream()));

            // Simulate sending a PUT request
            String jsonData = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
            String httpRequest = "PUT /data HTTP/1.1\r\n" +
                    "Host: localhost\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + jsonData.length() + "\r\n" +
                    "Lamport-Clock: 1\r\n" +
                    "Connection: close\r\n\r\n" +
                    jsonData;

            out.print(httpRequest);
            out.flush();

            // Mock response from server
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Lamport-Clock: 2\r\n" +
                    "Content-Length: 0\r\n\r\n";

            // Simulate reading the response
            in.readLine(); // Read the status line
            int receivedClock = -1;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("Lamport-Clock")) {
                    receivedClock = Integer.parseInt(line.split(": ")[1]);
                }
            }

            // Validate the received clock
            assertEquals(2, receivedClock);

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
