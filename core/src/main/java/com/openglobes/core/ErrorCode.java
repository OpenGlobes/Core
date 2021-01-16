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
    PLAYER_PLUGIN_NOT_FOUND(0x1012, "Plugin not found."),
    PLAYER_INVALID_PLUGIN_NAME(0x1013, "Invalid plugin name."),
    RESPONSE_CONTEXT_NULL(0x1014, "Response context null."),
    PLAYER_PLUGIN_PATH_NULL(0x1015, "Directory path for plugins null."),
    PLAYER_PLUGIN_BAD_URL(0x1016, "Plugin bad URL."),
    PLAYER_PLUGIN_NULL(0x1017, "Plugin null."),
    PLAYER_CONNECTOR_NOT_FOUND(0x1018, "Connector not found."),
    PLAYER_ENGINE_NOT_FOUND(0x1019, "Player's trader engine not found."),
    PLAYER_ALGORITHM_NOT_FOUND(0x1019, "Player's trader algorithm not found."),
    PLAYER_DATASOURCE_NOT_FOUND(0x1019, "Player's trader data source not found."),
    PLAYER_GATEWAY_NOT_FOUND(0x101A, "Player's gateway not found."),
    PLAYER_CONFIG_PARSE_FAIL(0x101B, "Player's configuration parsing failed."),
    PLAYER_FILE_OR_DIR_FAIL(0x101C, "Player access to file or directory failed."),
    SESSION_DEST_ID_NOT_FOUND(0x101D, "Destinated order ID not found."),
    SESSION_SRC_ID_NOT_FOUND(0x101D, "Source order ID not found."),
    SESSION_RESPOND_TYPE_NOT_SUPPORTED(0x101E, "Respond type not supported."),
    INTERCEPTOR_CHAIN_BUSY(0x101F, "Interceptor chain busy and locked."),
    INTERCEPTOR_REQUEST_TYPE_NOT_SUPPORTED(0x1020, "Interceptor does not support the request type."),
    INTERCEPTOR_RESPONSE_TYPE_NOT_SUPPORTED(0x1021, "Interceptor does not support the response type."),
    INTERCEPTOR_TYPE_NOT_SUPPORTED(0x1021, "Interceptor does not support the type."),
    PLAYER_SUBSCRIBE_EVENT_FAIL(0x1022, "Player subscription of event failed."),
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
