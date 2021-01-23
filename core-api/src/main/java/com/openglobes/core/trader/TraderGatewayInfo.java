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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Properties;

/**
 * Trader service information updated upon every start.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class TraderGatewayInfo extends Properties {

    private static final long serialVersionUID = 4389209809319836L;

    private LocalDate     actionDay;
    private LocalDate     tradingDay;
    private ZonedDateTime updateTimestamp;

    public TraderGatewayInfo() {
    }

    @Override
    public synchronized Object clone() {
        return super.clone();
    }

    public LocalDate getActionDay() {
        return actionDay;
    }

    public void setActionDay(LocalDate actionDay) {
        this.actionDay = actionDay;
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }

    public void setTradingDay(LocalDate tradingDay) {
        this.tradingDay = tradingDay;
    }

    public ZonedDateTime getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(ZonedDateTime updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
}
