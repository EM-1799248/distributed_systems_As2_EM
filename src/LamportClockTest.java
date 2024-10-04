import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class LamportClockTest {
    private LamportClock clock;

    @Before
    public void setUp() {
        clock = new LamportClock();
    }

    @Test
    public void testGetTime0() {
        int initialClock = clock.getTime();
        assertEquals(0, clock.getTime()); // Verify updated clock value
    }
    @Test
    public void testTick() {
        int initialClock = clock.getTime();
        clock.tick();
        assertEquals(initialClock + 1, clock.getTime()); // Verify that the clock has incremented
    }

    @Test
    public void testGetTime() {
        int initialClock = clock.getTime();
        assertEquals(1, clock.getTime()); // Verify updated clock value
    }

    @Test
    public void testUpdateClock() {
        clock.tick();
        int initialClock = clock.getTime();
        clock.update(5);
        assertEquals(initialClock + 1 + 5, clock.getTime()); // Verify updated clock value
    }
}
