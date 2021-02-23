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

import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DataSourceData {

    private final AbstractTraderDataSource ds = new DefaultTraderDataSource();
    private final Properties props = new Properties();

    protected DataSourceData() {
        setProperties();
        setDataSource();
    }

    protected ITraderDataSource dataSource() {
        return ds;
    }

    private void setDataSource() {
        ds.open(props);
    }

    private void setProperties() {
        /*
         * Set properties.
         */
        props.put("DataSource.URL", "jdbc:h2:mem:default-db");
        props.put("DataSource.DriverClass", "org.h2.Driver");
        props.put("USER", "sa");
        props.put("PASSWORD", "");
    }
}
