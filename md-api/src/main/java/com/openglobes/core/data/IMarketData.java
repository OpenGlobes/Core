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
package com.openglobes.core.data;

import com.openglobes.core.market.HolidayTime;
import com.openglobes.core.market.HolidayTimeSet;
import com.openglobes.core.market.InstrumentTime;
import com.openglobes.core.market.WorkdayTime;
import com.openglobes.core.market.WorkdayTimeSet;
import com.openglobes.core.trader.TradingDay;
import java.util.Collection;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketData extends AutoCloseable {

    Collection<WorkdayTime> getWorkdayTimes() throws MarketDataSourceException;

    Collection<WorkdayTime> getWorkdayTimesByTimeSetId(Long workdayTimeSetId) throws MarketDataSourceException;

    WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException;

    void addWorkdayTime(WorkdayTime time) throws MarketDataSourceException;

    void updateWorkdayTime(WorkdayTime time) throws MarketDataSourceException;

    void removeWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException;

    Collection<HolidayTime> getHolidayTimes() throws MarketDataSourceException;

    Collection<HolidayTime> getHolidayTimesByTimeSetId(Long holidayTimeSetId) throws MarketDataSourceException;

    HolidayTime getHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException;

    void addHolidayTime(HolidayTime time) throws MarketDataSourceException;

    void updateHolidayTime(HolidayTime time) throws MarketDataSourceException;

    void removeHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException;

    Collection<InstrumentTime> getInstrumentTimes() throws MarketDataSourceException;

    InstrumentTime getInstrumentTimeByInstrumentId(String instrumentId) throws MarketDataSourceException;

    void addInstrumentTime(InstrumentTime time) throws MarketDataSourceException;

    void updateInstrumentTime(InstrumentTime time) throws MarketDataSourceException;

    void removeInstrumentTimeById(Long instrumntTimeId) throws MarketDataSourceException;

    Collection<HolidayTimeSet> getHolidayTimeSets() throws MarketDataSourceException;

    HolidayTimeSet getHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException;

    void addHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException;

    void updateHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException;

    void removeHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException;

    Collection<WorkdayTimeSet> getWorkdayTimeSets() throws MarketDataSourceException;

    WorkdayTimeSet getWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException;

    void addWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException;

    void updateWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException;

    void removeWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException;

    TradingDay getTradingDay() throws MarketDataSourceException;

    void setTradingDay() throws MarketDataSourceException;
}
