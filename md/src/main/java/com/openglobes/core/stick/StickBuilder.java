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
 * @since 1.0
 */
public class StickBuilder implements IStickBuilder{
    
    public StickBuilder() {
    }

    @Override
    public void addMinutes(Integer minutes) throws StickException {
        // TODO addMinutes
    }
    
    @Override
    public Stick build() throws StickException {
        // TODO build
        return null;
    }

    @Override
    public String getInstrumentId() {
        // TODO getInstrumentId
        return null;
    }

    @Override
    public Collection<Integer> getMinutes() throws StickException {
        // TODO getMinutes
        return null;
    }

    @Override
    public void removeMinutes(Integer minutes) throws StickException {
        // TODO removeMinutes
    }

    @Override
    public void update(Tick tick) throws StickException {
        // TODO update
    }  
}
