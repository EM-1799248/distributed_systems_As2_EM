import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import java.io.*;
import java.net.Socket;

public class GETClientTest {
    private GETClient getClient;
    private final String testFilePath = "testData.txt"; // Path for the response file
    private final String serverAddress = "localhost";
    private final int serverPort = 4567;

    @Before
    public void setUp() throws Exception {
        // Ensure the test data file exists with the correct content
        // This assumes testData.txt is already created with the appropriate content
        File testFile = new File(testFilePath);
        if (!testFile.exists()) {
            throw new FileNotFoundException("Test data file not found: " + testFilePath);
        }

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
    public void testParseServerAddressHigh() {
        String[] expected = {"localhost", "65535"};
        String[] actual = getClient.parseServerAddress("http://localhost:65535");
        assertArrayEquals(expected, actual);

        expected = new String[]{"localhost", "65535"};
        actual = getClient.parseServerAddress("localhost:65535");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testParseServerAddressLow() {
        String[] expected = {"localhost", "1"};
        String[] actual = getClient.parseServerAddress("http://localhost:1");
        assertArrayEquals(expected, actual);

        expected = new String[]{"localhost", "1"};
        actual = getClient.parseServerAddress("localhost:1");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testParseServerPort() {
        String invalidAddress = "http://localhost:4567:1234";

        try {
            getClient.parseServerAddress(invalidAddress);
            // If we reach this line, the exception was not thrown
            fail("Expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // Check the exception message
            assertEquals("Invalid server address format.", e.getMessage());
        }
    }

}
