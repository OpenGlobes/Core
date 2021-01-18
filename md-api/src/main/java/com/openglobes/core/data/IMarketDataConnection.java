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

    void addHolidayTime(HolidayTime time) throws DataInsertionException;

    void addHolidayTimeSet(HolidayTimeSet set) throws DataInsertionException;

    void addInstrumentStickSetting(InstrumentStickSetting setting) throws DataInsertionException;

    void addInstrumentTime(InstrumentTime time) throws DataInsertionException;

    void addWorkdayTime(WorkdayTime time) throws DataInsertionException;

    void addWorkdayTimeSet(WorkdayTimeSet set) throws DataInsertionException;

    @Override
    void close();

    HolidayTime getHolidayTimeById(Long holidayTimeId) throws DataQueryException;

    HolidayTimeSet getHolidayTimeSetById(Long holidayTimeSetId) throws DataQueryException;

    Collection<HolidayTimeSet> getHolidayTimeSets() throws DataQueryException;

    Collection<HolidayTime> getHolidayTimes() throws DataQueryException;

    Collection<HolidayTime> getHolidayTimesByTimeSetId(Long holidayTimeSetId) throws DataQueryException;

    InstrumentStickSetting getInstrumentStickSettingById(Long instrumentStickSettingId) throws DataQueryException;

    Collection<InstrumentStickSetting> getInstrumentStickSettingByInstrumentId(String instrumentId) throws DataQueryException;

    Collection<InstrumentStickSetting> getInstrumentStickSettings() throws DataQueryException;

    InstrumentTime getInstrumentTimeByInstrumentId(String instrumentId) throws DataQueryException;

    Collection<InstrumentTime> getInstrumentTimes() throws DataQueryException;

    TradingDay getTradingDay() throws DataQueryException;

    WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws DataQueryException;

    WorkdayTimeSet getWorkdayTimeSetById(Long workdayTimeSetId) throws DataQueryException;

    Collection<WorkdayTimeSet> getWorkdayTimeSets() throws DataQueryException;

    Collection<WorkdayTime> getWorkdayTimes() throws DataQueryException;

    Collection<WorkdayTime> getWorkdayTimesByTimeSetId(Long workdayTimeSetId) throws DataQueryException;

    void removeHolidayTimeById(Long holidayTimeId) throws DataRemovalException;

    void removeHolidayTimeSetById(Long holidayTimeSetId) throws DataRemovalException;

    void removeInstrumentStickSettingById(Long instrumentStickSettingId) throws DataRemovalException;

    void removeInstrumentTimeById(Long instrumentTimeId) throws DataRemovalException;

    void removeWorkdayTimeById(Long workdayTimeId) throws DataRemovalException;

    void removeWorkdayTimeSetById(Long workdayTimeSetId) throws DataRemovalException;

    void updateHolidayTime(HolidayTime time) throws DataUpdateException;

    void updateHolidayTimeSet(HolidayTimeSet set) throws DataUpdateException;

    void updateInstrumentStickSetting(InstrumentStickSetting setting) throws DataUpdateException;

    void updateInstrumentTime(InstrumentTime time) throws DataUpdateException;

    void updateTradingDay(TradingDay tradingDay) throws DataUpdateException;

    void updateWorkdayTime(WorkdayTime time) throws DataUpdateException;

    void updateWorkdayTimeSet(WorkdayTimeSet set) throws DataUpdateException;

}
