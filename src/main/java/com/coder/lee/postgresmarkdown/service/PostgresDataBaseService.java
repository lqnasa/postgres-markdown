package com.coder.lee.postgresmarkdown.service;

import com.coder.lee.postgresmarkdown.model.PgDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description: PostgresDataBaseService
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 1:44
 *
 * @author coderLee23
 */
@Service
public class PostgresDataBaseService {

    private static final String GET_DATABASE_LIST_SQL = "select datname, pg_catalog.pg_get_userbyid(datdba) as owner from pg_database where  datistemplate = 'f' and pg_catalog.pg_get_userbyid(datdba) = ?";

    public List<PgDatabase> getDatabaseList(JdbcTemplate jdbcTemplate, String owner) {
        Assert.notNull(jdbcTemplate,"jdbcTemplate must not be null");
        Assert.hasText(owner,"owner must not be null or empty");
        // queryForStream 需要使用 try-with-resources 来释放连接
        // @return the result Stream, containing mapped objects, needing to be closed once fully processed (e.g. through a try-with-resources clause)
        try (Stream<PgDatabase> pgDatabaseStream = jdbcTemplate.queryForStream(
                GET_DATABASE_LIST_SQL,
                (rs, rowNum) -> PgDatabase.builder()
                        .datname(rs.getString("datname"))
                        .owner(rs.getString("owner")).build(),
                owner)) {
            return pgDatabaseStream.collect(Collectors.toList());
        }

    }

}
