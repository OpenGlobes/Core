package com.openglobes.core.utils;

import com.openglobes.core.trader.ActionType;
import com.openglobes.core.trader.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Utils Tests")
class UtilsTest {

    @Test
    @DisplayName("Utils::copy()")
    void copy() {
        var old = new Request();

        old.setAction(ActionType.DELETE);
        old.setExchangeId("DCE");

        var n = Utils.copy(old);

        assertEquals(old.getAction(),
                     n.getAction(),
                     "Action must be same.");
        assertEquals(old.getExchangeId(),
                     n.getExchangeId(),
                     "Exchange ID must be same.");
        assertNull(n.getInstrumentId(),
                   "Instrument ID should be null.");
    }

    @Test
    void getRoundedTimeByMinute() {
        var n = Utils.getRoundedTimeByMinute();
        var c = ZonedDateTime.now();

        assertEquals(0,
                     n.getSecond(),
                     "Seconds should be zero.");
        assertEquals(0,
                     n.getNano(),
                     "Nanos should be zero.");
        assertTrue(c.plusMinutes(1).getMinute() == n.getMinute() || c.getMinute() == n.getMinute(),
                   "Round minute failed.");
    }

    @Test
    void getExecutionId() {
        assertTrue(Utils.getExecutionId() > 0,
                   "Execution ID should be positive.");
    }

    @Test
    void inRange() {
    }

    @Test
    void nextId() {
        assertTrue(Utils.nextId() > 0,
                   "ID should be positive.");
    }

    @Test
    void schedulePerDuration() {
        var r = Utils.schedulePerDuration(new TimerTask() {
                                              @Override
                                              public void run() {
                                              }
                                          },
                                          Duration.ofHours(1));
        r.purge();
        r.cancel();
    }
}