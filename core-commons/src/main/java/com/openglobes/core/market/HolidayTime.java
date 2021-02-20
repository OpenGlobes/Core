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
import java.time.ZonedDateTime;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class HolidayTime implements Serializable {

    private Integer dayRank;
    private ZonedDateTime fromTime;
    private Long holidayTimeId;
    private ZonedDateTime timestamp;
    private ZonedDateTime toTime;
    private Integer type;

    public HolidayTime() {
    }

    public ZonedDateTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(ZonedDateTime fromTime) {
        this.fromTime = fromTime;
    }

    public ZonedDateTime getToTime() {
        return toTime;
    }

    public void setToTime(ZonedDateTime toTime) {
        this.toTime = toTime;
    }

    public Integer getDayRank() {
        return dayRank;
    }

    public void setDayRank(Integer dayRank) {
        this.dayRank = dayRank;
    }

    public Long getHolidayTimeId() {
        return holidayTimeId;
    }

    public void setHolidayTimeId(Long holidayTimeId) {
        this.holidayTimeId = holidayTimeId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
