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

import com.openglobes.core.dba.DbaException;
import com.openglobes.core.stick.ErrorCode;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DefaultMarketDataSource extends MarketDataSource {

    @Override
    public IMarketDataConnection getConnection() throws MarketDataSourceException {
        try {
            return new DefaultMarketDataConnection(getSqlConnection(), this);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.DATASOURCE_GET_CONNECTION_FAIL.code(),
                                                ErrorCode.DATASOURCE_GET_CONNECTION_FAIL.message(),
                                                ex);
        }
    }

}
