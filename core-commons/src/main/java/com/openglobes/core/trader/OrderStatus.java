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
package com.openglobes.core.trader;

/**
 * Order status.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class OrderStatus {

    public static final int ACCEPTED   = (0x63);
    public static final int ALL_TRADED = (0x60);
    public static final int DELETED    = (0x64);
    public static final int QUEUED     = (0x61);
    public static final int REJECTED   = (0x65);
    public static final int UNQUEUED   = (0x62);

    private OrderStatus() {
    }
}
