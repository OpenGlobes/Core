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

import com.openglobes.core.event.IEvent;
import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.market.InstrumentNotice;
import com.openglobes.core.utils.Loggers;

import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentNoticeHandler implements IEventHandler<InstrumentNotice> {

    private final IStickEngine eg;

    public InstrumentNoticeHandler(IStickEngine engine) {
        Objects.requireNonNull(engine);
        eg = engine;
    }

    @Override
    public void handle(IEvent<InstrumentNotice> event) {
        try {
            eg.onNotice(event.get());
        } catch (PublishException ex) {
            Loggers.getLogger(InstrumentNoticeHandler.class.getCanonicalName()).log(Level.SEVERE,
                                                                                    ex.toString(),
                                                                                    ex);
        }
    }

}
