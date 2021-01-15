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

import com.openglobes.core.dba.IPooledConnection;
import com.openglobes.core.market.HolidayTime;
import com.openglobes.core.market.HolidayTimeSet;
import com.openglobes.core.market.InstrumentStickSetting;
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
public interface IMarketDataConnection extends AutoCloseable,
                                               IPooledConnection {

    void addHolidayTime(HolidayTime time) throws MarketDataSourceException;

    void addHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException;

    void addInstrumentStickSetting(InstrumentStickSetting setting) throws MarketDataSourceException;

    void addInstrumentTime(InstrumentTime time) throws MarketDataSourceException;

    void addWorkdayTime(WorkdayTime time) throws MarketDataSourceException;

    void addWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException;

    @Override
    void close();

    HolidayTime getHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException;

    HolidayTimeSet getHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException;

    Collection<HolidayTimeSet> getHolidayTimeSets() throws MarketDataSourceException;

    Collection<HolidayTime> getHolidayTimes() throws MarketDataSourceException;

    Collection<HolidayTime> getHolidayTimesByTimeSetId(Long holidayTimeSetId) throws MarketDataSourceException;

    InstrumentStickSetting getInstrumentStickSettingById(Long instrumentStickSettingId) throws MarketDataSourceException;

    Collection<InstrumentStickSetting> getInstrumentStickSettingByInstrumentId(String instrumentId) throws MarketDataSourceException;

    Collection<InstrumentStickSetting> getInstrumentStickSettings() throws MarketDataSourceException;

    InstrumentTime getInstrumentTimeByInstrumentId(String instrumentId) throws MarketDataSourceException;

    Collection<InstrumentTime> getInstrumentTimes() throws MarketDataSourceException;

    TradingDay getTradingDay() throws MarketDataSourceException;

    WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException;

    WorkdayTimeSet getWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException;

    Collection<WorkdayTimeSet> getWorkdayTimeSets() throws MarketDataSourceException;

    Collection<WorkdayTime> getWorkdayTimes() throws MarketDataSourceException;

    Collection<WorkdayTime> getWorkdayTimesByTimeSetId(Long workdayTimeSetId) throws MarketDataSourceException;

    void removeHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException;

    void removeHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException;

    void removeInstrumentStickSettingById(Long instrumentStickSettingId) throws MarketDataSourceException;

    void removeInstrumentTimeById(Long instrumentTimeId) throws MarketDataSourceException;

    void removeWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException;

    void removeWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException;

    void updateHolidayTime(HolidayTime time) throws MarketDataSourceException;

    void updateHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException;

    void updateInstrumentStickSetting(InstrumentStickSetting setting) throws MarketDataSourceException;

    void updateInstrumentTime(InstrumentTime time) throws MarketDataSourceException;

    void updateTradingDay(TradingDay tradingDay) throws MarketDataSourceException;

    void updateWorkdayTime(WorkdayTime time) throws MarketDataSourceException;

    void updateWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException;

}
