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

import java.time.ZonedDateTime;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentMinuteNotice {

    private ZonedDateTime alignTime;
    private String instrumentId;
    private Long instrumentMinuteNoticeId;
    private Integer minuteOfTradingDay;
    private Integer minutes;
    private ZonedDateTime timestamp;

    public InstrumentMinuteNotice() {
    }

    public ZonedDateTime getAlignTime() {
        return alignTime;
    }

    public void setAlignTime(ZonedDateTime alignTime) {
        this.alignTime = alignTime;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getInstrumentMinuteNoticeId() {
        return instrumentMinuteNoticeId;
    }

    public void setInstrumentMinuteNoticeId(Long instrumentMinuteNoticeId) {
        this.instrumentMinuteNoticeId = instrumentMinuteNoticeId;
    }

    public Integer getMinuteOfTradingDay() {
        return minuteOfTradingDay;
    }

    public void setMinuteOfTradingDay(Integer minuteOfTradingDay) {
        this.minuteOfTradingDay = minuteOfTradingDay;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
