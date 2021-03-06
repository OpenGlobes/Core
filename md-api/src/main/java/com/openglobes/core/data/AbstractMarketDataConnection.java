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
package com.openglobes.core.data;

import com.openglobes.core.dba.AbstractPooledConnection;
import com.openglobes.core.dba.IPooledDataSource;

import java.sql.Connection;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public abstract class AbstractMarketDataConnection extends AbstractPooledConnection
        implements IMarketDataConnection {

    /*
     * This class provides an unified abstraction for market data connection
     * implementation.
     */
    public AbstractMarketDataConnection(Connection connection,
                                        IPooledDataSource source) {
        super(connection, source);
    }
}
