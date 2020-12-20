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
package com.openglobes.core.gateway;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Trader service information updated upon every start.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class TraderGatewayInfo {

    private LocalDate actionDay;
    private LocalDate tradingDay;
    private ZonedDateTime updateTimestamp;

    public TraderGatewayInfo() {
    }

    public LocalDate getActionDay() {
        return actionDay;
    }

    public void setActionDay(LocalDate actionDay) {
        this.actionDay = actionDay;
        updateTimestamp();
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }

    public void setTradingDay(LocalDate tradingDay) {
        this.tradingDay = tradingDay;
        updateTimestamp();
    }

    public ZonedDateTime getUpdateTimestamp() {
        return updateTimestamp;
    }

    private void updateTimestamp() {
        updateTimestamp = ZonedDateTime.now();
    }
}
