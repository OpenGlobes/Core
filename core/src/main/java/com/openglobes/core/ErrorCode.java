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
package com.openglobes.core;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public enum ErrorCode {
    TRADER_ID_NOT_FOUND(0x1, "No such trader ID in map."),
    TRADER_SELECT_TYPE_NULL(0x2, "Trader selection type null."),
    TRADER_ID_DUPLICATED(0x3, "Duplicated trader ID."),
    TRADER_GATEWAY_NULL(0x4, "Trader gateway null."),
    TRADER_START_FAILED(0x5, "Trader gateway failed starting."),
    TRADER_STOP_FAILED(0x6, "Trader gateway failed stopping."),
    DELETE_REQS_NULL(0x7, "Cance; request(s) null."),
    DATASOURCE_NULL(0x8, "Data source null."),
    CONTRACT_NULL(0x9, "Contract collection null."),
    ALGORITHM_NULL(0xA, "Algorithm null."),
    ACCOUNT_NULL(0xB, "Account null."),
    USER_CODE_ERROR(0xC, "User code throwed exception."),
    REQUEST_NULL(0xD, "Order request(s) null."),
    NO_TRADE(0xE, "Order response(s) null."),
    ORDER_ID_NOT_FOUND(0xF, "No such order ID in map."),
    TRADER_GW_HANDLER_NULL(0x10, "Trader gateway handler null."),
    DELETE_ORDER_FAILED(0x11, "Failed deleting order."),
    TRADER_NOT_ENABLED(0x12, "Specified trader not enabled."),
    NO_TRADER(0x13, "No trader."),
    NO_TRADER_AVAILABLE(0x14, "No trader available."),
    INSTRUMENT_NULL(0x15, "Instrument null."),
    INSUFFICIENT_MONEY(0x16, "Insufficient money."),
    INSUFFICIENT_POSITION(0x17, "Insufficient position."),
    NONPOSITIVE_VOLUMN(0x18, "Negative volumn."),
    VOLUMN_NULL(0x19, "Null volumn."),
    INVALID_ORDER_TYPE(0x1A, "Invalid order type."),
    DS_FAILURE_UNFIXABLE(0x1B, "Data source operation failed and unfixable."),
    INCONSISTENT_COMMISSION_CONTRACT_STATUSES(0x1C, "Inconsistent commission and contract statuses."),
    INCONSISTENT_MARGIN_CONTRACT_STATUSES(0x1D, "Inconsistent margin and contract statuses."),
    INVALID_DELETING_CONTRACT_STATUS(0x1E, "Invalid deleting contract status."),
    CONTRACT_ID_NULL(0x1F, "Contract ID null."),
    COMMISSION_NULL(0x20, "Commission null."),
    INCONSISTENT_FROZEN_INFO(0x21, "Incompleted info."),
    INVALID_DELETING_MARGIN_STATUS(0x22, "Invalid deleting status."),
    MARGIN_NULL(0x23, "Margin null."),
    UNEXPECTED_ERROR(0x24, "Unexpected error."),
    OBJECT_COPY_FAILED(0x25, "Object copy failed."),
    DEST_ID_NOT_FOUND(0x26, "Destinated ID(s) not found."),
    COUNTDOWN_NOT_FOUND(0x27, "Count down not found."),
    PREPROC_RSPS_FAILED(0x28, "Preprocess response failed."),
    PROPERTIES_NULL(0x29, "Properties null."),
    PROPERTY_NOT_FOUND(0x2A, "Property not found."),
    INVALID_REQUEST_INSTANCE(0x2B, "Invalid request instance."),
    TICK_NULL(0x2C, "Tick null."),
    PRICE_NULL(0x2D, "Price null."),
    CONTRACT_STATUS_NULL(0x2E, "Contract status null."),
    INVALID_CONTRACT_STATUS(0x2F, "Invalid contract status."),
    RATIO_TYPE_NULL(0x30, "Ratio type null."),
    RATIO_NULL(0x31, "Ratio null."),
    MULTIPLE_NULL(0x32, "Volumn multiple null."),
    INSTRUMENT_ID_NULL(0x33, "Instrument ID null."),
    INVALID_INSTRUMENT_ID(0x34, "Invalid instrument ID."),
    NO_RESPONSE(0x35, "No response."),
    INCONSISTENT_CONTRACT_ORDER_INFO(0x36, "inconsistent information between contracts and order."),
    ORDER_ID_NULL(0x37, "Order ID null."),
    WITHDRAW_NULL(0x38, "Withdraw(s) null."),
    DEPOSIT_NULL(0x39, "Deposit(s) null."),
    WITHDRAW_AMOUNT_NULL(0x3A, "Withdraw amount null."),
    DEPOSIT_AMOUNT_NULL(0x3B, "Deposit amount null."),
    POSITION_NULL(0x3C, "Position(s) null."),
    POSITION_FIELD_NULL(0x3D, "Position field(s) null."),
    COMMISSION_AMOUNT_NULL(0x3E, "Commission amount null."),
    INVALID_FEE_STATUS(0x3F, "Invalid fee status."),
    TRADER_ENGINE_HANDLER_NULL(0x40, "Trader engine handler null."),
    DATA_CONNECTION_NULL(0x41, "Data connection null."),
    TRANSACTION_COMMIT_FAILED(0x42, "Transaction commit failed."),
    TRANSACTION_BEGIN_FAILED(0x43, "JDBC transaction begin failed."),
    TRANSACTION_RESTORE_FAILED(0x44, "JDBC transaction store failed."),
    TRANSACTION_ROLLBACK_FAILED(0x45, "JDBC tranaction rollback failed."),
    ACTION_NULL(0x46, "Action null."),
    DIRECTION_NULL(0x47, "Direction null."),
    OFFSET_NULL(0x48, "Offset null."),
    NO_CONTRACT(0x49, "No contract."),
    INVALID_DELETING_COMMISSION_STATUS(0x4A, "Invalid deleting commission status."),
    PREPROCESS_TRADE_FAIL(0x4B, "Preprocess trade failed."),
    PREPROCESS_RESPONSE_FAIL(0x4C, "Preprocess response failed."),
    DATA_HANDLER_NULL(0x4D, "Data handler null."),
    DBA_INSERT_FAIL(0x4E, "Database insertion failed."),
    DBA_SELECT_FAIL(0x4F, "Database selection failed."),
    DBA_UPDATE_FAIL(0x50, "Database update failed."),
    DBA_DELETE_FAIL(0x51, "Database deletion failed."),
    REFLECTION_FAIL(0x52, "Reflection failed."),
    MORE_ROWS_THAN_EXPECTED(0x53, "More rows than expected."),
    LESS_ROWS_THAN_EXPECTED(0x54, "Less rows than expected."),
    OBTAIN_CONDITION_FAIL(0x55, "Obtaining condition failed."),
    INVALID_PK_TYPE(0x56, "Invalid primary key type."),
    EVENT_PUBLISH_FAIL(0x57, "Event publish failed."),
    EVENTSOURCE_NULL(0x58, "Event source null."),
    REQUEST_DISPATCH_FAIL(0x59, "Request dispatch failed."),
    SUBSCRIBE_EVENT_FAIL(0x5A, "Duplicated subscription to event type."),
    PUBLISH_TO_STOPPED_QUEUE(0x5D, "Publish events to a stopped queue."),
    PUBLISH_EVENT_FAIL(0x5E, "Publish events failed."),
    DATASOURCE_CONNECTION_NOT_CACHED(0x5F, "Connection not in pool cache."),
    DATASOURCE_DRIVER_CLASS_MISSING(0x60, "Driver class not found."),
    DATASOURCE_EVENTSOURCE_NOT_FOUND(0x61, "No event source for the specified data change."),
    INVALID_DATASOURCE_TYPE(0x62, "Invalid data source type."),
    DATASOURCE_GET_CONNECTION_FAIL(0x63, "Get connection failed."),
    SESSION_NULL(0x1001, "Session null."),
    SESSION_REQUEST_NULL(0x1002, "Session's request null."),
    SESSION_ORDERID_NULL(0x1003, "Session's order ID null."),
    SESSION_DUPLICATED_ORDERID(0x1004, "Duplicated session's order ID."),
    SESSION_NOT_FOUND(0x1005, "Session not found."),
    CLASS_NULL(0x1006, "Class<?> null."),
    REQUEST_CONTEXT_NULL(0x1007, "Request context null."),
    CONNECTOR_NULL(0x1008, "Connector null."),
    SESSION_DISPOSED(0x1009, "Session disposed and unavailable."),
    SESSION_RESPOND_FAIL(0x100A, "Session response failed."),
    REGISTER_REQUEST_SESSION_FAIL(0x100B, "Register request/session failed."),
    REQCTX_TRADER_ENGINE_NULL(0x100C, "Trader engine in requet context null."),
    SHARED_CONTEXT_NULL(0x100D, "Shared context null."),
    SESSION_DO_REQUEST_FAIL(0x100E, "Session failed doing request."),
    SESSION_QRY_INSTRUMENT_FAIL(0x100F, "Session failed querying instrument."),
    SESSION_CONNECTOR_NULL(0x1010, "Session connector null."),
    SESSION_CREATE_FAIL(0x1011, "Session creation failed."),
    CORE_PLUGIN_NOT_FOUND(0x1012, "Plugin not found."),
    CORE_INVALID_PLUGIN_NAME(0x1013, "Invalid plugin name."),
    RESPONSE_CONTEXT_NULL(0x1014, "Response context null."),
    CORE_PLUGIN_PATH_NULL(0x1015, "Directory path for plugins null."),
    CORE_PLUGIN_BAD_URL(0x1016, "Plugin bad URL."),
    CORE_PLUGIN_NULL(0x1017, "Plugin null."),
    CORE_CONNECTOR_NOT_FOUND(0x1018, "Connector not found."),
    CORE_ENGINE_NOT_FOUND(0x1019, "Core's trader engine not found."),
    CORE_ALGORITHM_NOT_FOUND(0x1019, "Core's trader algorithm not found."),
    CORE_DATASOURCE_NOT_FOUND(0x1019, "Core's trader data source not found."),
    CORE_GATEWAY_NOT_FOUND(0x101A, "Core's gateway not found."),
    CORE_CONFIG_PARSE_FAIL(0x101B, "Core's configuration parsing failed."),
    CORE_FILE_OR_DIR_FAIL(0x101C, "Core access to file or directory failed."),
    SESSION_DEST_ID_NOT_FOUND(0x101D, "Destinated order ID not found."),
    SESSION_SRC_ID_NOT_FOUND(0x101D, "Source order ID not found."),
    SESSION_RESPOND_TYPE_NOT_SUPPORTED(0x101E, "Respond type not supported."),
    INTERCEPTOR_CHAIN_BUSY(0x101F, "Interceptor chain busy and locked."),
    INTERCEPTOR_REQUEST_TYPE_NOT_SUPPORTED(0x1020, "Interceptor does not support the request type."),
    INTERCEPTOR_RESPONSE_TYPE_NOT_SUPPORTED(0x1021, "Interceptor does not support the response type."),
    INTERCEPTOR_TYPE_NOT_SUPPORTED(0x1021, "Interceptor does not support the type."),
    CORE_SUBSCRIBE_EVENT_FAIL(0x1022, "Core subscription of event failed."),
    SESSION_RESPOND_OBJECT_NULL(0x1023, "Session responds a null object.");
    
    private final int code;
    private final String msg;

    private ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public int code() {
        return code;
    }
    
    public String message() {
        return msg;
    }
}
