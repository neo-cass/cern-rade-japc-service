package cern.rade.services.japc.stats;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStatsTest {
    private static final Client CLIENT = new Client("pma", "10.0.0.1");
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-03T12:00:00Z"));
    private final ClientStats stats = new ClientStats(clock);

    @Test
    void countsLastMinuteAndTotalPerOperation() {
        stats.record(CLIENT, Operation.GET);
        stats.record(CLIENT, Operation.GET);
        stats.record(CLIENT, Operation.SET);
        ClientRate rate = stats.rates().getFirst();
        assertEquals(new ClientRate.Count(2, 2), rate.operations().get(Operation.GET));
        assertEquals(new ClientRate.Count(1, 1), rate.operations().get(Operation.SET));
        assertEquals(new ClientRate.Count(0, 0), rate.operations().get(Operation.SUBSCRIBE));
    }

    @Test
    void dropsRequestsOlderThanOneMinuteFromTheRate() {
        stats.record(CLIENT, Operation.GET);
        clock.advance(Duration.ofSeconds(30));
        stats.record(CLIENT, Operation.GET);
        clock.advance(Duration.ofSeconds(45));
        assertEquals(new ClientRate.Count(1, 2), stats.rates().getFirst().operations().get(Operation.GET));
    }

    @Test
    void reusesSlotsAfterAMinute() {
        stats.record(CLIENT, Operation.GET);
        clock.advance(Duration.ofSeconds(RateWindow.SECONDS));
        stats.record(CLIENT, Operation.GET);
        assertEquals(new ClientRate.Count(1, 2), stats.rates().getFirst().operations().get(Operation.GET));
    }

    @Test
    void keepsClientsApart() {
        stats.record(CLIENT, Operation.GET);
        stats.record(new Client("other", "10.0.0.2"), Operation.SET);
        assertEquals(2, stats.rates().size());
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
