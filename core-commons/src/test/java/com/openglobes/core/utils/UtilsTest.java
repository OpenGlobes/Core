package com.openglobes.core.utils;

import com.openglobes.core.trader.ActionType;
import com.openglobes.core.trader.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void getAlignByMinute() {
    }

    @Test
    void getExecutionId() {
    }

    @Test
    void inRange() {
    }

    @Test
    void nextId() {
    }

    @Test
    void nextUuid() {
    }

    @Test
    void schedulePerDuration() {
    }
}