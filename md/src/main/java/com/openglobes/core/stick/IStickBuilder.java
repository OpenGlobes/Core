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
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 *
 * @author Hongbao Chen
 * @sicne 1.0
 */
public interface IStickBuilder {

    /**
     * Update the specified {@link Tick} into corresponding stick context.
     * <p>
     * The method is synchronized on {@code this} object.
     *
     * @param tick tick.
     *
     * @throws StickException thrown when the specified tick doesn't belong to
     *                        this builder.
     */
    void update(Tick tick) throws StickException;

    void addMinutes(Integer minutes) throws StickException;

    void addDays(Integer days) throws StickException;

    void removeMinutes(Integer minutes) throws StickException;

    void removeDays(Integer days) throws StickException;

    Collection<Integer> getMinutes() throws StickException;

    Collection<Integer> getDays() throws StickException;

    String getInstrumentId();

    /**
     * Build sticks whose minutes can divide the specified minutes-of-day, or
     * days can divide the specified days of year.
     * <p>
     * The condition is equally {@code minutes-of-day % minutes == 0} or
     * {@code days-of-year % days == 0}.
     * <p>
     * The method is synchronized on {@code this} object.
     *
     * @param minutesOfDay minute-of-trading day.
     * @param daysOfyear   days-of-year.
     * @param alignTime    current align time on minute.
     *
     * @return collection of sticks that should be emitted on the specifed
     *         minutes-of-day of days-of-year.
     *
     * @throws StickException thrown when given parameters are invalid, or fail
     *                        to create sticks.
     */
    Collection<Stick> build(Integer minutesOfDay, Integer daysOfyear, ZonedDateTime alignTime) throws StickException;

    /**
     * Try the build all sticks by the specified time, which is aligned on
     * minute, no matter whether now is its corresponding minute.
     * <p>
     * If current time is before the specfied align time, no stick is built.
     * When it is the specified time, call to {@code build(...)} will build all
     * sticks including those refered by this method.
     * <p>
     * If current time is the specified time, and the {@code build(...)} had
     * been called, call to the method builds sticks that are not-yet built by
     * calling {@code build(...)}. So sticks built by {@code build(...)} and
     * {@code tryBuild(...)} are all sticks that can be built in the end of
     * trading day.
     * <p>
     * The method is synchronized on {@code this} object.
     *
     * @param endOfDayTime time for building all sticks at the end of trading
     *                     day.
     *
     * @return collection of sticks that should be emitted at the specified
     *         time.
     *
     * @throws StickException thrown when fail building sticks.s
     */
    Collection<Stick> tryBuild(ZonedDateTime endOfDayTime) throws StickException;
}
