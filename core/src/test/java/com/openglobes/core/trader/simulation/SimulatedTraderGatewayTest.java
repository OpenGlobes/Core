/*
 * Copyright (c) 2020-2021. Hongbao Chen <chenhongbao@outlook.com>
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

package com.openglobes.core.trader.simulation;

import com.openglobes.core.GatewayRuntimeException;
import com.openglobes.core.ServiceRuntimeStatus;
import com.openglobes.core.trader.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SimulatedTraderGatewayTest extends SimTestSupporter {

    private final ITraderGateway gateway = new SimulatedTraderGateway();

    @BeforeEach
    public void setup() {
        gateway.setHandler(new ITraderGatewayHandler() {
            @Override
            public void onTrade(Trade trade) {
                trades(trade.getOrderId()).add(trade);
            }

            @Override
            public void onResponse(Response response) {
                if (response.getStatus() == OrderStatus.REJECTED) {
                    badResponses(response.getOrderId()).add(response);
                } else {
                    goodResponses(response.getOrderId()).add(response);
                }
            }

            @Override
            public void onError(GatewayRuntimeException exception) {
                fail(exception.getMessage());
            }

            @Override
            public void onStatusChange(ServiceRuntimeStatus status) {
                fail(status.getMessage());
            }
        });
        prepare();
    }

    private void prepare() {
        /*
         * Prepare the order book.
         * The requests will not need responses, so set order ID to null.
         */
        var mine = SimGatewayUtils.createNewRequest(1L,
                                                    101L,
                                                    "c2109",
                                                    2690.0D,
                                                    5L,
                                                    Direction.BUY,
                                                    Offset.OPEN);
        gateway.insert(mine);
        mine = SimGatewayUtils.createNewRequest(2L,
                                                102L,
                                                "c2109",
                                                2689.0D,
                                                4L,
                                                Direction.BUY,
                                                Offset.CLOSE_TODAY);
        gateway.insert(mine);
        mine = SimGatewayUtils.createNewRequest(3L,
                                                103L,
                                                "c2109",
                                                2691.0D,
                                                6L,
                                                Direction.SELL,
                                                Offset.CLOSE_AUTO);
        gateway.insert(mine);
    }

    @Test
    @DisplayName("Good request.")
    public void testGoodRequest() {
        var r = SimGatewayUtils.createNewRequest(21L,
                                                 201L,
                                                 "c2109",
                                                 2690.0D,
                                                 1L,
                                                 Direction.SELL,
                                                 Offset.CLOSE_YD);
        gateway.insert(r);
        waitResponse();
        /*
         * Check responses for Order(1L).
         */
        assertEquals(2, goodResponses(1L).size());
        assertEquals(1, trades(1L).size());
        assertEquals(0, badResponses(1L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(1L).get(0).getStatus());
        assertEquals(OrderStatus.QUEUED, goodResponses(1L).get(1).getStatus());

        assertEquals(1L, goodResponses(1L).get(1).getOrderId());
        assertEquals(Direction.BUY, goodResponses(1L).get(1).getDirection());
        assertEquals(Offset.OPEN, goodResponses(1L).get(1).getOffset());
        assertEquals(ActionType.NEW, goodResponses(1L).get(1).getAction());
        assertEquals("c2109", goodResponses(1L).get(1).getInstrumentId());
        assertEquals(0, goodResponses(1L).get(1).getStatusCode());
        assertEquals("部分成交队列中", goodResponses(1L).get(1).getStatusMessage());

        /*
         * Check responses for Order(21L).
         */
        assertEquals(2, goodResponses(21L).size());
        assertEquals(1, trades(21L).size());
        assertEquals(0, badResponses(21L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(21L).get(0).getStatus());
        assertEquals(OrderStatus.ALL_TRADED, goodResponses(21L).get(1).getStatus());

        assertEquals(21L, goodResponses(21L).get(1).getOrderId());
        assertEquals(Direction.SELL, goodResponses(21L).get(1).getDirection());
        assertEquals(Offset.CLOSE_YD, goodResponses(21L).get(1).getOffset());
        assertEquals(ActionType.NEW, goodResponses(21L).get(1).getAction());
        assertEquals("c2109", goodResponses(21L).get(1).getInstrumentId());
        assertEquals(0, goodResponses(21L).get(1).getStatusCode());
        assertEquals("全部成交", goodResponses(21L).get(1).getStatusMessage());

        /*
         * Check trades for Order(1L).
         */
        assertEquals(1, trades(1L).get(0).getQuantity());
        assertEquals(2690.0D, trades(1L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.BUY, trades(1L).get(0).getDirection());
        assertEquals(Offset.OPEN, trades(1L).get(0).getOffset());
        assertEquals("c2109", trades(1L).get(0).getInstrumentId());
        assertEquals(1L, trades(1L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(1L).get(0).getAction());

        /*
         * Check trades for Order(21L).
         */
        assertEquals(1, trades(21L).get(0).getQuantity());
        assertEquals(2690.0D, trades(21L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.SELL, trades(21L).get(0).getDirection());
        assertEquals(Offset.CLOSE_YD, trades(21L).get(0).getOffset());
        assertEquals("c2109", trades(21L).get(0).getInstrumentId());
        assertEquals(21L, trades(21L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(21L).get(0).getAction());

        r = SimGatewayUtils.createNewRequest(22L,
                                             202L,
                                             "c2109",
                                             2690.0D,
                                             5L,
                                             Direction.SELL,
                                             Offset.CLOSE_TODAY);
        gateway.insert(r);
        waitResponse();

        /*
         * Check responses for Order(1L).
         */
        assertEquals(3, goodResponses(1L).size());
        assertEquals(2, trades(1L).size());
        assertEquals(0, badResponses(1L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(1L).get(0).getStatus());
        assertEquals(OrderStatus.QUEUED, goodResponses(1L).get(1).getStatus());
        assertEquals(OrderStatus.ALL_TRADED, goodResponses(1L).get(2).getStatus());

        assertEquals(1L, goodResponses(1L).get(2).getOrderId());
        assertEquals(Direction.BUY, goodResponses(1L).get(2).getDirection());
        assertEquals(Offset.OPEN, goodResponses(1L).get(2).getOffset());
        assertEquals(ActionType.NEW, goodResponses(1L).get(2).getAction());
        assertEquals("c2109", goodResponses(1L).get(2).getInstrumentId());
        assertEquals(0, goodResponses(1L).get(2).getStatusCode());
        assertEquals("全部成交", goodResponses(1L).get(2).getStatusMessage());

        /*
         * Check responses for Order(22L).
         */
        assertEquals(2, goodResponses(22L).size());
        assertEquals(1, trades(22L).size());
        assertEquals(0, badResponses(22L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(22L).get(0).getStatus());
        assertEquals(OrderStatus.QUEUED, goodResponses(22L).get(1).getStatus());

        assertEquals(22L, goodResponses(22L).get(1).getOrderId());
        assertEquals(Direction.SELL, goodResponses(22L).get(1).getDirection());
        assertEquals(Offset.CLOSE_TODAY, goodResponses(22L).get(1).getOffset());
        assertEquals(ActionType.NEW, goodResponses(22L).get(1).getAction());
        assertEquals("c2109", goodResponses(22L).get(1).getInstrumentId());
        assertEquals(0, goodResponses(22L).get(1).getStatusCode());
        assertEquals("部分成交队列中", goodResponses(22L).get(1).getStatusMessage());

        /*
         * Check trades for Order(1L).
         */
        assertEquals(4, trades(1L).get(1).getQuantity());
        assertEquals(2690.0D, trades(1L).get(1).getPrice(), 0.5D);
        assertEquals(Direction.BUY, trades(1L).get(1).getDirection());
        assertEquals(Offset.OPEN, trades(1L).get(1).getOffset());
        assertEquals("c2109", trades(1L).get(1).getInstrumentId());
        assertEquals(1L, trades(1L).get(1).getOrderId());
        assertEquals(ActionType.NEW, trades(1L).get(1).getAction());

        /*
         * Check trades for Order(22L).
         */
        assertEquals(4, trades(22L).get(0).getQuantity());
        assertEquals(2690.0D, trades(22L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.SELL, trades(22L).get(0).getDirection());
        assertEquals(Offset.CLOSE_TODAY, trades(22L).get(0).getOffset());
        assertEquals("c2109", trades(22L).get(0).getInstrumentId());
        assertEquals(22L, trades(22L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(22L).get(0).getAction());

        r = SimGatewayUtils.createNewRequest(23L,
                                             203L,
                                             "c2109",
                                             2690.0D,
                                             1L,
                                             Direction.BUY,
                                             Offset.CLOSE_TODAY);
        gateway.insert(r);
        waitResponse();

        /*
         * Check responses for Order(22L).
         */
        assertEquals(3, goodResponses(22L).size());
        assertEquals(2, trades(22L).size());
        assertEquals(0, badResponses(22L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(22L).get(0).getStatus());
        assertEquals(OrderStatus.QUEUED, goodResponses(22L).get(1).getStatus());
        assertEquals(OrderStatus.ALL_TRADED, goodResponses(22L).get(2).getStatus());

        assertEquals(22L, goodResponses(22L).get(2).getOrderId());
        assertEquals(Direction.SELL, goodResponses(22L).get(2).getDirection());
        assertEquals(Offset.CLOSE_TODAY, goodResponses(22L).get(2).getOffset());
        assertEquals(ActionType.NEW, goodResponses(22L).get(2).getAction());
        assertEquals("c2109", goodResponses(22L).get(2).getInstrumentId());
        assertEquals(0, goodResponses(22L).get(2).getStatusCode());
        assertEquals("全部成交", goodResponses(22L).get(2).getStatusMessage());

        /*
         * Check trades for Order(22L).
         */
        assertEquals(1, trades(22L).get(1).getQuantity());
        assertEquals(2690.0D, trades(22L).get(1).getPrice(), 0.5D);
        assertEquals(Direction.SELL, trades(22L).get(1).getDirection());
        assertEquals(Offset.CLOSE_TODAY, trades(22L).get(1).getOffset());
        assertEquals("c2109", trades(22L).get(1).getInstrumentId());
        assertEquals(22L, trades(22L).get(1).getOrderId());
        assertEquals(ActionType.NEW, trades(22L).get(1).getAction());

        /*
         * Check responses for Order(23L).
         */
        assertEquals(2, goodResponses(23L).size());
        assertEquals(1, trades(23L).size());
        assertEquals(0, badResponses(23L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(23L).get(0).getStatus());
        assertEquals(OrderStatus.ALL_TRADED, goodResponses(23L).get(1).getStatus());

        assertEquals(23L, goodResponses(23L).get(1).getOrderId());
        assertEquals(Direction.BUY, goodResponses(23L).get(1).getDirection());
        assertEquals(Offset.CLOSE_TODAY, goodResponses(23L).get(1).getOffset());
        assertEquals(ActionType.NEW, goodResponses(23L).get(1).getAction());
        assertEquals("c2109", goodResponses(23L).get(1).getInstrumentId());
        assertEquals(0, goodResponses(23L).get(1).getStatusCode());
        assertEquals("全部成交", goodResponses(23L).get(1).getStatusMessage());

        /*
         * Check trades for Order(23L).
         */
        assertEquals(1, trades(23L).get(0).getQuantity());
        assertEquals(2690.0D, trades(23L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.BUY, trades(23L).get(0).getDirection());
        assertEquals(Offset.CLOSE_TODAY, trades(23L).get(0).getOffset());
        assertEquals("c2109", trades(23L).get(0).getInstrumentId());
        assertEquals(23L, trades(23L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(23L).get(0).getAction());
    }

    @Test
    @DisplayName("Bad request.")
    public void testBadRequest() {
        var r = SimGatewayUtils.createNewRequest(1L,
                                                 1001L,
                                                 "c2109",
                                                 2690.0D,
                                                 15L,
                                                 Direction.SELL,
                                                 Offset.CLOSE_TODAY);
        gateway.insert(r);
        waitResponse();

        /*
         * Check bad requests and responses.
         */
        assertEquals(1, goodResponses(1L).size());
        assertEquals(0, trades(1L).size());
        assertEquals(1, badResponses(1L).size());

        /*
         * Check bad response internals.
         */
        assertEquals(OrderStatus.REJECTED, badResponses(1L).get(0).getStatus());
        assertEquals(102, badResponses(1L).get(0).getStatusCode());
        assertEquals("重复报单", badResponses(1L).get(0).getStatusMessage());
    }

    @Test
    @DisplayName("Deleted request.")
    public void testDeletedRequest() {
        var r = SimGatewayUtils.createNewRequest(24L,
                                                 204L,
                                                 "c2109",
                                                 2690.0D,
                                                 1L,
                                                 Direction.SELL,
                                                 Offset.CLOSE_YD);
        gateway.insert(r);

        /*
         * Delete request.
         */
        r = SimGatewayUtils.createDeleteRequest(1L, 205L, "c2109", Direction.BUY);
        gateway.insert(r);
        waitResponse();

        /*
         * Check responses for Order(1L).
         */
        assertEquals(3, goodResponses(1L).size());
        assertEquals(1, trades(1L).size());
        assertEquals(0, badResponses(1L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(1L).get(0).getStatus());
        assertEquals(OrderStatus.QUEUED, goodResponses(1L).get(1).getStatus());
        assertEquals(OrderStatus.DELETED, goodResponses(1L).get(2).getStatus());

        assertEquals(1L, goodResponses(1L).get(2).getOrderId());
        assertEquals(Direction.BUY, goodResponses(1L).get(2).getDirection());
        assertEquals(Offset.OPEN, goodResponses(1L).get(2).getOffset());
        assertEquals(ActionType.NEW, goodResponses(1L).get(2).getAction());
        assertEquals("c2109", goodResponses(1L).get(2).getInstrumentId());
        assertEquals(0, goodResponses(1L).get(2).getStatusCode());
        assertEquals("已撤单", goodResponses(1L).get(2).getStatusMessage());

        /*
         * Check trades for Order(1L).
         */
        assertEquals(1, trades(1L).get(0).getQuantity());
        assertEquals(2690.0D, trades(1L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.BUY, trades(1L).get(0).getDirection());
        assertEquals(Offset.OPEN, trades(1L).get(0).getOffset());
        assertEquals("c2109", trades(1L).get(0).getInstrumentId());
        assertEquals(1L, trades(1L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(1L).get(0).getAction());
    }

    @Test
    @DisplayName("Bad deleted request.")
    public void testBadDelete() {
        var r = SimGatewayUtils.createDeleteRequest(9999L, 206L, "c2109", Direction.BUY);
        gateway.insert(r);
        waitResponse();

        /*
         * Check responses for wrong order ID.
         */
        assertEquals(0, goodResponses(9999L).size());
        assertEquals(0, trades(9999L).size());
        assertEquals(1, badResponses(9999L).size());

        assertEquals(OrderStatus.REJECTED, badResponses(9999L).get(0).getStatus());
        assertEquals(100, badResponses(9999L).get(0).getStatusCode());
        assertEquals("找不到报单", badResponses(9999L).get(0).getStatusMessage());

        /*
         * Check response details.
         */
        assertEquals(9999L, badResponses(9999L).get(0).getOrderId());
        assertEquals(ActionType.DELETE, badResponses(9999L).get(0).getAction());
        assertEquals("c2109", badResponses(9999L).get(0).getInstrumentId());
        assertEquals(Direction.BUY, badResponses(9999L).get(0).getDirection());
    }

    @Test
    @DisplayName("Sweep all orders.")
    public void testSweep() {
        var r = SimGatewayUtils.createNewRequest(25L,
                                                 208L,
                                                 "c2109",
                                                 2688.0D,
                                                 20L,
                                                 Direction.SELL,
                                                 Offset.CLOSE_YD);
        gateway.insert(r);
        waitResponse();

        /*
         * Check responses for Order(25L).
         */
        assertEquals(2, goodResponses(25L).size());
        assertEquals(2, trades(25L).size());
        assertEquals(0, badResponses(25L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(25L).get(0).getStatus());
        assertEquals(OrderStatus.QUEUED, goodResponses(25L).get(1).getStatus());

        assertEquals(25L, goodResponses(25L).get(1).getOrderId());
        assertEquals(Direction.SELL, goodResponses(25L).get(1).getDirection());
        assertEquals(Offset.CLOSE_YD, goodResponses(25L).get(1).getOffset());
        assertEquals(ActionType.NEW, goodResponses(25L).get(1).getAction());
        assertEquals("c2109", goodResponses(25L).get(1).getInstrumentId());
        assertEquals(0, goodResponses(25L).get(1).getStatusCode());
        assertEquals("部分成交队列中", goodResponses(25L).get(1).getStatusMessage());

        /*
         * Check trades for Order(23L).
         */
        assertEquals(5, trades(25L).get(0).getQuantity());
        assertEquals(2690.0D, trades(25L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.SELL, trades(25L).get(0).getDirection());
        assertEquals(Offset.CLOSE_YD, trades(25L).get(0).getOffset());
        assertEquals("c2109", trades(25L).get(0).getInstrumentId());
        assertEquals(25L, trades(25L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(25L).get(0).getAction());

        assertEquals(4, trades(25L).get(1).getQuantity());
        assertEquals(2689.0D, trades(25L).get(1).getPrice(), 0.5D);
        assertEquals(Direction.SELL, trades(25L).get(1).getDirection());
        assertEquals(Offset.CLOSE_YD, trades(25L).get(1).getOffset());
        assertEquals("c2109", trades(25L).get(1).getInstrumentId());
        assertEquals(25L, trades(25L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(25L).get(1).getAction());

        /*
         * Check responses for Order(1L).
         */
        assertEquals(2, goodResponses(1L).size());
        assertEquals(1, trades(1L).size());
        assertEquals(0, badResponses(1L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(1L).get(0).getStatus());
        assertEquals(OrderStatus.ALL_TRADED, goodResponses(1L).get(1).getStatus());

        assertEquals(1L, goodResponses(1L).get(1).getOrderId());
        assertEquals(Direction.BUY, goodResponses(1L).get(1).getDirection());
        assertEquals(Offset.OPEN, goodResponses(1L).get(1).getOffset());
        assertEquals(ActionType.NEW, goodResponses(1L).get(1).getAction());
        assertEquals("c2109", goodResponses(1L).get(1).getInstrumentId());
        assertEquals(0, goodResponses(1L).get(1).getStatusCode());
        assertEquals("全部成交", goodResponses(1L).get(1).getStatusMessage());

        /*
         * Check trades for Order(1L).
         */
        assertEquals(5, trades(1L).get(0).getQuantity());
        assertEquals(2690.0D, trades(1L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.BUY, trades(1L).get(0).getDirection());
        assertEquals(Offset.OPEN, trades(1L).get(0).getOffset());
        assertEquals("c2109", trades(1L).get(0).getInstrumentId());
        assertEquals(1L, trades(1L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(1L).get(0).getAction());

        /*
         * Check responses for Order(2L).
         */
        assertEquals(2, goodResponses(2L).size());
        assertEquals(1, trades(2L).size());
        assertEquals(0, badResponses(2L).size());

        assertEquals(OrderStatus.ACCEPTED, goodResponses(2L).get(0).getStatus());
        assertEquals(OrderStatus.ALL_TRADED, goodResponses(2L).get(1).getStatus());

        assertEquals(2L, goodResponses(2L).get(1).getOrderId());
        assertEquals(Direction.BUY, goodResponses(2L).get(1).getDirection());
        assertEquals(Offset.CLOSE_TODAY, goodResponses(2L).get(1).getOffset());
        assertEquals(ActionType.NEW, goodResponses(2L).get(1).getAction());
        assertEquals("c2109", goodResponses(2L).get(1).getInstrumentId());
        assertEquals(0, goodResponses(2L).get(1).getStatusCode());
        assertEquals("全部成交", goodResponses(2L).get(1).getStatusMessage());

        /*
         * Check trades for Order(2L).
         */
        assertEquals(4, trades(2L).get(0).getQuantity());
        assertEquals(2689.0D, trades(2L).get(0).getPrice(), 0.5D);
        assertEquals(Direction.BUY, trades(2L).get(0).getDirection());
        assertEquals(Offset.CLOSE_TODAY, trades(2L).get(0).getOffset());
        assertEquals("c2109", trades(2L).get(0).getInstrumentId());
        assertEquals(2L, trades(2L).get(0).getOrderId());
        assertEquals(ActionType.NEW, trades(2L).get(0).getAction());
    }
}