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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Loggers {

    private static final Map<Handler, Object> handlers = new ConcurrentHashMap<>(64);

    public static void addLogHandler(Handler handler) {
        handlers.put(handler, handler);
    }

    public static Logger getLogger(Object name) {
        var r = Logger.getLogger(name.toString());
        r.setUseParentHandlers(false);
        r.addHandler(new CollectiveHandler());
        return r;
    }

    public static void removeLogHandler(Handler handler) {
        handlers.remove(handler);
    }

    private Loggers() {
    }

    private static class CollectiveHandler extends Handler {

        private final SimpleFormatter format;

        CollectiveHandler() {
            format = new SimpleFormatter();
        }

        @Override
        public void close() throws SecurityException {
            handlers.keySet().forEach(h -> {
                h.close();
            });
        }

        @Override
        public void flush() {
            handlers.keySet().forEach(h -> {
                h.flush();
            });
        }

        @Override
        public void publish(LogRecord record) {
            publishLog(record);
        }

        private void publishLog(LogRecord record) {
            if (handlers.isEmpty()) {
                System.err.println(format.format(record));
            }
            else {
                handlers.keySet().forEach(h -> {
                    h.publish(record);
                });
            }
        }
    }
}
