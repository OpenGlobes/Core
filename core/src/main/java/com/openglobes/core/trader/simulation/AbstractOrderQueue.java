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

import com.openglobes.core.trader.IllegalRequestException;
import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.Trade;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class AbstractOrderQueue extends LinkedList<RequestBucket> {

    protected final Map<Long, RequestBucket> buckets = new HashMap<>(64);

    @Override
    public Object clone() {
        return super.clone();
    }

    public void dequeOrder(Long orderId) throws UnkownOrderIdException {
        if (!buckets.containsKey(orderId)) {
            throw new UnkownOrderIdException("Unknown order ID: " + orderId + ".");
        }
        buckets.get(orderId).removeOrder(orderId);
    }

    public void enqueOrder(Request request) throws IllegalRequestException {
        try {
            checkRequest(request);
            var b = findBucketAtPrice(request);
            b.enqueueRequest(request);
            buckets.put(request.getOrderId(), b);
        } catch (Throwable th) {
            throw new IllegalRequestException(th.getMessage(), th);
        }
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
        return r;
    }

    protected abstract void sortBuckets();

    protected abstract void checkRequest(Request request);
}
