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

import com.openglobes.core.GatewayRuntimeException;
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.InvalidSubscriptionException;
import com.openglobes.core.event.NoSubscribedClassException;
import com.openglobes.core.trader.*;
import com.openglobes.core.utils.Loggers;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class SimulatedTraderGateway implements ITraderGateway {

    protected final Set<Long> orderIds = new HashSet<>(64);
    protected final LinkedList<Response> responses = new LinkedList<>();
    private final TraderGatewayInfo info = new TraderGatewayInfo();
    private final Map<String, MarketMaker> makers = new HashMap<>();
    private final EventSource es = new EventSource();
    private ITraderGatewayHandler handler = null;
    private int status = 0;

    public SimulatedTraderGateway() {
        try {
            es.subscribe(Request.class, event -> {
                var request = event.get();
                var m = makers.computeIfAbsent(request.getInstrumentId(),
                                               k -> new MarketMaker());
                if (!isRequestValid(request)) {
                    invokeHandler(m, request);
                    return;
                }
                try {
                    m.enqueueRequest(request);
                } catch (Throwable th) {
                    Loggers.getLogger(SimulatedTraderGateway.class.getCanonicalName())
                           .severe(th.getMessage());
                }
                invokeHandler(m, request);
                try {
                    m.matchTrade(request);
                } catch (Throwable th) {
                    Loggers.getLogger(SimulatedTraderGateway.class.getCanonicalName())
                           .severe(th.getMessage());
                }
                invokeHandler(m, request);
            });
        } catch (InvalidSubscriptionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void addResponse(Request request, int status, int code, String msg) {
        responses.add(AbstractOrderQueue.createResponseWithError(request, status, code, msg));
    }

    private boolean isRequestValid(Request request) {
        if (request.getAction() != ActionType.NEW &&
            request.getAction() != ActionType.DELETE) {
            addResponse(request,
                        OrderStatus.REJECTED,
                        101,
                        "请求类型错误");
            return false;
        }
        if (request.getAction() == ActionType.NEW) {
            return isNewRequestValid(request);
        }
        return true;
    }

    private boolean isNewRequestValid(Request request) {
        var orderId = request.getOrderId();
        if (orderId != null && orderIds.contains(orderId)) {
            addResponse(request,
                        OrderStatus.REJECTED,
                        102,
                        "重复报单");
            return false;
        }
        if (request.getDirection() != Direction.BUY &&
            request.getDirection() != Direction.SELL) {
            addResponse(request,
                        OrderStatus.REJECTED,
                        103,
                        "交易方向错误");
            return false;
        }
        orderIds.add(orderId);
        return true;
    }

    private void invokeHandler(MarketMaker m, Request r) {
        m.getTradeUpdates().forEach(trade -> {
            try {
                if (trade.getOrderId() == null) {
                    return;
                }
                handler.onTrade(trade);
            } catch (Throwable th) {
                handler.onError(new GatewayRuntimeException(-1, th.getMessage(), th));
            }
        });
        getResponseUpdates(m).forEach(response -> {
            try {
                if (response.getOrderId() == null) {
                    return;
                }
                handler.onResponse(response);
            } catch (Throwable th) {
                handler.onError(new GatewayRuntimeException(-1, th.getMessage(), th));
            }
        });
    }

    private Collection<Response> getResponseUpdates(MarketMaker m) {
        var r = new LinkedList<Response>();
        r.addAll(m.getResponseUpdates());
        r.addAll(responses);
        responses.clear();
        return r;
    }

    @Override
    public TraderGatewayInfo getGatewayInfo() {
        info.setTradingDay(LocalDate.now());
        info.setActionDay(LocalDate.now());
        info.setUpdateTimestamp(ZonedDateTime.now());
        return info;
    }

    @Override
    public void setHandler(ITraderGatewayHandler handler) {
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
    public void insert(Request request) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(request.getInstrumentId());
        try {
            es.publish(Request.class, request);
        } catch (NoSubscribedClassException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

}
