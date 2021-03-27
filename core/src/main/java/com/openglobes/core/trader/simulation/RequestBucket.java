/*
 * Copyright (C) 2020-2021 Hongbao Chen <chenhongbao@outlook.com>
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

import com.openglobes.core.trader.*;
import com.openglobes.core.utils.Utils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
class RequestBucket extends LinkedList<Order> implements IRequestBucket {

    private final double price;
    private final int direction;
    private final int offset;
    private final LinkedList<Trade> trades = new LinkedList<>();
    private final LinkedList<Response> responses = new LinkedList<>();

    public RequestBucket(double price, int direction, int offset) {
        this.price = price;
        this.direction = direction;
        this.offset = offset;
    }

    public void enqueueRequest(Request request) {
        var o = new Order();
        o.setStatus(OrderStatus.ACCEPTED);
        o.setTradedVolumn(0L);
        o.setDirection(request.getDirection());
        o.setDeleted(false);
        o.setOffset(request.getOffset());
        o.setDeleteTimestamp(null);
        o.setInsertTimestamp(ZonedDateTime.now());
        o.setInstrumentId(request.getInstrumentId());
        o.setOrderId(request.getOrderId());
        o.setPrice(request.getPrice());
        o.setQuantity(request.getQuantity());
        o.setTraderId(Integer.MAX_VALUE);
        o.setTradingDay(LocalDate.now());
        o.setUpdateTimestamp(ZonedDateTime.now());
        addResponse(o);
        super.add(o);
    }

    public Collection<Trade> getTradeUpdates() {
        var r = new LinkedList<>(trades);
        trades.clear();
        return r;
    }

    public Collection<Response> getResponseUpdates() {
        var r = new LinkedList<>(responses);
        responses.clear();
        return r;
    }

    public boolean removeOrder(Long orderId) {
        if (isEmpty()) {
            throw new NoSuchElementException("Empty container.");
        }
        for (int i = 0; i < size(); ++i) {
            var o = get(i);
            if (o.getOrderId().equals(orderId)) {
                o.setStatus(OrderStatus.DELETED);
                addResponse(o);
                remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Trade the request with the specified volumn and price. The real traded price
     * is the price of the specified request.
     *
     * @param request request to be trade on the queue.
     */
    public void applyRequest(Request request) {
        var vol = request.getQuantity();
        var it = iterator();
        while (it.hasNext() && vol > 0) {
            var order = it.next();
            checkRequest(request, order);
            var traded = Math.min(vol, order.getQuantity() - order.getTradedVolumn());
            doOrder(traded, request.getPrice(), order);
            vol -= traded;
        }
        if (vol != 0) {
            throw new IllegalStateException("Fail applying request with volumn diff: " + vol + ".");
        }
    }

    private void doOrder(long traded, double price, Order order) {
        order.setTradedVolumn(order.getTradedVolumn() + traded);
        if (order.getTradedVolumn() == order.getQuantity()) {
            order.setStatus(OrderStatus.ALL_TRADED);
            addResponse(order);
        } else {
            order.setStatus(OrderStatus.QUEUED);
            /* First trade on this order. */
            if (order.getTradedVolumn() == traded) {
                addResponse(order);
            }
        }
        addTrade(traded, price, order);
    }

    private void addResponse(Order order) {
        var r = new Response();
        r.setAction(ActionType.NEW);
        r.setResponseId(Utils.nextId());
        r.setDirection(order.getDirection());
        r.setStatus(order.getStatus());
        r.setOffset(order.getOffset());
        r.setInstrumentId(order.getInstrumentId());
        r.setOrderId(order.getOrderId());
        r.setSignature(UUID.randomUUID().toString());
        r.setStatusCode(0);
        setResponseMsg(r, order);
        r.setTimestamp(ZonedDateTime.now());
        r.setTraderId(Integer.MAX_VALUE);
        r.setTradingDay(LocalDate.now());
        responses.add(r);
    }

    private void setResponseMsg(Response r, Order order) {
        switch (order.getStatus()) {
            case OrderStatus.ALL_TRADED:
                r.setStatusMessage("全部成交");
                break;
            case OrderStatus.ACCEPTED:
                r.setStatusMessage("已提交");
                break;
            case OrderStatus.QUEUED:
                r.setStatusMessage("部分成交队列中");
                break;
            default:
                throw new IllegalArgumentException("Unexpected order status: " + order.getStatus() + ".");
        }
    }

    private void addTrade(long traded, double price, Order order) {
        var r = new Trade();
        r.setAction(ActionType.NEW);
        r.setDirection(order.getDirection());
        r.setOrderId(order.getOrderId());
        r.setOffset(order.getOffset());
        r.setPrice(price);
        r.setInstrumentId(order.getInstrumentId());
        r.setQuantity(traded);
        r.setSignature(UUID.randomUUID().toString());
        r.setTimestamp(ZonedDateTime.now());
        r.setTradeId(Utils.nextId());
        r.setTraderId(Integer.MAX_VALUE);
        r.setTradingDay(LocalDate.now());
        trades.add(r);
    }

    private void checkRequest(Request request, Order order) {
        if (request.getDirection() == order.getDirection()) {
            throw new IllegalStateException("Request and order directions are not matched.");
        }
        if (order.getQuantity() - order.getTradedVolumn() <= 0) {
            throw new IllegalStateException("Trade on a completed order.");
        }
    }

    @Override
    public Double getPrice() {
        return price;
    }

    @Override
    public Long getVolumn() {
        return sumUpVolumn();
    }

    private Long sumUpVolumn() {
        var v = 0L;
        for (var o : this) {
            var x = o.getQuantity() - o.getTradedVolumn();
            if (x <= 0) {
                throw new IllegalStateException("Order has been completed or over completed.");
            }
            v += x;
        }
        return v;
    }

    @Override
    public int getDirection() {
        return direction;
    }

    @Override
    public int getOffset() {
        return offset;
    }
}
