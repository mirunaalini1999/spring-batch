/**
 * Copyright (C) Appranix, Inc - All Rights Reserved.
 *
 * <p>Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * <p>Proprietary and confidential.
 */
package com.example.springbatch.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Convert the given Map collection into JSON String and vice versa. */
@Slf4j
public class MapCollectionTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    @Setter private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(
            PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            if (parameter == null) {
                parameter = new HashMap<>();
            }
            ps.setString(i, toJsonString(parameter)); // Store JSON as TEXT in SQLite
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        } catch (RuntimeException e) {
            log.error("ALERT:ACTION_REQUIRED MY_BATIS_TYPE_HANDLER MAP_TYPE_ERROR - ", e);
            throw new SQLException(e);
        }
    }

    public String toJsonString(Map<String, Object> additionalAttributes)
            throws JsonProcessingException {
        return objectMapper.writeValueAsString(additionalAttributes);
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        try {
            String value = rs.getString(columnName);
            if (value == null) {
                return new HashMap<>();
            } else {
                return objectMapper.readValue(
                        value,
                        new TypeReference<>() {
                            // convert Map json string to object
                        });
            }
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        } catch (RuntimeException e) {
            log.error("ALERT:ACTION_REQUIRED MY_BATIS_TYPE_HANDLER MAP_TYPE_ERROR - ", e);
            throw new SQLException(e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        try {
            String value = rs.getString(columnIndex);
            if (value == null) {
                return new HashMap<>();
            } else {
                return objectMapper.readValue(
                        value,
                        new TypeReference<>() {
                            // convert Map json string to object
                        });
            }
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        } catch (RuntimeException e) {
            log.error("ALERT:ACTION_REQUIRED MY_BATIS_TYPE_HANDLER MAP_TYPE_ERROR - ", e);
            throw new SQLException(e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        try {
            String value = cs.getString(columnIndex);
            if (value == null) {
                return new HashMap<>();
            } else {
                return objectMapper.readValue(
                        value,
                        new TypeReference<>() {
                            // convert Map json string to object
                        });
            }
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        } catch (RuntimeException e) {
            log.error("ALERT:ACTION_REQUIRED MY_BATIS_TYPE_HANDLER MAP_TYPE_ERROR - ", e);
            throw new SQLException(e);
        }
    }
}
