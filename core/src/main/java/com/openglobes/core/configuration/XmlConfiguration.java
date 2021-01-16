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
package com.openglobes.core.configuration;

import com.openglobes.core.ErrorCode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Hongbao Chen
 * @sicne 1.0
 */
public class XmlConfiguration {

    public static <T> T load(Class<T> clazz, Reader reader) throws ConfigurationException {
        try {
            var content = JAXBContext.newInstance(clazz);
            return clazz.cast(content.createUnmarshaller().unmarshal(reader));
        }
        catch (JAXBException ex) {
            throw new ConfigurationException(ErrorCode.CORE_CONFIG_PARSE_FAIL.code(),
                                             ErrorCode.CORE_CONFIG_PARSE_FAIL.message(),
                                             ex);
        }
    }

    public static <T> T load(Class<T> clazz, InputStream stream) throws ConfigurationException {
        try {
            var content = JAXBContext.newInstance(clazz);
            return clazz.cast(content.createUnmarshaller().unmarshal(stream));
        }
        catch (JAXBException ex) {
            throw new ConfigurationException(ErrorCode.CORE_CONFIG_PARSE_FAIL.code(),
                                             ErrorCode.CORE_CONFIG_PARSE_FAIL.message(),
                                             ex);
        }
    }

    public static <T> T load(Class<T> clazz, String xml) throws ConfigurationException {
        try {
            var content = JAXBContext.newInstance(clazz);
            return clazz.cast(content.createUnmarshaller().unmarshal(new ByteArrayInputStream(xml.getBytes())));
        }
        catch (JAXBException ex) {
            throw new ConfigurationException(ErrorCode.CORE_CONFIG_PARSE_FAIL.code(),
                                             ErrorCode.CORE_CONFIG_PARSE_FAIL.message(),
                                             ex);
        }
    }

    private XmlConfiguration() {
    }
}
