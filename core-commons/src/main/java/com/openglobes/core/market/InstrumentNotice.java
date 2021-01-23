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

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentNotice {

    private final ZonedDateTime alignTime;
    private final String        instrumentId;
    private final Long          instrumentNoticeId;
    private final ZonedDateTime timestamp;
    private final LocalDate     tradingDay;
    private final Integer       type;

    public InstrumentNotice(Long instrumentNoticeId,
                            String instrumentId,
                            Integer type,
                            ZonedDateTime alignTime,
                            ZonedDateTime timestamp,
                            LocalDate tradingDay) {
        this.instrumentId       = instrumentId;
        this.instrumentNoticeId = instrumentNoticeId;
        this.alignTime          = alignTime;
        this.timestamp          = timestamp;
        this.tradingDay         = tradingDay;
        this.type               = type;
    }

    public ZonedDateTime getAlignTime() {
        return alignTime;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public Long getInstrumentNoticeId() {
        return instrumentNoticeId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }

    public Integer getType() {
        return type;
    }
}
