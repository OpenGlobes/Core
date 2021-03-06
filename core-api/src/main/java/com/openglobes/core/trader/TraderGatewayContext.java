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

import java.time.ZonedDateTime;

/**
 * Trader gateway information.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class TraderGatewayContext {

    private final ZonedDateTime registerTimestamp;
    private Boolean enabled;
    private ITraderEngine engine;
    private ITraderGatewayHandler handler;
    private String note;
    private ITraderGateway trader;
    private Integer traderId;
    private ZonedDateTime updateTimestamp;

    public TraderGatewayContext() {
        registerTimestamp = ZonedDateTime.now();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
        updateTimestamp();
    }

    public ITraderEngine getEngine() {
        return engine;
    }

    public void setEngine(ITraderEngine engine) {
        this.engine = engine;
        updateTimestamp();
    }

    public ITraderGatewayHandler getHandler() {
        return handler;
    }

    public void setHandler(ITraderGatewayHandler handler) {
        this.handler = handler;
        updateTimestamp();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        updateTimestamp();
    }

    public ZonedDateTime getRegisterTimestamp() {
        return registerTimestamp;
    }

    public ITraderGateway getTrader() {
        return trader;
    }

    public void setTrader(ITraderGateway trader) {
        this.trader = trader;
        updateTimestamp();
    }

    public Integer getTraderId() {
        return traderId;
    }

    public void setTraderId(Integer traderId) {
        this.traderId = traderId;
        updateTimestamp();
    }

    public ZonedDateTime getUpdateTimestamp() {
        return updateTimestamp;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    private void updateTimestamp() {
        updateTimestamp = ZonedDateTime.now();
    }

}
