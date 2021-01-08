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
package com.openglobes.core.utils;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Utils {

    /**
     * Test the now's time in the specified range denoted by the range
     * {@code (from, to]}, which is an exclusive begin to the inclusive end.
     *
     * @param now       now'time.
     * @param rangeFrom exclusive begin of the time rane.
     * @param rangeTo   inclusive end of the time range.
     *
     * @return {@code true} is the specifed now is in the time range.
     */
    public static boolean inRange(LocalTime now,
                                  LocalTime rangeFrom,
                                  LocalTime rangeTo) {
        if (rangeFrom.isBefore(rangeTo)) {
            return now.isAfter(rangeFrom) && (!now.isAfter(rangeTo));
        }
        else if (rangeFrom.isAfter(rangeTo)) {
            return now.isAfter(rangeFrom) || (!now.isAfter(rangeTo));
        }
        else {
            return false;
        }
    }

    /**
     * Schedule task to run from the begin of next time when some durations past
     * since the begin of day, and repeats for every duration.
     *
     * @param task     timer task.
     * @param duration duration between two tasks
     */
    public static void schedulePerDuration(TimerTask task, Duration duration) {
        var now = ZonedDateTime.now();
        var calendar = Calendar.getInstance();
        /*
         * Move time to next begin, may have fractional seconds of a minute.
         */
        var s = ZonedDateTime.of(now.toLocalDate(),
                             LocalTime.of(0, 0),
                             now.getZone());
        while (s.isBefore(now)) {
            s = s.plus(duration);
        }
        /*
         * Adjust month value to be used by Date/Calendar. Don't give the
         * seconds so it rounds by minute.
         */
        calendar.set(s.getYear(),
                     s.getMonthValue() - 1,
                     s.getDayOfMonth(),
                     s.getHour(),
                     s.getMinute(),
                     s.getSecond());
        new Timer().schedule(task,
                             calendar.getTime(),
                             duration.toMillis());
    }

    private Utils() {
    }

    public static ZonedDateTime getAlignByMinute() {
        ZonedDateTime n = ZonedDateTime.now();
        ZonedDateTime r = ZonedDateTime.of(n.getYear(),
                                           n.getMonthValue(),
                                           n.getDayOfMonth(),
                                           n.getHour(),
                                           n.getMinute(),
                                           0,
                                           0,
                                           n.getZone());
        return n.getSecond() >= 30 ? r.plusMinutes(1) : r;
    }
}
