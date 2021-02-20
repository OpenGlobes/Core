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
public class WorkdayTimePair implements Serializable {

    private ZonedDateTime timestamp;
    private Long workdayTimeId;
    private Long workdayTimePairId;
    private Long workdayTimeSetId;

    public WorkdayTimePair() {
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getWorkdayTimeId() {
        return workdayTimeId;
    }

    public void setWorkdayTimeId(Long workdayTimeId) {
        this.workdayTimeId = workdayTimeId;
    }

    public Long getWorkdayTimePairId() {
        return workdayTimePairId;
    }

    public void setWorkdayTimePairId(Long workdayTimePairId) {
        this.workdayTimePairId = workdayTimePairId;
    }

    public Long getWorkdayTimeSetId() {
        return workdayTimeSetId;
    }

    public void setWorkdayTimeSetId(Long workdayTimeSetId) {
        this.workdayTimeSetId = workdayTimeSetId;
    }
}
