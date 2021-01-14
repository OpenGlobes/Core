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

import com.openglobes.core.dba.AbstractPooledConnection;
import com.openglobes.core.dba.DbaException;
import com.openglobes.core.dba.IPooledDataSource;
import com.openglobes.core.market.HolidayTime;
import com.openglobes.core.market.HolidayTimeSet;
import com.openglobes.core.market.InstrumentStickSetting;
import com.openglobes.core.market.InstrumentTime;
import com.openglobes.core.market.WorkdayTime;
import com.openglobes.core.market.WorkdayTimeSet;
import com.openglobes.core.trader.TradingDay;
import java.sql.Connection;
import java.util.Collection;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class MarketDataConnection extends AbstractPooledConnection 
        implements IMarketDataConnection {

    public MarketDataConnection(Connection connection, 
                                IPooledDataSource source) {
        super(connection, source);
    }
    
    @Override
    public void addHolidayTime(HolidayTime time) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addInstrumentStickSetting(InstrumentStickSetting setting) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addInstrumentTime(InstrumentTime time) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addWorkdayTime(WorkdayTime time) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commit() throws DbaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HolidayTime getHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HolidayTimeSet getHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<HolidayTimeSet> getHolidayTimeSets() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<HolidayTime> getHolidayTimes() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<HolidayTime> getHolidayTimesByTimeSetId(Long holidayTimeSetId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InstrumentStickSetting getInstrumentStickSettingById(Long instrumentStickSettingId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<InstrumentStickSetting> getInstrumentStickSettingByInstrumentId(String instrumentId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<InstrumentStickSetting> getInstrumentStickSettings() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InstrumentTime getInstrumentTimeByInstrumentId(String instrumentId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<InstrumentTime> getInstrumentTimes() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TradingDay getTradingDay() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WorkdayTimeSet getWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<WorkdayTimeSet> getWorkdayTimeSets() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<WorkdayTime> getWorkdayTimes() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<WorkdayTime> getWorkdayTimesByTimeSetId(Long workdayTimeSetId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeInstrumentStickSettingById(Long instrumentStickSettingId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeInstrumentTimeById(Long instrumntTimeId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rollback() throws DbaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTradingDay() throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void transaction() throws DbaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateHolidayTime(HolidayTime time) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateInstrumentStickSetting(InstrumentStickSetting setting) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateInstrumentTime(InstrumentTime time) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateWorkdayTime(WorkdayTime time) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
