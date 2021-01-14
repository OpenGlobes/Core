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

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IEventSource extends AutoCloseable {

    Collection<Class<?>> getSubscribedTypes();

    <T> void subscribe(Class<T> clazz, IEventHandler<T> handler) throws EventSourceException;

    Map<Class<?>, IEventHandler<?>> handlers();
    
    @Override
    void close();

    boolean isEmpty();

    <T> void publish(Class<T> clazz, T object) throws EventSourceException;
}
