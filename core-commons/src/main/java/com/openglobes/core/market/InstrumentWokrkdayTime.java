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

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentWokrkdayTime {

    private String instrumentId;
    private String instrumentWorkdayTimeId;
    private Long workdayTimeId;

    public InstrumentWokrkdayTime() {
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getInstrumentWorkdayTimeId() {
        return instrumentWorkdayTimeId;
    }

    public void setInstrumentWorkdayTimeId(String instrumentWorkdayTimeId) {
        this.instrumentWorkdayTimeId = instrumentWorkdayTimeId;
    }

    public Long getWorkdayTimeId() {
        return workdayTimeId;
    }

    public void setWorkdayTimeId(Long workdayTimeId) {
        this.workdayTimeId = workdayTimeId;
    }

}
