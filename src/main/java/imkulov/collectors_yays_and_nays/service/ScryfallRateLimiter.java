package imkulov.collectors_yays_and_nays.service;

import org.springframework.stereotype.Component;

@Component
public class ScryfallRateLimiter {

    // 200 ms = maximum 5 requests per second
    private static final long REQUEST_INTERVAL_MS = 200;

    private long lastRequestTime = 0;

    public synchronized void waitForPermission() {

        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < REQUEST_INTERVAL_MS) {

            long waitTime = REQUEST_INTERVAL_MS - timeSinceLastRequest;

            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Rate limiter interrupted", e);
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }
}