/*
 * Copyright (C) 2020-2021 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.trader.simulation;

import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.Trade;

import java.util.Collection;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class MarketMaker implements IMarketMaker {
    
    private final AbstractOrderQueue askQue;
    private final AbstractOrderQueue bidQue;
    
    protected MarketMaker() {
        askQue = new AskingOrderQueue();
        bidQue = new BidingOrderQueue();
    }

    @Override
    public Collection<Trade> getTradeUpdates() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Response> getResponseUpdates() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void applyRequest(Request request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
