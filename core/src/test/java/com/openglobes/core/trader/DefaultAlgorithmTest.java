/*
 * Copyright (C) 2021 Hongbao Chen <chenhongbao@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.openglobes.core.trader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAlgorithmTest extends AlgorithmData {

    @Test
    void getAccount() {
        assertDoesNotThrow(() -> {
            var positions = algorithm().getPositions(contracts(),
                                                     commissions(),
                                                     margins(),
                                                     settlements(),
                                                     instruments(),
                                                     LocalDate.now());
            Account account = algorithm().getAccount(account(),
                                                     deposits(),
                                                     withdraws(),
                                                     positions);
            assertEquals(getCloseProfit(contracts()),
                         account.getCloseProfit());
            assertEquals(getPositionProfit(contracts(),
                                           settlements(),
                                           instruments()),
                         account.getPositionProfit());
        });
    }

    @Test
    void getAmount() {
        var price = 2960.0;
        var instrument = instrument("c2109");

        assertEquals(price * instrument.getMultiple(),
                     algorithm().getAmount(price,
                                           instrument));
    }

    @Test
    void getCommission() {
        var instrument = instrument("c2105");
        var price = 2960.0;
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
        price = 2000.0;
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
        var price = 2960.0;
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
    void getPositions() {
        assertDoesNotThrow(() -> {
            var positions = algorithm().getPositions(contracts(),
                                                     commissions(),
                                                     margins(),
                                                     settlements(),
                                                     instruments(),
                                                     LocalDate.now());
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
            assertEquals(3,
                         sell.getTodayOpenVolumn());

            /*
             * Closing today's contracts requires commission.
             */
            r = requests().get(4L);
            var comm = algorithm().getCommission(r.getPrice(),
                                                 instrument(r.getInstrumentId()),
                                                 r.getOffset(),
                                                 getContractById(4L),
                                                 r.getTradingDay());
            assertEquals(comm,
                         sell.getFrozenCommission());
            /*
             * Open 3, Close 1.
             */
            assertEquals(comm * 4,
                         sell.getCommission());
        });
    }

    @Test
    @DisplayName("Test request that closes both today and yesterday contracts, and is accepted.")
    void testAcceptedOrder() {
        var r = requests().get(4L);
        var trades = getTradesByOrderId(r.getOrderId());
        var cs = getContractsByTrades(trades);
        var rs = getResponsesByOrderId(r.getOrderId());

        assertDoesNotThrow(() -> {
            var order = algorithm().getOrder(r,
                                             cs,
                                             trades,
                                             rs,
                                             instruments());
            assertEquals(OrderStatus.ACCEPTED,
                         order.getStatus());
            assertEquals(0,
                         order.getTradedVolumn());
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
    @DisplayName("Test request whose volumn is now all closed.")
    void testTradedOrder2() {
        testTradedOrder(2L);
    }

    @Test
    @DisplayName("Test request whose volumn is now partially closing.")
    void testTradedOrder3() {
        testTradedOrder(3L);
    }

    @Test
    @DisplayName("Test request that closes contracts for yesterday, all traded.")
    void testTradedOrder5() {
        testTradedOrder(5L);
    }

    @Test
    @DisplayName("Test request opens contracts for today, all traded.")
    void testTradedOrder6() {
        testTradedOrder(6L);
    }

    @Test
    @DisplayName("Test request closes contracts for today, all traded.")
    void testTradedOrder7() {
        testTradedOrder(7L);
    }

    private Double getCloseProfit(Collection<Contract> contracts) {
        final Double[] r = new Double[]{0.0D};
        contracts.forEach(c -> {
            if (c.getCloseAmount() == null) {
                return;
            }
            if (c.getDirection() == Direction.BUY) {
                r[0] += c.getCloseAmount() - c.getOpenAmount();
            } else {
                r[0] += c.getOpenAmount() - c.getCloseAmount();
            }
        });
        return r[0];
    }

    private long getMultiple(String instrumentId,
                             Map<String, Instrument> instruments) {
        return instruments.get(instrumentId).getMultiple();
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

    private double getPositionProfit(Collection<Contract> contracts,
                                     Map<String, SettlementPrice> settlements,
                                     Map<String, Instrument> instruments) {
        final Double[] r = new Double[]{0.0D};
        contracts.forEach(c -> {
            double price = getSettlementPrice(c.getInstrumentId(),
                                              settlements);
            double amount = price * getMultiple(c.getInstrumentId(),
                                                instruments);
            if (c.getCloseAmount() != null) {
                return;
            }
            if (c.getDirection() == Direction.BUY) {
                r[0] += amount - c.getOpenAmount();
            } else {
                r[0] += c.getOpenAmount() - amount;
            }
        });
        return r[0];
    }

    private double getSettlementPrice(String instrumentId,
                                      Map<String, SettlementPrice> settlements) {
        return settlements.get(instrumentId).getSettlementPrice();
    }

    private void testTradedOrder(Long orderId) {
        var r = requests().get(orderId);
        var trades = getTradesByOrderId(r.getOrderId());
        var cs = getContractsByTrades(trades);
        var rs = getResponsesByOrderId(r.getOrderId());

        assertDoesNotThrow(() -> {
            var order = algorithm().getOrder(r,
                                             cs,
                                             trades,
                                             rs,
                                             instruments());
            assertEquals(OrderStatus.ALL_TRADED,
                         order.getStatus());
            assertEquals(r.getQuantity(),
                         order.getTradedVolumn());
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
