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
package com.openglobes.core.context;

import com.openglobes.core.ICore;
import com.openglobes.core.configuration.PluginConfiguration;
import com.openglobes.core.plugin.IPlugin;
import com.openglobes.core.plugin.IPluginContext;
import com.openglobes.core.plugin.PluginException;
import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class PluginContext implements IPluginContext {

    private final PluginConfiguration conf;
    private final ICore core;
    private final IPlugin p;

    public PluginContext(PluginConfiguration configuration,
                         IPlugin plugin,
                         ICore core) {
        this.conf = configuration;
        this.p = plugin;
        this.core = core;
    }

    @Override
    public IPlugin get() {
        return p;
    }

    @Override
    public String getClassName() {
        return conf.getClassCanonicalName();
    }

    @Override
    public ICore getCore() {
        return core;
    }

    @Override
    public String getName() {
        return conf.getName();
    }

    @Override
    public Properties getProperties() {
        var r = new Properties();
        r.putAll(conf.getProperties());
        return r;
    }

    IPlugin getPlugin() throws PluginException {
        return p;
    }
}
