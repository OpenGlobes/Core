/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.data;

import com.openglobes.core.market.HolidayTime;
import com.openglobes.core.market.InstrumentHolidayTime;
import com.openglobes.core.market.InstrumentWokrkdayTime;
import com.openglobes.core.market.WorkdayTime;
import java.util.Collection;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketData {
    Collection<InstrumentWokrkdayTime> getInstrumentWokrkdayTimes() throws MarketDataSourceException;
    
    Collection<InstrumentWokrkdayTime> getInstrumentWokrkdayTimesByInstrumentId(String instrumentId) throws MarketDataSourceException;

    Collection<WorkdayTime> getWorkdayTimes() throws MarketDataSourceException;
    
    WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException;
    
    Collection<WorkdayTime> getWorkdayTimesByInstrumentId(String instrumentId) throws MarketDataSourceException;
    
    Collection<InstrumentHolidayTime> getInstrumentHolidayTimes() throws MarketDataSourceException;
    
    Collection<InstrumentHolidayTime> getInstrumentHolidayTimesByInstrumentId(String instrumentId) throws  MarketDataSourceException;
    
    Collection<HolidayTime> getHolidayTimes() throws MarketDataSourceException;
    
    Collection<HolidayTime> getHolidayTimesByInstrumentId(String instrumentId) throws MarketDataSourceException;
    
    HolidayTime getHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException;
}
