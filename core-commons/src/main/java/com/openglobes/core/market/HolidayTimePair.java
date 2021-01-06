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
public class HolidayTimePair {

    private Long holidayTimeId;
    private Long holidayTimePairId;
    private Long holidayTimeSetId;
    private ZonedDateTime timestamp;

    public HolidayTimePair() {
    }

    public Long getHolidayTimeId() {
        return holidayTimeId;
    }

    public void setHolidayTimeId(Long holidayTimeId) {
        this.holidayTimeId = holidayTimeId;
    }

    public Long getHolidayTimePairId() {
        return holidayTimePairId;
    }

    public void setHolidayTimePairId(Long holidayTimePairId) {
        this.holidayTimePairId = holidayTimePairId;
    }

    public Long getHolidayTimeSetId() {
        return holidayTimeSetId;
    }

    public void setHolidayTimeSetId(Long holidayTimeSetId) {
        this.holidayTimeSetId = holidayTimeSetId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
