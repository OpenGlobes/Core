/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.market;

import com.openglobes.core.data.IMarketData;
import com.openglobes.core.data.MarketDataSourceException;
import com.openglobes.core.utils.Utils;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentTimeKeeper {

    public static InstrumentTimeKeeper create(String instrumentId,
                                              IMarketData connection) throws MarketDataSourceException {
        var ts = connection.getInstrumentTimeByInstrumentId(instrumentId);
        var wd = connection.getWorkdayTimesByTimeSetId(ts.getWorkdayTimeSetId());
        var hd = connection.getHolidayTimesByTimeSetId(ts.getHolidayTimeSetId());
        return new InstrumentTimeKeeper(instrumentId,
                                        wd,
                                        hd);
    }

    private final List<HolidayTime> holiday;
    private final String instrumentId;
    private final List<WorkdayTime> workday;

    public InstrumentTimeKeeper(String instrumentId,
                                Collection<WorkdayTime> workday,
                                Collection<HolidayTime> holiday) {
        this.instrumentId = instrumentId;
        this.holiday = new LinkedList<>(holiday);
        this.workday = new LinkedList<>(workday);
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public boolean isWorking(ZonedDateTime now) {
        return inWorkTime(now) && !inHoliday(now);
    }

    protected boolean inHoliday(ZonedDateTime now) {
        return holiday.stream().anyMatch(hd -> Utils.inRange(now.toLocalTime(),
                                                             hd.getFromTime(),
                                                             hd.getToTime()));
    }

    protected boolean inWorkTime(ZonedDateTime now) {
        return workday.stream().anyMatch(wt -> Utils.inRange(now.toLocalTime(),
                                                             wt.getFromTime(),
                                                             wt.getToTime()));
    }

}
