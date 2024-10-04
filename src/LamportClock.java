/*
Implements the Lamport clock mechanism for tracking request order.

- timestamp all GET and PUT requests to maintain order of requests
- applied in every entity
- local lamport clocks sent to other entities with requests
- can just use a simple counter, does not need to use actual time
 */

public class LamportClock {
    private int clock;

    // Method to initialise/reset the clock counter
    public LamportClock() {
        this.clock = 0;
    }

    // Method to increment the clock counter
    public synchronized void tick() {
        clock++;
    }

    // Method to update the clock counter
    public synchronized void update(int receivedTime) {
        clock = this.clock + receivedTime + 1;
    }

    // Method to return the clock counter value
    public synchronized int getTime() {
        return clock;
    }
}
