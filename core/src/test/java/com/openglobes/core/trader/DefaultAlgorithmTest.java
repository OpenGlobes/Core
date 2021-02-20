package com.openglobes.core.trader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;

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
                                               Offset.OPEN,
                                               null,
                                               null));
        assertEquals(0.0,
                     algorithm().getCommission(price,
                                               instrument,
                                               Offset.CLOSE_YD,
                                               null,
                                               null));
        assertEquals(instrument.getCommissionCloseTodayRatio(),
                     algorithm().getCommission(price,
                                               instrument,
                                               Offset.CLOSE_TODAY,
                                               null,
                                               null));

        instrument = instrument("x2109");
        price      = 2000.0;
        assertEquals(price * instrument.getMultiple() * instrument.getCommissionOpenRatio(),
                     algorithm().getCommission(price,
                                               instrument,
                                               Offset.OPEN,
                                               null,
                                               null));
        assertEquals(0.0,
                     algorithm().getCommission(2000.0,
                                               instrument,
                                               Offset.CLOSE_YD,
                                               null,
                                               null));
        assertEquals(price * instrument.getMultiple() * instrument.getCommissionCloseTodayRatio(),
                     algorithm().getCommission(price,
                                               instrument,
                                               Offset.CLOSE_TODAY,
                                               null,
                                               null));
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
        var day = LocalDate.now();
        var sp  = new HashMap<String, SettlementPrice>();

        var sp0 = new SettlementPrice();
        sp0.setInstrumentId("c2105");
        sp0.setTimestamp(ZonedDateTime.now());
        sp0.setTradingDay(day);
        sp0.setSettlementPrice(2970.0);
        sp0.setSettlementPriceId(1L);
        sp.put(sp0.getInstrumentId(),
               sp0);
        assertDoesNotThrow(() -> {
            var positions = algorithm().getPositions(contracts(),
                                                     commissions(),
                                                     margins(),
                                                     sp,
                                                     instruments(),
                                                     day);
            assertEquals(2,
                         positions.size());

            var buy = getPosition(Direction.BUY,
                                  positions);
            assertEquals(1,
                         buy.getVolumn());
            assertEquals(2,
                         buy.getPreVolumn());
            assertEquals(0,
                         buy.getTodayVolumn());
            assertEquals(0,
                         buy.getTodayOpenVolumn());

            var r = requests().get(5L);
            assertEquals(algorithm().getCommission(r.getPrice(),
                                                   instrument(r.getInstrumentId()),
                                                   r.getOffset(),
                                                   getContractById(2L),
                                                   r.getTradingDay()),
                         buy.getCommission());

            var sell = getPosition(Direction.SELL,
                                   positions);
            assertEquals(3,
                         sell.getVolumn());
            assertEquals(1,
                         sell.getPreVolumn());
            assertEquals(2,
                         sell.getTodayVolumn());
            assertEquals(2,
                         sell.getTodayOpenVolumn());

            /*
             * Closing today's contracts requires commission.
             */
            r = requests().get(4L);
            assertEquals(algorithm().getCommission(r.getPrice(),
                                                   instrument(r.getInstrumentId()),
                                                   r.getOffset(),
                                                   getContractById(4L),
                                                   r.getTradingDay()),
                         sell.getFrozenCommission());
        });
    }

    private Position getPosition(Integer direction,
                                 Collection<Position> positions) {
        for (var p : positions) {
            if (p.getDirection().equals(direction)) {
                return p;
            }
        }
        throw new NullPointerException("Position not found.");
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