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
package com.openglobes.core.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
@XmlRootElement(name = "Core")
public class CoreConfiguration {

    private List<ConnectorConfiguration>  connectors;
    private List<DataSourceConfiguration> dataSources;
    private List<GatewayConfiguration>    gates;
    private List<PluginConfiguration>     plugins;

    public CoreConfiguration() {
        gates       = new LinkedList<>();
        plugins     = new LinkedList<>();
        connectors  = new LinkedList<>();
        dataSources = new LinkedList<>();
    }

    @XmlElement(name = "Connectors")
    public List<ConnectorConfiguration> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<ConnectorConfiguration> connectors) {
        this.connectors = connectors;
    }

    @XmlElement(name = "DataSources")
    public List<DataSourceConfiguration> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<DataSourceConfiguration> dataSources) {
        this.dataSources = dataSources;
    }

    @XmlElement(name = "Gateways")
    public List<GatewayConfiguration> getGateways() {
        return gates;
    }

    public void setGates(List<GatewayConfiguration> gates) {
        this.gates = gates;
    }

    @XmlElement(name = "Plugins")
    public List<PluginConfiguration> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginConfiguration> plugins) {
        this.plugins = plugins;
    }

}
