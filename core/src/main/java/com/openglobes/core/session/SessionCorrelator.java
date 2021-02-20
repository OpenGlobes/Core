/*
 * Copyright (C) 2020 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.session;

import com.openglobes.core.trader.Request;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Hongbao
 * @since 1.0
 */
public class SessionCorrelator implements ISessionCorrelator {

    private final Map<Long, ISession> map = new ConcurrentHashMap<>(128);
    private final AtomicLong uId = new AtomicLong(0);

    public SessionCorrelator() {
    }

    @Override
    public ISession getSessionByOrderId(Long orderId) {
        Objects.requireNonNull(orderId);
        var r = map.get(orderId);
        Objects.requireNonNull(r);
        return r;
    }

    @Override
    public Long registerRequest(Request request, ISession session) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(session);
        var oldId = request.getOrderId();
        registerWithNewId(request, session);
        return oldId;
    }

    private Long getNewId() {
        return uId.incrementAndGet();
    }

    private void registerWithNewId(Request request, ISession session) {
        var newId = getNewId();
        request.setOrderId(newId);
        map.put(newId, session);
    }
}
