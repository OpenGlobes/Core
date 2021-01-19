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
package com.openglobes.core.dba;

import com.openglobes.core.dba.tables.EmptyTable;
import com.openglobes.core.dba.tables.InvalidTableWithUnsupportedFieldTypes;
import com.openglobes.core.dba.tables.InvalidTableWithoutId;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
@DisplayName("Exception Cases")
public class ExceptionCaseTest extends Facilities {

    public ExceptionCaseTest() {
    }

    @Test
    @DisplayName("No field in a table.")
    public void emptyTable() {
        Exception ignored = assertThrows(NoFieldException.class,
                                         () -> {
                                             query().insert(EmptyTable.class,
                                                            new EmptyTable());
                                         },
                                         "IQuery::insert should throw exception on am empty data object.");
    }

    @Test
    @DisplayName("No ID in table.")
    public void noPrimaryKey() {
        var r = new InvalidTableWithoutId();
        r.setName("UserWithoutID");
        Exception ignored = assertThrows(NoPrimaryKeyException.class,
                                         () -> {
                                             query().insert(InvalidTableWithoutId.class,
                                                            r);
                                         },
                                         "IQuery::insert should throw exception on data object without ID.");
    }

    @Test
    @DisplayName("Null parameter.")
    public void nullParameter() {
        Exception ignored = assertThrows(Exception.class,
                                         () -> {
                                             Queries.createQuery(null);
                                         },
                                         "Method must throw exception on null parameters.");
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        clear();
    }

    @Test
    @DisplayName("Expect exception on unsupported field types.")
    public void unsupportedFieldTypes() {
        var r = new InvalidTableWithUnsupportedFieldTypes();
        r.setInvalidTableWithUnsupportedFieldTypesId(System.currentTimeMillis());
        Exception ignored = assertThrows(UnsupportedFieldTypeException.class,
                                         () -> {
                                             query().insert(InvalidTableWithUnsupportedFieldTypes.class,
                                                            r);
                                         },
                                         "IQuery::insert should throw exception on data object with supported field types.");
    }

}
