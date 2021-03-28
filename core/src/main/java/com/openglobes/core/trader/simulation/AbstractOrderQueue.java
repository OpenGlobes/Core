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

import com.openglobes.core.trader.OrderStatus;
import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.Trade;
import com.openglobes.core.utils.Utils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

public abstract class AbstractOrderQueue extends LinkedList<RequestBucket> {
    protected final LinkedList<Response> responses = new LinkedList<>();

    @Override
    public Object clone() {
        return super.clone();
    }

    public void dequeOrder(Request request) throws NoSuchElementException {
        var orderId = request.getOrderId();
        for (var b : this) {
            if (b.removeOrder(orderId)) {
                return;
            }
        }
        addResponse(request,
                    OrderStatus.REJECTED,
                    100,
                    "找不到报单");
    }

    public void enqueOrder(Request request) throws IllegalArgumentException {
        try {
            checkOffset(request);
            findBucketAtPrice(request).enqueueRequest(request);
        } catch (Throwable th) {
            throw new IllegalArgumentException(th.getMessage(), th);
        }
    }

    public static Response createResponseWithError(Request request, int status, int code, String msg) {
        var r = new Response();
        r.setAction(request.getAction());
        r.setResponseId(Utils.nextId());
        r.setDirection(request.getDirection());
        r.setStatus(status);
        r.setOffset(request.getOffset());
        r.setInstrumentId(request.getInstrumentId());
        r.setOrderId(request.getOrderId());
        r.setSignature(UUID.randomUUID().toString());
        r.setStatusCode(code);
        r.setStatusMessage(msg);
        r.setTimestamp(ZonedDateTime.now());
        r.setTraderId(Integer.MAX_VALUE);
        r.setTradingDay(LocalDate.now());
        return r;
    }

    private void addResponse(Request request, int status, int code, String msg) {
        responses.add(createResponseWithError(request, status, code, msg));
    }

    protected RequestBucket findBucketAtPrice(Request request) {
        for (var b : this) {
            if (b.getPrice().equals(request.getPrice())) {
                return b;
            }
        }
        var b = new RequestBucket(request.getPrice(),
                                  request.getDirection(),
                                  request.getOffset());
        add(b);
        sortBuckets();
        return b;
    }

    public Collection<Trade> getTradeUpdates() {
        var r = new LinkedList<Trade>();
        forEach(bucket -> {
            r.addAll(bucket.getTradeUpdates());
        });
        return r;
    }

    public Collection<Response> getResponseUpdates() {
        var r = new LinkedList<Response>();
        forEach(bucket -> {
            r.addAll(bucket.getResponseUpdates());
        });
        r.addAll(responses);
        responses.clear();
        return r;
    }

    protected abstract void sortBuckets();

    protected abstract void checkOffset(Request request);
}
