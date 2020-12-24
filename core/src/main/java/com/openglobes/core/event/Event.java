/*
 * Copyright (C) 2020 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.event;

import com.openglobes.core.utils.Utils;
import java.time.ZonedDateTime;

/**
 *
 * @author Hongbao Chen
 * @param <T>
 *
 * @since 1.0
 */
public class Event<T> implements IEvent<T> {

    private T o;
    private Class<T> c;
    private final Long seq;
    private final ZonedDateTime ts;

    public Event() {
        seq = Utils.getExecutionId();
        ts = ZonedDateTime.now();
    }

    public void set(T o) {
        this.o = o;
    }

    public void setType(Class<T> c) {
        this.c = c;
    }

    @Override
    public T get() {
        return o;
    }

    @Override
    public Long getSequence() {
        return seq;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return ts;
    }

    @Override
    public Class<T> getType() {
        return c;
    }

}
