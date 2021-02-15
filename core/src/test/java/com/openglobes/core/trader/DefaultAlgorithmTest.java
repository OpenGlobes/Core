package com.openglobes.core.trader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultAlgorithmTest extends AlgorithmData {

    @Test
    void getAccount() {
    }

    @Test
    void getAmount() {
        var price      = 2960.0;
        var instrument = instrument("c2109");

        assertEquals(price * instrument.getMultiple(),
                     algorithm().getAmount(price,
                                           instrument));
    }

    @Test
    void getCommission() {
        var instrument = instrument("c2105");
        var price      = 2960.0;
        assertEquals(instrument.getCommissionOpenRatio(),
                     algorithm().getCommission(price,
                                               instrument,
                                               Direction.BUY,
                                               Offset.OPEN));
        assertEquals(0.0,
                     algorithm().getCommission(price,
                                               instrument,
                                               Direction.BUY,
                                               Offset.CLOSE));
        assertEquals(instrument.getCommissionCloseTodayRatio(),
                     algorithm().getCommission(price,
                                               instrument,
                                               Direction.BUY,
                                               Offset.CLOSE_TODAY));

        instrument = instrument("x2109");
        price      = 2000.0;
        assertEquals(price * instrument.getMultiple() * instrument.getCommissionOpenRatio(),
                     algorithm().getCommission(price,
                                               instrument,
                                               Direction.BUY,
                                               Offset.OPEN));
        assertEquals(0.0,
                     algorithm().getCommission(2000.0,
                                               instrument,
                                               Direction.BUY,
                                               Offset.CLOSE));
        assertEquals(price * instrument.getMultiple() * instrument.getCommissionCloseTodayRatio(),
                     algorithm().getCommission(price,
                                               instrument,
                                               Direction.BUY,
                                               Offset.CLOSE_TODAY));
    }

    @Test
    void getMargin() {
        var price      = 2960.0;
        var instrument = instrument("c2109");

        assertEquals(price * instrument.getMultiple() * instrument.getMarginRatio(),
                     algorithm().getMargin(price,
                                           instrument));

        instrument = instrument("x2109");
        assertEquals(instrument.getMarginRatio(),
                     algorithm().getMargin(price,
                                           instrument));
    }

    @Test
    void getOrder() {
    }

    @Test
    void getPositions() {
    }
}