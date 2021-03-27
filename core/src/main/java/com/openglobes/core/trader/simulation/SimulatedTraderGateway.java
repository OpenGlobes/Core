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

import com.openglobes.core.GatewayException;
import com.openglobes.core.GatewayRuntimeException;
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.InvalidSubscriptionException;
import com.openglobes.core.event.NoSubscribedClassException;
import com.openglobes.core.trader.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class SimulatedTraderGateway implements ITraderGateway {

    private final TraderGatewayInfo info = new TraderGatewayInfo();
    private final Map<String, MarketMaker> makers = new HashMap<>();
    private final EventSource es = new EventSource();
    private ITraderGatewayHandler handler = null;
    private int status = 0;

    public SimulatedTraderGateway() {
        try {
            es.subscribe(Request.class, event -> {
                var request = event.get();
                var m = makers.computeIfAbsent(request.getInstrumentId(), k -> new MarketMaker());
                m.enqueueRequest(request);
                invokeHandler(m, request);
                m.matchTrade(request.getDirection());
                invokeHandler(m, request);
            });
        } catch (InvalidSubscriptionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void invokeHandler(MarketMaker m, Request r) {
        m.getTradeUpdates().forEach(trade -> {
            try {
                handler.onTrade(trade);
            } catch (Throwable th) {
                handler.onError(new GatewayRuntimeException(-1, th.getMessage(), th));
            }
        });
        m.getResponseUpdates().forEach(response -> {
            try {
                if (response.getStatus() == OrderStatus.REJECTED) {
                    handler.onError(r, response);
                } else {
                    handler.onResponse(response);
                }
            } catch (Throwable th) {
                handler.onError(new GatewayRuntimeException(-1, th.getMessage(), th));
            }
        });
    }

    @Override
    public TraderGatewayInfo getGatewayInfo() {
        return info;
    }

    @Override
    public void setHandler(ITraderGatewayHandler handler) throws GatewayException {
        this.handler = handler;
    }

    public MarketMaker getMarketer(String instrumentId) {
        return makers.get(instrumentId);
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void insert(Request request) throws GatewayException {
        Objects.requireNonNull(request);
        Objects.requireNonNull(request.getInstrumentId());
        try {
            es.publish(Request.class, request);
        } catch (NoSubscribedClassException e) {
            throw new GatewayException(-1, e.getMessage(), e);
        }
    }

}
