/*
 * Copyright (C) 2021 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.market;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class MarketNotice implements Serializable {

    private Long marketNoticeId;
    private ZonedDateTime timestamp;
    private LocalDate tradingDay;
    private Integer type;

    public MarketNotice() {
    }

    public Long getMarketNoticeId() {
        return marketNoticeId;
    }

    public void setMarketNoticeId(Long marketNoticeId) {
        this.marketNoticeId = marketNoticeId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }

    public void setTradingDay(LocalDate tradingDay) {
        this.tradingDay = tradingDay;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
