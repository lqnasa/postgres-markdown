package com.coder.lee.postgresmarkdown.controller;

import com.coder.lee.postgresmarkdown.datasource.DynamicRefreshProxy;
import com.coder.lee.postgresmarkdown.enums.TableTypeEnum;
import com.coder.lee.postgresmarkdown.model.PgDatabase;
import com.coder.lee.postgresmarkdown.model.PgTable;
import com.coder.lee.postgresmarkdown.model.PgTableInfo;
import com.coder.lee.postgresmarkdown.service.PostgresDataBaseService;
import com.coder.lee.postgresmarkdown.service.PostgresTableInfoService;
import com.coder.lee.postgresmarkdown.service.PostgresTableService;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Description: 导出数据库表生成markdown文件
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 1:33
 *
 * @author coderLee23
 */

@Api(tags = "postgresql markdown")
@RestController
public class PostgresMarkdownController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresMarkdownController.class);

    private static final String SAVE_FILE_PATH = System.getProperty("user.dir");

    @Autowired
    private PostgresDataBaseService postgresDataBaseService;

    @Autowired
    private PostgresTableService postgresTableService;

    @Autowired
    private PostgresTableInfoService postgresTableInfoService;

    @GetMapping("/generator")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ip", value = "数据库地址", defaultValue = "localhost", required = true),
            @ApiImplicitParam(name = "port", value = "端口", defaultValue = "5432", required = true),
            @ApiImplicitParam(name = "databaseName", value = "数据库名", required = true),
            @ApiImplicitParam(name = "userName", value = "用户名", defaultValue = "root", required = true),
            @ApiImplicitParam(name = "password", value = "密码", defaultValue = "root", required = true)
    })
    public String generatorDatabaseDoc(@RequestParam(defaultValue = "localhost") @Validated String ip,
                                       @RequestParam(defaultValue = "5432") @Validated String port,
                                       @RequestParam(defaultValue = "postgres") @Validated String databaseName,
                                       @RequestParam(defaultValue = "root") @Validated String userName,
                                       @RequestParam(defaultValue = "root") @Validated String password) {
        // 1、动态创建jdbcTemplate
        JdbcTemplate jdbcTemplate = getJdbcTemplate(ip, port, databaseName, userName, password);
        // 2、获取该用户拥有的所有数据库
        List<PgDatabase> databaseList = postgresDataBaseService.getDatabaseList(jdbcTemplate, userName);
        for (PgDatabase pgDatabase : databaseList) {
            // 3、动态切换需要导出的数据库
            jdbcTemplate = getJdbcTemplate(ip, port, pgDatabase.getDatname(), userName, password);
            //4、生成markdown文件的字符串结构
            String str = getTablesMarkdownStr(jdbcTemplate, pgDatabase);
            //5、写入到markdown文件中
            writeMarkdownFile(pgDatabase.getDatname(), str);
        }

        return String.format("文档生成成功，访问路径为：%s", SAVE_FILE_PATH);
    }

    private void writeMarkdownFile(String fileName, String str) {
        try {
            Files.write(Paths.get(SAVE_FILE_PATH, String.format("%s.md", fileName)), str.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("写入文档失败！", e);
        }
    }

    private String getTablesMarkdownStr(JdbcTemplate jdbcTemplate, PgDatabase pgDatabase) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("## 数据库 ").append(pgDatabase.getDatname()).append(" 的所有表信息\n");

        // 获取数据源的所有表
        List<PgTable> pgTableList = postgresTableService.getPgTableList(jdbcTemplate, TableTypeEnum.BASE_TABLE);

        for (PgTable pgTable : pgTableList) {
            stringBuilder.append("#### ").append(pgTableList.indexOf(pgTable) + 1).append("  ").append(pgTable.getTableName()).append("\n\n");
            stringBuilder.append("| 序号 | 名称 | 类型 | 长度/小数点 | 为空 | 主键约束 | 注释 | 默认值 |\n")
                    .append("| :--: | :--: | :--: | :--: | :--: | :--: | :--: | :--: |\n");
            // 获取表字段信息
            List<PgTableInfo> pgTableInfoList = postgresTableInfoService.getTableInfo(jdbcTemplate, pgTable.getTableName());
            for (PgTableInfo pgTableInfo : pgTableInfoList) {
                stringBuilder.append(String.format("| %s | `%s` | %s | %s | %s | %s | %s | %s |",
                        pgTableInfo.getOrdinalPosition(),
                        pgTableInfo.getColumnName(),
                        pgTableInfo.getUdtName(),
                        pgTableInfo.getLengthPrecision(),
                        pgTableInfo.getIsNullable(),
                        pgTableInfo.getColumnKey(),
                        pgTableInfo.getColDescription(),
                        pgTableInfo.getColumnDefault()))
                        .append("\n");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private JdbcTemplate getJdbcTemplate(String ip, String port, String databaseName, String userName, String password) {
        DataSource dataSource = getHikariDataSource(ip, port, databaseName, userName, password);
        return new JdbcTemplate(dataSource);
    }

    private DataSource getHikariDataSource(String ip, String port, String databaseName, String userName, String password) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", ip, port, databaseName));
        hikariDataSource.setUsername(userName);
        hikariDataSource.setPassword(password);
        hikariDataSource.setDriverClassName("org.postgresql.Driver");
        hikariDataSource.setConnectionTestQuery("select 1");
        // 代理方式切换数据库
        return DynamicRefreshProxy.newInstance(hikariDataSource);
    }


}
