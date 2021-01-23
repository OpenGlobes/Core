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
package com.openglobes.core.trader;

import com.openglobes.core.GatewayException;

import java.util.Properties;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class TraderContext extends IdTranslator {

    private final TraderGatewayContext ctx;

    TraderContext(TraderGatewayContext ctx) {
        this.ctx = ctx;
    }

    ITraderEngine getEngine() {
        return ctx.getEngine();
    }

    TraderGatewayInfo getGatewayInfo() {
        return ctx.getTrader().getGatewayInfo();
    }

    ITraderGatewayHandler getHandler() {
        return ctx.getHandler();
    }

    void setHandler(ITraderGatewayHandler handler) {
        ctx.setHandler(handler);
    }

    TraderGatewayContext getTraderGatewayContext() {
        return ctx;
    }

    Integer getTraderId() {
        return ctx.getTraderId();
    }

    void insert(Request request, int requestId) throws GatewayException {
        ctx.getTrader().insert(request, requestId);
    }

    boolean isEnabled() {
        return ctx.isEnabled();
    }

    void start(Properties globalProperties) throws GatewayException {
        var properties = new Properties();
        if (ctx.getStartProperties() != null) {
            properties.putAll(ctx.getStartProperties());
        }
        if (globalProperties != null) {
            properties.putAll(globalProperties);
        }
        ctx.getTrader().start(properties, ctx.getHandler());
    }

    void stop() throws GatewayException {
        ctx.getTrader().stop();
    }

}
