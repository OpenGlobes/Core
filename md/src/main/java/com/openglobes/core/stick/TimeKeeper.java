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
package com.openglobes.core.stick;

import com.openglobes.core.data.DataQueryException;
import com.openglobes.core.data.IMarketDataConnection;
import com.openglobes.core.market.HolidayTime;
import com.openglobes.core.market.WorkdayTime;
import com.openglobes.core.utils.Utils;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class TimeKeeper implements ITimeKeeper {

    final Long holidayTimeSetId;
    final Long workdayTimeSetId;
    private final List<HolidayTime> holidays;
    private final List<WorkdayTime> workdays;

    private TimeKeeper(Long workdayTimeSetId,
                       Collection<WorkdayTime> workday,
                       Long holidayTimeSetId,
                       Collection<HolidayTime> holidays) {
        this.holidayTimeSetId = holidayTimeSetId;
        this.workdayTimeSetId = workdayTimeSetId;
        this.holidays = new LinkedList<>(holidays);
        this.workdays = new LinkedList<>(workday);
        setup();
    }

    private void setup() {
        workdays.sort((WorkdayTime o1, WorkdayTime o2) -> {
            var r = o1.getDayRank().compareTo(o2.getDayRank());
            return r != 0 ? r : (o1.getFromTime().compareTo(o2.getFromTime()));
        });
        holidays.sort((HolidayTime o1, HolidayTime o2) -> {
            var r = o1.getDayRank().compareTo(o2.getDayRank());
            return r != 0 ? r : (o1.getFromTime().compareTo(o2.getFromTime()));
        });
    }

    public static TimeKeeper create(Long holidayTimeSetId,
                                    Long workdayTimeSetId,
                                    IMarketDataConnection connection) throws NoWorkdayException,
                                                                             NoHolidayException,
                                                                             DataQueryException {
        Objects.requireNonNull(connection);
        var wd = connection.getWorkdayTimesByTimeSetId(workdayTimeSetId);
        Objects.requireNonNull(wd);
        if (wd.isEmpty()) {
            throw new NoWorkdayException("No workday.");
        }
        var hd = connection.getHolidayTimesByTimeSetId(holidayTimeSetId);
        Objects.requireNonNull(hd);
        if (hd.isEmpty()) {
            throw new NoHolidayException("No holiday.");
        }
        return new TimeKeeper(workdayTimeSetId,
                              wd,
                              holidayTimeSetId,
                              hd);
    }

    @Override
    public boolean isBegin(ZonedDateTime now) {
        return isWorkBegin(now) && (!inHoliday(now));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this || obj.hashCode() == hashCode()) {
            return true;
        } else if (obj instanceof TimeKeeper) {
            var x = (TimeKeeper) obj;
            return Objects.equals(x.workdayTimeSetId, workdayTimeSetId)
                   && Objects.equals(x.holidayTimeSetId, holidayTimeSetId);
        } else {
            return false;
        }
    }

    @Override
    public boolean isTrading(ZonedDateTime now) {
        return inWorkTime(now) && (!inHoliday(now));
    }

    @Override
    public boolean isRegularTrading(ZonedDateTime now) {
        return inWorkTime(now);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean isEnd(ZonedDateTime now) {
        return isWorkEnd(now) && (!inHoliday(now));
    }

    protected boolean inWorkTime(ZonedDateTime now) {
        return workdays.stream().anyMatch(wt -> Utils.inRange(now.toLocalTime(),
                                                              wt.getFromTime(),
                                                              wt.getToTime())
                                                && now.getDayOfWeek() == wt.getDayOfWeek());
    }

    protected boolean inHoliday(ZonedDateTime now) {
        return holidays.stream().anyMatch(hd -> Utils.inRange(now,
                                                              hd.getFromTime(),
                                                              hd.getToTime()));
    }

    protected boolean isWorkBegin(ZonedDateTime now) {
        var n = Utils.getRoundedTimeByMinute().toLocalTime();
        return n.compareTo(workdays.get(0).getFromTime()) == 0;
    }

    protected boolean isWorkEnd(ZonedDateTime now) {
        return now.toLocalTime().compareTo(workdays.get(workdays.size() - 1).getToTime()) == 0;
    }

}
