import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ContentServerTest {
    private static ContentServer contentServer;
    private static final String testFilePath = "data.txt";

    @BeforeClass
    public static void setUp() throws Exception {

        // Initialise ContentServer with the test data file
        contentServer = new ContentServer();
    }

    @Test
    public void testReadDataFile() throws IOException {
        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("id", "IDS60901");
        expectedData.put("state", "SA");
        expectedData.put("time_zone", "CST");
        expectedData.put("lat", "-34.9");
        expectedData.put("lon", "138.6");
        expectedData.put("local_date_time", "15/04:00pm");
        expectedData.put("local_date_time_full", "20230715160000");
        expectedData.put("air_temp", "13.3");
        expectedData.put("apparent_t", "9.5");
        expectedData.put("cloud", "Partly cloudy");
        expectedData.put("dewpt", "5.7");
        expectedData.put("press", "1023.9");
        expectedData.put("rel_hum", "60");
        expectedData.put("wind_dir", "S");
        expectedData.put("wind_spd_kmh", "15");
        expectedData.put("wind_spd_kt", "8");


        // Read the test data file
        Map<String, String> actualData = contentServer.readDataFile(testFilePath);
        System.out.println(actualData);

        // Assert that the actual data matches the expected data
        assertEquals("IDS60901", actualData.get("id"));
        assertEquals("SA", actualData.get("state"));
        assertEquals("CST", actualData.get("time_zone"));
        assertEquals("-34.9", actualData.get("lat"));
        assertEquals("138.6", actualData.get("lon"));
        assertEquals("15/04:00pm", actualData.get("local_date_time"));
        assertEquals("20230715160000", actualData.get("local_date_time_full"));
        assertEquals("13.3", actualData.get("air_temp"));
        assertEquals("9.5", actualData.get("apparent_t"));
        assertEquals("Partly cloudy", actualData.get("cloud"));
        assertEquals("5.7", actualData.get("dewpt"));
        assertEquals("1023.9", actualData.get("press"));
        assertEquals("60", actualData.get("rel_hum"));
        assertEquals("S", actualData.get("wind_dir"));
        assertEquals("15", actualData.get("wind_spd_kmh"));
        assertEquals("8", actualData.get("wind_spd_kt"));

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

}
