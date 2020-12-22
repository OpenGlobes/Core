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

import com.openglobes.core.exceptions.EngineException;
import com.openglobes.core.exceptions.GatewayException;
import java.util.Properties;

/**
 *
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

    TraderGatewayContext getTraderGatewayContext() {
        return ctx;
    }

    ITraderGatewayHandler getHandler() {
        return ctx.getHandler();
    }

    void setHandler(ITraderGatewayHandler handler) {
        ctx.setHandler(handler);
    }

    boolean isEnabled() {
        return ctx.isEnabled();
    }

    Integer getTraderId() {
        return ctx.getTraderId();
    }

    void start(Properties globalProperties) throws GatewayException,
                                                   EngineException {
        var properties = new Properties();
        if (ctx.getStartProperties() != null) {
            properties.putAll(ctx.getStartProperties());
        }
        if (globalProperties != null) {
            properties.putAll(globalProperties);
        }
        check0();
        ctx.getTrader().start(properties, ctx.getHandler());
    }

    void stop() throws GatewayException,
                       EngineException {
        check0();
        ctx.getTrader().stop();
    }

    void insert(Request request, int requestId) throws GatewayException,
                                                       EngineException {
        check0();
        ctx.getTrader().insert(request, requestId);
    }

    TraderGatewayInfo getGatewayInfo() throws EngineException {
        check0();
        return ctx.getTrader().getGatewayInfo();
    }

    private void check0() throws EngineException {
        if (ctx.getTrader() == null) {
            throw new EngineException(
                    Exceptions.TRADER_GATEWAY_NULL.code(),
                    Exceptions.TRADER_GATEWAY_NULL.message() + "(Trader ID:" + ctx.getTraderId() + ")");
        }
    }
}
