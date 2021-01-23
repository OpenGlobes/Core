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

import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class WorkdayTime {

    private Integer       dayRank;
    private LocalTime     fromTime;
    private ZonedDateTime timestamp;
    private LocalTime     toTime;
    private Integer       type;
    private Long          workdayTimeId;

    public WorkdayTime() {
    }

    public Integer getDayRank() {
        return dayRank;
    }

    public void setDayRank(Integer dayRank) {
        this.dayRank = dayRank;
    }

    public LocalTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(LocalTime fromTime) {
        this.fromTime = fromTime;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalTime getToTime() {
        return toTime;
    }

    public void setToTime(LocalTime toTime) {
        this.toTime = toTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getWorkdayTimeId() {
        return workdayTimeId;
    }

    public void setWorkdayTimeId(Long workdayTimeId) {
        this.workdayTimeId = workdayTimeId;
    }

}
