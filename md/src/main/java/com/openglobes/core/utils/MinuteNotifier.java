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

import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.IEventSource;

import java.lang.ref.Cleaner;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class MinuteNotifier extends TimerTask implements IMinuteNotifier,
                                                         AutoCloseable {

    private final Cleaner.Cleanable cleanable;
    private final Cleaner cleaner = Cleaner.create();
    private final IEventSource evt;
    private final AtomicLong nid;
    private final Timer tm;

    public MinuteNotifier() {
        evt = new EventSource();
        nid = new AtomicLong(0);
        tm = Utils.schedulePerDuration(this,
                                       Duration.ofMinutes(1));
        cleanable = cleaner.register(this,
                                     new CleanAction(evt, tm));
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }

    @Override
    public IEventSource getEventSource() {
        return evt;
    }

    @Override
    public void run() {
        evt.publish(MinuteNotice.class,
                    new MinuteNotice(nid.incrementAndGet(),
                                     Utils.getRoundedTimeByMinute(),
                                     ZonedDateTime.now()));
    }

    private static class CleanAction implements Runnable {

        private final IEventSource src;
        private final Timer tm;

        CleanAction(IEventSource source, Timer timer) {
            src = source;
            tm = timer;
        }

        @Override
        public void run() {
            src.close();
            tm.cancel();
            tm.purge();
        }
    }
}
