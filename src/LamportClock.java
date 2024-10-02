/*
Implements the Lamport clock mechanism for tracking request order.

- timestamp all GET and PUT requests to maintain order of requests
- applied in every entity
- local lamport clocks sent to other entities with requests
- can just use a simple counter, does not need to use actual time
 */

public class LamportClock {
    private int clock;

    public LamportClock() {
        this.clock = 0;
    }

    public synchronized void tick() {
        clock++;
    }

    public synchronized void update(int receivedTime) {
        clock = this.clock + receivedTime + 1;
        // clock = Math.max(this.clock, receivedTime) + 1;
    }

    public synchronized int getTime() {
        return clock;
    }
}
