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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
@DisplayName("Exception Cases")
public class ExceptionCaseMaster {

    private static IQuery query;

    @BeforeAll
    public static void setUpClass() {
        query = Queries.createQuery(TestUtils.getDefaultConnection());
    }

    @AfterAll
    public static void tearDownClass() {
        query = null;
        System.gc();
    }

    public ExceptionCaseMaster() {
    }

    @Test
    @DisplayName("No field in a table.")
    public void emptyTable() {
        try {
            query.insert(EmptyTable.class,
                         new EmptyTable());
            Assertions.fail("IQuery::insert should throw exception on am empty data object.");
        }
        catch (NoFieldException ignored) {
        }
        catch (Throwable th) {
            Assertions.fail("Unexpected exception: " + th.getMessage());
        }
    }

    @Test
    @DisplayName("No ID in table.")
    public void noPrimaryKey() {
        try {
            var r = new InvalidTableWithoutId();
            r.setName("UserWithoutID");
            query.insert(InvalidTableWithoutId.class,
                         r);
            Assertions.fail("IQuery::insert should throw exception on data object without ID.");
        }
        catch (NoPrimaryKeyException ignored) {
        }
        catch (Throwable th) {
            Assertions.fail("Unexpected exception: " + th.getMessage());
        }
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    @DisplayName("Expect exception on unsupported field types.")
    public void unsupportedFieldTypes() {
        try {
            var r = new InvalidTableWithUnsupportedFieldTypes();
            r.setInvalidTableWithUnsupportedFieldTypesId(System.currentTimeMillis());
            query.insert(InvalidTableWithUnsupportedFieldTypes.class,
                         r);
            Assertions.fail("IQuery::insert should throw exception on data object with supported field types.");
        }
        catch (UnsupportedFieldTypeException ignored) {
        }
        catch (Throwable th) {
            Assertions.fail("Unexpected exception: " + th.getMessage());
        }
    }

}
