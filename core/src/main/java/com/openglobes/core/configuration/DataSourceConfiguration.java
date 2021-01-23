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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class DataSourceConfiguration {

    private String              classCanonicalName;
    private String              driverClass;
    private String              name;
    private String              password;
    private Map<String, String> properties;
    private String              url;
    private String              username;

    public DataSourceConfiguration() {
        properties = new HashMap<>(16);
    }

    @XmlElement(name = "ClassCanonicalName",
                required = true)
    public String getClassCanonicalName() {
        return classCanonicalName;
    }

    public void setClassCanonicalName(String classCanonicalName) {
        this.classCanonicalName = classCanonicalName;
    }

    @XmlElement(name = "DriverClass",
                required = true)
    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    @XmlElement(name = "Name",
                required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "Password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "Properties")
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @XmlElement(name = "Url",
                required = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name = "Username")
    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }
}
