package com.coder.lee.postgresmarkdown.service;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 2:58
 *
 * @author coderLee23
 */

import com.coder.lee.postgresmarkdown.model.PgTableInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PostgresTableInfoService {

    private static final String TABLE_INFO_SQL = " select" +
            "    base.ordinal_position," +
            "    base.column_name," +
            "    base.udt_name," +
            "    coalesce(character_maximum_length, numeric_precision, datetime_precision) length_precision," +
            "    base.is_nullable," +
            "            (case when(select attr.attnum = any(conkey) from pg_constraint where conrelid = clazz.oid and contype = 'p') = 't' then 'PRI' else '' end) column_key," +
            "    col_description(clazz.oid, attr.attnum) col_description," +
            "    base.column_default" +
            "            from" +
            "    information_schema.columns base," +
            "    pg_class clazz," +
            "    pg_attribute attr" +
            "    where" +
            "    base.table_name = ?" +
            "    and clazz.relname = base.table_name" +
            "    and attr.attname = base.column_name" +
            "    and clazz.oid = attr.attrelid" +
            "    and attr.attnum > 0";


    public List<PgTableInfo> getTableInfo(JdbcTemplate jdbcTemplate, String tableName) {
        Assert.notNull(jdbcTemplate,"jdbcTemplate must not be null");
        Assert.hasText(tableName,"tableName must not be null or empty");
        try (Stream<PgTableInfo> pgTableInfoStream = jdbcTemplate.queryForStream(
                TABLE_INFO_SQL,
                (rs, rowNum) -> PgTableInfo.builder()
                        .ordinalPosition(rs.getInt("ordinal_position"))
                        .columnName(rs.getString("column_name"))
                        .udtName(rs.getString("udt_name"))
                        .lengthPrecision(Optional.ofNullable(rs.getString("length_precision")).orElse(""))
                        .isNullable(rs.getString("is_nullable"))
                        .columnKey(rs.getString("column_key"))
                        .colDescription(Optional.ofNullable(rs.getString("col_description")).orElse(""))
                        .columnDefault(Optional.ofNullable(rs.getString("column_default")).orElse(""))
                        .build(),
                tableName)) {
            return pgTableInfoStream.collect(Collectors.toList());
        }

    }
}
