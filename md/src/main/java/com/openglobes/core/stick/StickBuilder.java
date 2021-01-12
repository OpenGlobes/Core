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
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class StickBuilder implements IStickBuilder {

    private ZonedDateTime align;
    private final Map<Integer, IStickContext> days;
    private Integer daysOfYr;
    private final IStickEngine eg;
    private String iid;
    private Integer minOfDay;
    private final Map<Integer, IStickContext> mins;

    public StickBuilder(IStickEngine engine) {
        mins = new ConcurrentHashMap<>(512);
        days = new ConcurrentHashMap<>(8);
        eg = engine;
        align = Utils.getAlignByMinute();
    }

    @Override
    public void addDays(Integer days) throws StickException {
        if (days <= 0) {
            throw new StickException(ErrorCode.INVALID_STICK_DAYS.code(),
                                     ErrorCode.INVALID_STICK_DAYS.message());
        }
        this.days.computeIfAbsent(days, m -> {
                              return new StickContext(days, true);
                          });
    }

    @Override
    public void addMinutes(Integer minutes) throws StickException {
        if (minutes <= 0) {
            throw new StickException(ErrorCode.INVALID_STICK_MINUTES.code(),
                                     ErrorCode.INVALID_STICK_MINUTES.message());
        }
        mins.computeIfAbsent(minutes, m -> {
                         return new StickContext(minutes, false);
                     });
    }

    @Override
    public Collection<Stick> build(Integer minutesOfDay,
                                   Integer daysOfyear,
                                   ZonedDateTime alignTime) throws StickException {
        synchronized (this) {
            try {
                var r = new HashSet<Stick>(16);
                if (minutesOfDay != null) {
                    if (minutesOfDay <= 0) {
                        throw new StickException(ErrorCode.INVALID_MINUTES_OF_DAY.code(),
                                                 ErrorCode.INVALID_MINUTES_OF_DAY.message());
                    }
                    else {
                        r.addAll(buildMinutesWithRest(minutesOfDay,
                                                      alignTime));
                    }
                }
                if (daysOfyear != null) {
                    if (daysOfyear <= 0) {
                        throw new StickException(ErrorCode.INVALID_MINUTES_OF_DAY.code(),
                                                 ErrorCode.INVALID_MINUTES_OF_DAY.message());
                    }
                    else {
                        r.addAll(buildDaysWithRest(daysOfyear,
                                                   alignTime));
                    }
                }
                return r;
            }
            finally {
                align = alignTime;
            }
        }
    }

    @Override
    public Collection<Integer> getDays() throws StickException {
        return days.keySet();
    }

    @Override
    public String getInstrumentId() {
        return iid;
    }

    @Override
    public Collection<Integer> getMinutes() throws StickException {
        return mins.keySet();
    }

    @Override
    public void removeDays(Integer d) throws StickException {
        days.remove(d);
    }

    @Override
    public void removeMinutes(Integer minutes) throws StickException {
        mins.remove(minutes);
    }

    @Override
    public Collection<Stick> tryBuild(ZonedDateTime alignTime) throws StickException {
        synchronized (this) {
            try {
                if (align.isEqual(alignTime)) {
                    /*
                     * Some sticks published, just publish the rest.
                     */
                    return buildRest();
                }
                else if (align.isAfter(alignTime)) {
                    /*
                     * Wield thing happens.
                     */
                    throw new StickException(ErrorCode.WRONG_END_TRADE_ALIGNTIME.code(),
                                             ErrorCode.WRONG_END_TRADE_ALIGNTIME.message());
                }
                else {
                    return new HashSet<>(1);
                }
            }
            finally {
                align = alignTime;
            }
        }
    }

    @Override
    public void update(Tick tick) throws StickException {
        synchronized (this) {
            if (iid == null) {
                iid = tick.getInstrumentId();
            }
            if (!iid.equals(tick.getInstrumentId())) {
                throw new StickException(ErrorCode.WRONG_INSTRUMENT_TICK.code(),
                                         ErrorCode.WRONG_INSTRUMENT_TICK.message());
            }
            for (var v : mins.values()) {
                v.update(tick);
            }
        }
    }

    private Collection<Stick> buildDaysWithRest(Integer daysOfYear,
                                                ZonedDateTime alignTime) throws StickException {
        try {
            var r = new HashSet<Stick>(16);
            r.addAll(getSticks(daysOfYear,
                               this.days,
                               true,
                               true));
            if (align.isEqual(alignTime)) {
                /*
                 * Time to build rest sticks.
                 */
                r.addAll(getSticks(daysOfYear,
                                   this.days,
                                   true,
                                   false));
            }
            return r;
        }
        finally {
            daysOfYr = daysOfYear;
        }
    }

    private Collection<Stick> buildMinutesWithRest(Integer minutesOfDay,
                                                   ZonedDateTime alignTime) throws StickException {
        try {
            var r = new HashSet<Stick>(16);
            r.addAll(getSticks(minutesOfDay,
                               mins,
                               false,
                               true));
            if (align.isEqual(alignTime)) {
                /*
                 * Time to build rest sticks.
                 */
                r.addAll(getSticks(minutesOfDay,
                                   mins,
                                   false,
                                   false));
            }

            return r;
        }
        finally {
            minOfDay = minutesOfDay;
        }
    }

    private Collection<Stick> buildRest() throws StickException {
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
                                        boolean canDiv) throws StickException {
        var r = new HashSet<Stick>(12);
        for (var e : sticks.entrySet()) {
            if (canDiv == (x % e.getKey() == 0)) {
                var s = e.getValue().nextStick(eg.nextStickId());
                if (!isDay) {
                    s.setMinutes(e.getKey());
                }
                else {
                    s.setDays(e.getKey());
                }
                r.add(s);
            }
        }
        return r;
    }
}
