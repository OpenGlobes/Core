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

import com.openglobes.core.trader.Request;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
@DisplayName("Normal Cases")
@TestMethodOrder(OrderAnnotation.class)
public class NormalCaseTest extends Facilities {

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @Test
    @Order(0)
    @DisplayName("IQuery::insert()")
    public void insertion() {
        try {
            assertEquals(1,
                         insertRequest(1L,
                                       "c2101",
                                       1001L));
            assertEquals(1,
                         insertRequest(2L,
                                       "c2105",
                                       1002L));
            assertEquals(1,
                         insertRequest(3L,
                                       "c2109",
                                       1003L));
            assertEquals(1,
                         insertRequest(4L,
                                       "c2201",
                                       1004L));
            assertThrows(SQLException.class,
                         () -> {
                             insertRequest(4L, "c2205", 1005L);
                         },
                         "Insert duplicated primary should throw SQLException.");
        } catch (SQLException ex) {
            fail(ex.getMessage() + "(" + ex.getSQLState() + ":" + ex.getErrorCode() + ")");
        } catch (DbaException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("IQuery::remove()")
    public void removal() {
        try {
            assertEquals(1,
                         removeRequest(1L,
                                       null,
                                       null));
            assertEquals(3,
                         selectRequest(),
                         "Delete one record.");
            assertEquals(1,
                         removeRequest(null,
                                       "c2105",
                                       null));
            assertEquals(2,
                         selectRequest(),
                         "Delete two records.");
            assertEquals(1,
                         removeRequest(null,
                                       null,
                                       1003L));
            assertEquals(1,
                         selectRequest(),
                         "Delete three records.");
        } catch (SQLException ex) {
            fail(ex.getMessage() + "(" + ex.getSQLState() + ":" + ex.getErrorCode() + ")");
        } catch (DbaException ex) {
            fail(ex.getMessage());
        }
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        clear();
    }

    @Test
    @Order(2)
    @DisplayName("IQuery::update()")
    public void update() {
        var x = new Request();
        try {
            assertEquals(1,
                         selectRequestById(4L),
                         "Selecting existing record with primary key should return 1 row.");
            x.setRequestId(4L);
            x.setInstrumentId("c2205");
            assertEquals(1,
                         updateRequest(x));
            x.setRequestId(5L);
            assertEquals(0,
                         updateRequest(x),
                         "Update an record with non-existing key should return nothing.");
            assertEquals(1,
                         selectRequestByInstrumentId("c2205"),
                         "Select with a non-primary key condition should work too.");

        } catch (SQLException ex) {
            fail(ex.getMessage() + "(" + ex.getSQLState() + ":" + ex.getErrorCode() + ")");
        } catch (DbaException ex) {
            fail(ex.getMessage());
        }
    }

    private int insertRequest(Long requestId,
                              String instrumentId,
                              Long orderId) throws SQLException,
                                                   DbaException {
        var r = new Request();
        r.setRequestId(requestId);
        r.setInstrumentId(instrumentId);
        r.setOrderId(orderId);
        return query().insert(Request.class,
                              r);
    }

    private int removeRequest(Long requestId,
                              String instrumentId,
                              Long orderId) throws SQLException, DbaException {
        try {
            if (requestId != null) {
                return query().remove(Request.class,
                                      Queries.equals(Request.class.getDeclaredField("requestId"),
                                                     requestId));
            } else if (instrumentId != null) {
                return query().remove(Request.class,
                                      Queries.equals(Request.class.getDeclaredField("instrumentId"),
                                                     instrumentId));
            } else if (orderId != null) {
                return query().remove(Request.class,
                                      Queries.equals(Request.class.getDeclaredField("orderId"),
                                                     orderId));
            } else {
                return 0;
            }
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new FieldAccessException(ex.getMessage(),
                                           ex);
        }
    }

    private int selectRequest() throws SQLException,
                                       DbaException {
        try {
            return query().select(Request.class,
                                  Queries.isNotNull(Request.class.getDeclaredField("requestId")),
                                  Request::new)
                          .size();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new FieldAccessException(ex.getMessage(),
                                           ex);
        }
    }

    private int selectRequestById(Long requestId) throws SQLException,
                                                         DbaException {
        try {
            return query().select(Request.class,
                                  Queries.equals(Request.class.getDeclaredField("requestId"),
                                                 requestId),
                                  Request::new)
                          .size();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new FieldAccessException(ex.getMessage(),
                                           ex);
        }
    }

    private int selectRequestByInstrumentId(String instrumentId) throws SQLException,
                                                                        DbaException {
        try {
            return query().select(Request.class,
                                  Queries.equals(Request.class.getDeclaredField("instrumentId"),
                                                 instrumentId),
                                  Request::new)
                          .size();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new FieldAccessException(ex.getMessage(),
                                           ex);
        }
    }

    private int updateRequest(Request object) throws SQLException,
                                                     DbaException {
        try {
            return query().update(Request.class,
                                  object,
                                  Queries.equals(Request.class.getDeclaredField("requestId"),
                                                 object.getRequestId()));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new FieldAccessException(ex.getMessage(),
                                           ex);
        }
    }
}
