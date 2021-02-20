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

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentTime implements Serializable {

    private Long holidayTimeSetId;
    private String instrumentId;
    private Long instrumentTimeId;
    private Long workdayTimeSetId;

    public InstrumentTime() {
    }

    public Long getHolidayTimeSetId() {
        return holidayTimeSetId;
    }

    public void setHolidayTimeSetId(Long holidayTimeSetId) {
        this.holidayTimeSetId = holidayTimeSetId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getInstrumentTimeId() {
        return instrumentTimeId;
    }

    public void setInstrumentTimeId(Long instrumentTimeId) {
        this.instrumentTimeId = instrumentTimeId;
    }

    public Long getWorkdayTimeSetId() {
        return workdayTimeSetId;
    }

    public void setWorkdayTimeSetId(Long workdayTimeSetId) {
        this.workdayTimeSetId = workdayTimeSetId;
    }
}
