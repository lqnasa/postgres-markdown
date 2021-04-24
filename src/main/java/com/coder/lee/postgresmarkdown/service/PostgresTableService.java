package com.coder.lee.postgresmarkdown.service;

import com.coder.lee.postgresmarkdown.enums.TableTypeEnum;
import com.coder.lee.postgresmarkdown.model.PgTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description: PostgresTableService
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 2:39
 *
 * @author coderLee23
 */
@Service
public class PostgresTableService {

    private static final String GET_PG_TABLE_LIST = "select table_name,table_catalog,table_type from information_schema.tables where table_schema = 'public' and table_type= ?";

    public List<PgTable> getPgTableList(JdbcTemplate jdbcTemplate, TableTypeEnum tableTypeEnum) {
        Assert.notNull(jdbcTemplate, "jdbcTemplate must not be null");
        Assert.notNull(tableTypeEnum, "tableTypeEnum must not be null");

        try (Stream<PgTable> pgTableStream = jdbcTemplate.queryForStream(
                GET_PG_TABLE_LIST,
                (rs, rowNum) -> PgTable.builder()
                        .tableName(rs.getString("table_name"))
                        .tableCatalog(rs.getString("table_catalog"))
                        .tableType(TableTypeEnum.valueOfByName(rs.getString("table_type")))
                        .build(),
                tableTypeEnum.getName())) {
            return pgTableStream.collect(Collectors.toList());
        }

    }

}
