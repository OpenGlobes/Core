/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.data;

import com.openglobes.core.market.HolidayTime;
import com.openglobes.core.market.HolidayTimeSet;
import com.openglobes.core.market.InstrumentTime;
import com.openglobes.core.market.WorkdayTime;
import com.openglobes.core.market.WorkdayTimeSet;
import java.util.Collection;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketData extends AutoCloseable{

    Collection<WorkdayTime> getWorkdayTimes() throws MarketDataSourceException;

    Collection<WorkdayTime> getWorkdayTimesByTimeSetId(Long workdayTimeSetId) throws MarketDataSourceException;

    WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException;

    Collection<HolidayTime> getHolidayTimes() throws MarketDataSourceException;

    Collection<HolidayTime> getHolidayTimesByTimeSetId(Long holidayTimeSetId) throws MarketDataSourceException;

    HolidayTime getHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException;
    
    Collection<InstrumentTime> getInstrumentTimes() throws MarketDataSourceException;
    
    InstrumentTime getInstrumentTimeByInstrumentId(String instrumentId) throws MarketDataSourceException;
    
    Collection<HolidayTimeSet> getHolidayTimeSets() throws MarketDataSourceException;
    
    HolidayTimeSet getHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException;
    
    Collection<WorkdayTimeSet> getWorkdayTimeSets() throws MarketDataSourceException;
    
    WorkdayTimeSet getWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException;
}
