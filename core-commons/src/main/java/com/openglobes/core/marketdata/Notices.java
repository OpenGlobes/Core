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
package com.openglobes.core.marketdata;

/**
 *
 * @author HongbaoChen
 * @since 1.0
 */
public class Notices {

    public static final int INSTRUMENT_END_TRADE = 0x6;
    public static final int INSTRUMENT_NO_TRADE = 0x4;
    public static final int INSTRUMENT_PRE_TRADE = 0x3;
    public static final int INSTRUMENT_TRADE = 0x5;
    public static final int MARKET_INIT = 0x1;
    public static final int MARKET_SETTLE = 0x2;

    private Notices() {
    }
}
