package com.openglobes.core.trader;

import org.junit.jupiter.api.DisplayName;
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
    void testAcceptedOrder() {
        var r      = requests().get(4L);
        var trades = getTradesByOrderId(r.getOrderId());
        var cs     = getContractsByTrades(trades);
        var rs     = getResponsesByOrderId(r.getOrderId());

        assertDoesNotThrow(() -> {
            var order = algorithm().getOrder(r,
                                             cs,
                                             trades,
                                             rs);
            assertEquals(OrderStatus.ACCEPTED,
                         order.getStatus());
            assertEquals(r.getQuantity(),
                         order.getQuantity());
            assertEquals(r.getOrderId(),
                         order.getOrderId());
            assertEquals(r.getDirection(),
                         order.getDirection());
            assertEquals(r.getOffset(),
                         order.getOffset());
            assertEquals(r.getPrice(),
                         order.getPrice());
            assertEquals(r.getInstrumentId(),
                         order.getInstrumentId());
            assertEquals(r.getTradingDay(),
                         order.getTradingDay());
        });
    }

    @Test
    @DisplayName("Test request whose volumn is all traded.")
    void testTradedOrder1() {
        testTradedOrder(1L);
    }

    @Test
    @DisplayName("Test request whose volumn is now all closing.")
    void testTradedOrder2() {
        testTradedOrder(2L);
    }

    @Test
    @DisplayName("Test request whose volumn is now partially closing.")
    void testTradedOrder3() {
        testTradedOrder(3L);
    }

    @Test
    void getPositions() {
    }

    private void testTradedOrder(Long orderId) {
        var r      = requests().get(orderId);
        var trades = getTradesByOrderId(r.getOrderId());
        var cs     = getContractsByTrades(trades);
        var rs     = getResponsesByOrderId(r.getOrderId());

        assertDoesNotThrow(() -> {
            var order = algorithm().getOrder(r,
                                             cs,
                                             trades,
                                             rs);
            assertEquals(OrderStatus.ALL_TRADED,
                         order.getStatus());
            assertEquals(r.getQuantity(),
                         order.getQuantity());
            assertEquals(r.getOrderId(),
                         order.getOrderId());
            assertEquals(r.getDirection(),
                         order.getDirection());
            assertEquals(r.getOffset(),
                         order.getOffset());
            assertEquals(r.getPrice(),
                         order.getPrice());
            assertEquals(r.getInstrumentId(),
                         order.getInstrumentId());
            assertEquals(r.getTradingDay(),
                         order.getTradingDay());
        });
    }
}