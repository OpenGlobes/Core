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

import com.openglobes.core.market.Stick;
import com.openglobes.core.market.Tick;
import com.openglobes.core.utils.Utils;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class StickBuilder implements IStickBuilder {

    private final Map<Integer, IStickContext> days;
    private final IStickEngine eg;
    private final Map<Integer, IStickContext> mins;
    private Integer daysOfYr;
    private ZonedDateTime endOfDay;
    private String iid;
    private Integer minOfDay;
    private ZonedDateTime preAlign;

    public StickBuilder(IStickEngine engine) {
        mins = new ConcurrentHashMap<>(512);
        days = new ConcurrentHashMap<>(8);
        eg = engine;
        preAlign = Utils.getRoundedTimeByMinute();
    }

    @Override
    public void addDays(Integer days) throws IllegalDaysException {
        if (days <= 0) {
            throw new IllegalDaysException(days.toString());
        }
        this.days.computeIfAbsent(days, m -> {
            return new StickContext(days, true);
        });
    }

    @Override
    public void addMinutes(Integer minutes) throws IllegalMinutesException {
        if (minutes <= 0) {
            throw new IllegalMinutesException(minutes.toString());
        }
        mins.computeIfAbsent(minutes, m -> {
            return new StickContext(minutes, false);
        });
    }

    @Override
    public Collection<Stick> build(Integer minutesOfDay,
                                   Integer daysOfyear,
                                   ZonedDateTime alignTime) throws IllegalMinutesException,
                                                                   IllegalDaysException {
        synchronized (this) {
            try {
                var r = new HashSet<Stick>(16);
                if (minutesOfDay != null) {
                    if (minutesOfDay <= 0) {
                        throw new IllegalMinutesException(minutesOfDay.toString());
                    } else {
                        r.addAll(buildMinutesWithRest(minutesOfDay,
                                                      alignTime));
                    }
                }
                if (daysOfyear != null) {
                    if (daysOfyear <= 0) {
                        throw new IllegalDaysException(daysOfyear.toString());
                    } else {
                        r.addAll(buildDaysWithRest(daysOfyear,
                                                   alignTime));
                    }
                }
                return r;
            } finally {
                /*
                 * Save pre-align time for try build.
                 */
                preAlign = alignTime;
            }
        }
    }

    @Override
    public Collection<Integer> getDays() {
        return days.keySet();
    }

    @Override
    public String getInstrumentId() {
        return iid;
    }

    @Override
    public Collection<Integer> getMinutes() {
        return mins.keySet();
    }

    @Override
    public void removeDays(Integer d) {
        days.remove(d);
    }

    @Override
    public void removeMinutes(Integer minutes) {
        mins.remove(minutes);
    }

    @Override
    public Collection<Stick> tryBuild(ZonedDateTime eodTime) throws IllegalEodException {
        synchronized (this) {
            try {
                if (preAlign.isEqual(eodTime)) {
                    /*
                     * Some sticks published, just publish the rest.
                     */
                    return buildRest();
                } else if (preAlign.isAfter(eodTime)) {
                    /*
                     * Wield thing happens.
                     */
                    throw new IllegalEodException(eodTime.toString());
                } else {
                    return new HashSet<>(1);
                }
            } finally {
                /*
                 * Save EOD time for build.
                 */
                endOfDay = eodTime;
            }
        }
    }

    @Override
    public void update(Tick tick) throws IllegalInstrumentIdException {
        synchronized (this) {
            if (iid == null) {
                iid = tick.getInstrumentId();
            }
            if (!iid.equals(tick.getInstrumentId())) {
                throw new IllegalInstrumentIdException("Expect " + iid + " but " + tick.getInstrumentId());
            }
            mins.values().forEach(v -> {
                v.update(tick);
            });
        }
    }

    private Collection<Stick> buildDaysWithRest(Integer daysOfYear,
                                                ZonedDateTime alignTime) {
        try {
            var r = new HashSet<Stick>(16);
            r.addAll(getSticks(daysOfYear,
                               this.days,
                               true,
                               true));
            if (endOfDay != null && endOfDay.isEqual(alignTime)) {
                /*
                 * Time to build rest sticks at the end of a day.
                 */
                r.addAll(getSticks(daysOfYear,
                                   this.days,
                                   true,
                                   false));
            }
            return r;
        } finally {
            /*
             * Save days-of-year for building rest.
             */
            daysOfYr = daysOfYear;
        }
    }

    private Collection<Stick> buildMinutesWithRest(Integer minutesOfDay,
                                                   ZonedDateTime alignTime) {
        try {
            var r = new HashSet<Stick>(16);
            r.addAll(getSticks(minutesOfDay,
                               mins,
                               false,
                               true));
            if (endOfDay != null && endOfDay.isEqual(alignTime)) {
                /*
                 * Time to build rest sticks at the end of a day.
                 */
                r.addAll(getSticks(minutesOfDay,
                                   mins,
                                   false,
                                   false));
            }

            return r;
        } finally {
            /*
             * Save minutes-of-day for building rest.
             */
            minOfDay = minutesOfDay;
        }
    }

    private Collection<Stick> buildRest() {
        var r = new HashSet<Stick>(16);
        r.addAll(getSticks(minOfDay,
                           mins,
                           false,
                           false));
        r.addAll(getSticks(daysOfYr,
                           this.days,
                           true,
                           false));
        return r;
    }

    private Collection<Stick> getSticks(Integer x,
                                        Map<Integer, IStickContext> sticks,
                                        boolean isDay,
                                        boolean canDiv) {
        var r = new HashSet<Stick>(12);
        for (var e : sticks.entrySet()) {
            if (canDiv == (x % e.getKey() == 0)) {
                var s = e.getValue().nextStick(eg.nextStickId());
                if (!isDay) {
                    s.setMinutes(e.getKey());
                } else {
                    s.setDays(e.getKey());
                }
                r.add(s);
            }
        }
        return r;
    }
}
