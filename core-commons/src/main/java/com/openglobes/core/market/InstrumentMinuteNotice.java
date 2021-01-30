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
public class InstrumentMinuteNotice implements Serializable {

    private final ZonedDateTime alignTime;
    private final String        instrumentId;
    private final Long          instrumentMinuteNoticeId;
    private final Integer       minuteOfTradingDay;
    private final Integer       minutes;
    private final ZonedDateTime timestamp;
    private final LocalDate     tradingDay;

    public InstrumentMinuteNotice(Long instrumentMinuteNoticeId,
                                  String instrumentId,
                                  Integer minutes,
                                  Integer minuteOfTradingDay,
                                  ZonedDateTime alignTime,
                                  ZonedDateTime timestamp,
                                  LocalDate tradingDay) {
        this.alignTime                = alignTime;
        this.instrumentId             = instrumentId;
        this.instrumentMinuteNoticeId = instrumentMinuteNoticeId;
        this.minuteOfTradingDay       = minuteOfTradingDay;
        this.minutes                  = minutes;
        this.timestamp                = timestamp;
        this.tradingDay               = tradingDay;
    }

    public ZonedDateTime getAlignTime() {
        return alignTime;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public Long getInstrumentMinuteNoticeId() {
        return instrumentMinuteNoticeId;
    }

    public Integer getMinuteOfTradingDay() {
        return minuteOfTradingDay;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }
}
