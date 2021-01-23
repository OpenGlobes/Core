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
package com.openglobes.core.context;

import com.openglobes.core.configuration.DataSourceConfiguration;
import com.openglobes.core.data.ITraderDataSource;

import java.util.Properties;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class DataSourceContext implements IDataSourceContext {

    private final DataSourceConfiguration conf;
    private final ITraderDataSource       source;

    public DataSourceContext(DataSourceConfiguration configuration,
                             ITraderDataSource source) {
        this.conf   = configuration;
        this.source = source;
    }

    @Override
    public String getClassName() {
        return conf.getClassCanonicalName();
    }

    @Override
    public String getDriverClass() {
        return conf.getDriverClass();
    }

    @Override
    public String getName() {
        return conf.getName();
    }

    @Override
    public ITraderDataSource get() {
        return source;
    }

    @Override
    public String getPassword() {
        return conf.getPassword();
    }

    @Override
    public Properties getProperties() {
        var r = new Properties();
        r.putAll(conf.getProperties());
        return r;
    }

    @Override
    public String getUrl() {
        return conf.getUrl();
    }

    @Override
    public String getUsername() {
        return conf.getUserName();
    }

}
