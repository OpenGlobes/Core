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
import java.util.Collection;

/**
 *
 * @author Hongbao Chen
 * @sicne 1.0
 */
public interface IStickBuilder {

    void update(Tick tick) throws StickException;

    void addMinutes(Integer minutes) throws StickException;
    
    void removeMinutes(Integer minutes) throws StickException;

    Collection<Integer> getMinutes() throws StickException;
    
    String getInstrumentId();
    
    Stick build() throws StickException;
}
