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

import com.openglobes.core.data.IMarketData;
import com.openglobes.core.data.MarketDataSourceException;
import com.openglobes.core.exceptions.Exceptions;
import com.openglobes.core.utils.Utils;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class TimeKeeper {

    public static TimeKeeper create(Long holidayTimeSetId,
                                    Long workdayTimeSetId,
                                    IMarketData connection) throws MarketDataSourceException {
        assertTrue(connection != null, Exceptions.DATASOURCE_NULL);
        var wd = connection.getWorkdayTimesByTimeSetId(workdayTimeSetId);
        assertTrue(!wd.isEmpty(), Exceptions.NO_WORKDAY_TIME);
        var hd = connection.getHolidayTimesByTimeSetId(holidayTimeSetId);
        assertTrue(!hd.isEmpty(), Exceptions.NO_HOLIDAY_TIME);
        return new TimeKeeper(workdayTimeSetId,
                              wd,
                              holidayTimeSetId,
                              hd);
    }

    private static void assertTrue(Boolean x, Exceptions exp) throws MarketDataSourceException {
        if (!x) {
            throw new MarketDataSourceException(exp.code(), exp.message());
        }
    }

    private final List<HolidayTime> holiday;
    private final List<WorkdayTime> workday;
    final Long holidayTimeSetId;
    final Long workdayTimeSetId;

    private TimeKeeper(Long workdayTimeSetId,
                       Collection<WorkdayTime> workday,
                       Long holidayTimeSetId,
                       Collection<HolidayTime> holiday) {
        this.holidayTimeSetId = holidayTimeSetId;
        this.workdayTimeSetId = workdayTimeSetId;
        this.holiday = new LinkedList<>(holiday);
        this.workday = new LinkedList<>(workday);
        setup();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        else if (obj == this || obj.hashCode() == hashCode()) {
            return true;
        }
        else if (obj instanceof TimeKeeper) {
            var x = (TimeKeeper) obj;
            return Objects.equals(x.workdayTimeSetId, workdayTimeSetId)
                   && Objects.equals(x.holidayTimeSetId, holidayTimeSetId);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean isBegin(ZonedDateTime now) {
        return isWorkBegin(now) && (!inHoliday(now));
    }

    public boolean isEnd(ZonedDateTime now) {
        return isWorkEnd(now) && (!inHoliday(now));
    }

    public boolean isWorking(ZonedDateTime now) {
        return inWorkTime(now) && !inHoliday(now);
    }

    private void setup() {
        workday.sort((WorkdayTime o1, WorkdayTime o2) -> {
            var r = o1.getDayRank().compareTo(o2.getDayRank());
            return r != 0 ? r : (o1.getFromTime().compareTo(o2.getFromTime()));
        });
        holiday.sort((HolidayTime o1, HolidayTime o2) -> {
            var r = o1.getDayRank().compareTo(o2.getDayRank());
            return r != 0 ? r : (o1.getFromTime().compareTo(o2.getFromTime()));
        });
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

    protected boolean isWorkBegin(ZonedDateTime now) {
        var n = Utils.getAlignByMinute().toLocalTime();
        return n.compareTo(workday.get(0).getFromTime()) == 0;
    }

    protected boolean isWorkEnd(ZonedDateTime now) {
        var n = Utils.getAlignByMinute().toLocalTime();
        return n.compareTo(workday.get(workday.size() - 1).getToTime()) == 0;
    }

}
