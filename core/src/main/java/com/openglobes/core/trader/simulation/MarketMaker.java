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

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class MarketMaker implements IMarketMaker {

    private final AbstractOrderQueue askQue;
    private final AbstractOrderQueue bidQue;
    private final LinkedList<Trade> trades = new LinkedList<>();
    private final LinkedList<Response> responses = new LinkedList<>();

    public MarketMaker() {
        askQue = new AskingOrderQueue();
        bidQue = new BidingOrderQueue();
    }

    @Override
    public Collection<Trade> getTradeUpdates() {
        var r = new LinkedList<Trade>();
        r.addAll(askQue.getTradeUpdates());
        r.addAll(bidQue.getTradeUpdates());
        r.addAll(trades);
        trades.clear();
        return r;
    }

    @Override
    public Collection<Response> getResponseUpdates() {
        var r = new LinkedList<Response>();
        r.addAll(askQue.getResponseUpdates());
        r.addAll(bidQue.getResponseUpdates());
        r.addAll(responses);
        responses.clear();
        return r;
    }

    /**
     * Request with null order ID won't check ID duplication.
     *
     * @param request request to put on queue.
     */
    @Override
    public void enqueueRequest(Request request) {
        switch (request.getAction()) {
            case ActionType.NEW:
                newOrder(request);
                break;
            case ActionType.DELETE:
                deleteOrder(request);
                break;
            default:
                throw new IllegalArgumentException("Illegal action: " + request.getAction() + ".");
        }
    }

    private void deleteOrder(Request request) throws NoSuchElementException {
        switch (request.getDirection()) {
            case Direction.BUY:
                bidQue.dequeOrder(request);
                break;
            case Direction.SELL:
                askQue.dequeOrder(request);
                break;
            default:
                throw new IllegalArgumentException("Illegal direction: " + request.getDirection() + ".");
        }
    }

    private void newOrder(Request request) {
        switch (request.getDirection()) {
            case Direction.BUY:
                bidQue.enqueOrder(request);
                break;
            case Direction.SELL:
                askQue.enqueOrder(request);
                break;
            default:
                throw new IllegalArgumentException("Illegal direction: " + request.getDirection() + ".");
        }
    }

    @Override
    public void matchTrade(int direction) {
        switch (direction) {
            case Direction.BUY:
                buy();
                break;
            case Direction.SELL:
                sell();
                break;
            default:
                throw new IllegalArgumentException("Illegal direction: " + direction + ".");
        }
    }

    private void sell() {
        trade(askQue, bidQue, -1);
    }

    private void trimQueue() {
        trimQueueX(askQue);
        trimQueueX(bidQue);
    }

    private void trimQueueX(LinkedList<RequestBucket> buckets) {
        var it = buckets.iterator();
        while (it.hasNext()) {
            var n = it.next();
            if (n.getVolumn() == 0) {
                trades.addAll(n.getTradeUpdates());
                responses.addAll(n.getResponseUpdates());
                it.remove();
            }
        }
    }

    private void buy() {
        trade(bidQue, askQue, 1);
    }

    private void trade(LinkedList<RequestBucket> heads, LinkedList<RequestBucket> tos, int sign) {
        RequestBucket head = heads.getFirst();
        RequestBucket to = tos.getFirst();
        while ((head.getPrice() - to.getPrice()) * sign >= 0) {
            activeTrade(head, to);
            trimQueue();
            head = heads.getFirst();
            to = tos.getFirst();
        }
    }

    private void activeTrade(RequestBucket head, RequestBucket to) {
        var q = Math.min(head.getVolumn(), to.getVolumn());
        var r = new Request();
        r.setPrice(to.getPrice());
        r.setQuantity(q);
        r.setDirection(head.getDirection());
        to.applyRequest(r);
        r.setDirection(oppositeDirection(head.getDirection()));
        head.applyRequest(r);
    }

    private int oppositeDirection(int direction) {
        switch (direction) {
            case Direction.BUY:
                return Direction.SELL;
            case Direction.SELL:
                return Direction.BUY;
            default:
                throw new IllegalArgumentException("Illegal direction: " + direction + ".");
        }
    }
}
