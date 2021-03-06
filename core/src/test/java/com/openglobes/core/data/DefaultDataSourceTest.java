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

import com.openglobes.core.event.IEvent;
import com.openglobes.core.trader.Contract;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultDataSourceTest extends DataSourceData {

    private final Set<DataChangeType> changeTypes = new HashSet<DataChangeType>();

    public DefaultDataSourceTest() {
        setupListeners();
    }

    @Test
    @Order(1)
    @DisplayName("Test CREATE/UPDATE/DELETE callbacks.")
    public void testCallbacks() {
        final var contract = new Contract();

        contract.setContractId(getNextId());
        contract.setTimestamp(ZonedDateTime.now());

        assertDoesNotThrow(() -> {
            var conn = dataSource().getConnection();
            /*
             * CEATE callback.
             */
            conn.addContract(contract);
            /*
             * UPDATE callback.
             */
            contract.setOpenAmount(10000000.0D);
            contract.setTimestamp(ZonedDateTime.now());
            conn.updateContract(contract);
            /*
             * DELETE callback.
             */
            conn.removeContract(contract.getContractId());
            Thread.sleep(1000);
        });

        /*
         * Check callbacks.
         */
        assertEquals(3,
                     changeTypes.size());
        assertTrue(changeTypes.contains(DataChangeType.CREATE));
        assertTrue(changeTypes.contains(DataChangeType.UPDATE));
        assertTrue(changeTypes.contains(DataChangeType.DELETE));
    }

    @Test
    @DisplayName("Test connection's query.")
    public void testQuery() {
        final var contract = new Contract();

        contract.setContractId(getNextId());
        contract.setTimestamp(ZonedDateTime.now());

        assertDoesNotThrow(() -> {
            var conn = dataSource().getConnection();
            /*
             * Add contract.
             */
            conn.addContract(contract);

            /*
             * Test get specified contract.
             */
            Contract c = conn.getContractById(contract.getContractId());
            assertEquals(contract.getContractId(),
                         c.getContractId());
            assertEquals(contract.getTimestamp(),
                         c.getTimestamp());
            /*
             * Check get all contracts.
             */
            var cs = conn.getContracts();
            assertEquals(1,
                         cs.size());
            /*
             * Check integrity of contracts.
             */
            c = cs.iterator().next();
            assertEquals(contract.getContractId(),
                         c.getContractId());
            assertEquals(contract.getTimestamp(),
                         c.getTimestamp());
        });
    }

    @Test
    @DisplayName("Test connection's update.")
    public void testUpdate() {
        final var contract = new Contract();

        contract.setContractId(getNextId());
        contract.setTimestamp(ZonedDateTime.now());

        assertDoesNotThrow(() -> {
            var conn = dataSource().getConnection();
            /*
             * Add contract.
             */
            conn.addContract(contract);
            /*
             * Update contract.
             */
            contract.setTag("updated");
            conn.updateContract(contract);
            /*
             * Test get specified contract.
             */
            Contract c = conn.getContractById(contract.getContractId());
            assertEquals(contract.getContractId(),
                         c.getContractId());
            assertEquals(contract.getTimestamp(),
                         c.getTimestamp());
            assertEquals(contract.getTag(),
                         c.getTag());
        });
    }

    @Test
    @DisplayName("Test connection's remove.")
    public void testRemove() {
        final var contract = new Contract();

        contract.setContractId(getNextId());
        contract.setTimestamp(ZonedDateTime.now());

        assertDoesNotThrow(() -> {
            var conn = dataSource().getConnection();
            /*
             * Add contract.
             */
            conn.addContract(contract);
            /*
             * Remove contract.
             */
            conn.removeContract(contract.getContractId());
            /*
             * Test get specified contract, specified contract should have
             * been removed.
             */
            conn.getContracts().forEach(c -> {
                assertFalse(c.getContractId().equals(contract.getContractId()));
            });
        });
    }

    private void setupListeners() {
        assertDoesNotThrow(() -> {
            dataSource().addListener(Contract.class,
                                     (IEvent<Contract> event) -> {
                                         changeTypes.add(DataChangeType.CREATE);
                                     },
                                     DataChangeType.CREATE);
            dataSource().addListener(Contract.class,
                                     (IEvent<Contract> event) -> {
                                         changeTypes.add(DataChangeType.UPDATE);
                                     },
                                     DataChangeType.UPDATE);
            dataSource().addListener(Contract.class,
                                     (IEvent<Contract> event) -> {
                                         changeTypes.add(DataChangeType.DELETE);
                                     },
                                     DataChangeType.DELETE);
        });
    }
}
