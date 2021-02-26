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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class GatewayConfiguration {

    private String classCanonicalName;
    private String name;
    private Map<String, String> properties;

    public GatewayConfiguration() {
        properties = new HashMap<>(16);
    }

    public String getClassCanonicalName() {
        return classCanonicalName;
    }

    public void setClassCanonicalName(String classCanonicalName) {
        this.classCanonicalName = classCanonicalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
