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

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class StickContext extends Stick implements IStickContext {
    
    private Long curVol, preVol;

    public StickContext(Integer minutesOrDays, boolean isDay) {
        setup(minutesOrDays,
              isDay);
    }

    @Override
    public Stick nextStick(Long stickId) throws StickException {
        try {
            var r = get(stickId, curVol -preVol);
            preVol = curVol;
            return r;
        }
        finally {
            clean();
        }
    }

    @Override
    public void update(Tick tick) throws StickException {
        setClosePrice(tick.getPrice());
        if (getClosePrice() > getHighPrice()) {
            setHighPrice(getClosePrice());
        }
        if (getClosePrice() < getLowPrice()) {
            setLowPrice(getClosePrice());
        }
        setInstrumentId(tick.getInstrumentId());
        setOpenInsterest(tick.getOpenInterest());
        if (getOpenPrice() == null) {
            setOpenPrice(tick.getPrice());
        }
        setTimestamp(tick.getTimestamp());
        setTradingDay(tick.getTradingDay());
        if (preVol == null) {
            preVol = tick.getVolumn();
        }
        curVol = tick.getVolumn();
    }

    private void clean() {
        setClosePrice(null);
        setDays(null);
        setHighPrice(-Double.MAX_VALUE);
        setInstrumentId(null);
        setLowPrice(Double.MAX_VALUE);
        setMinutes(null);
        setOpenInsterest(null);
        setOpenPrice(null);
        setStickId(null);
        setTimestamp(null);
        setTradingDay(null);
        setVolumn(null);
    }

    private Stick get(Long stickId, Long volume) {
        var stick = new Stick();
        stick.setClosePrice(getClosePrice());
        stick.setDays(getDays());
        stick.setHighPrice(getHighPrice());
        stick.setInstrumentId(getInstrumentId());
        stick.setLowPrice(getLowPrice());
        stick.setMinutes(getMinutes());
        stick.setOpenInsterest(getOpenInsterest());
        stick.setOpenPrice(getOpenPrice());
        stick.setStickId(stickId);
        stick.setTimestamp(getTimestamp());
        stick.setTradingDay(getTradingDay());
        stick.setVolumn(volume);
        return stick;
    }

    private void setup(Integer x, boolean isDay) {
        clean();
        if (!isDay) {
            setMinutes(x);
            setDays(0);
        }
        else {
            setMinutes(0);
            setDays(x);
        }
    }

}
