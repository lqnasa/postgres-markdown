# 快速导出Postgres表结构生成MarkDown文档

## 一、背景

在实施软件工程的时候，当要将某一版本归档时，需要汇总的文档要求还是比较高的、各类文档齐全，包括项目架构、项目安装、接口等文档，而数据库表结构说明文档亦属于其一为了维护文档方便，有必要开一个简单的工具，可以很容易的导出 `markdown` 文档。

于是 `postgres_markdown` 就出现了、应用而生。它是一款基于 `java` 语言编写的一个工具。**那么它可以做什么？他只有一个功能、就是生成数据库表结构说明文档，格式为`markdown`**。

当然其他语言比如go、python也可以很简单的编写一个工具。（go语言版的是修改别人的mysql-markdown，修改后的代码看起来比较凌乱……）

python实现也不会复杂，有时间可以写一下。java依靠强大的springboot和swagger3，开发起来也简单。

## 二、开发环境

JDK 1.8.131

1、spring-boot-starter-web

2、postgresql

3、lombok

4、springfox-boot-starter

5、knife4j-spring-boot-starter

6、spring-boot-starter-data-jdbc

7、spring-boot-starter-actuator (健康监控)

以上为项目的核心包

使用knife4j+swagger3省去前端编写，舒服。界面美观。

使用spring-data-jdbc 原因是怎么简单怎么来，对于多表关联查询很容易写出。

## 三、案例目标

简单输入数据库信息，执行请求便可快速导出该用户所有的数据库及其表结构信息生成markdown文档。

## 四、技术实现

1、Application中添加`@EnableOpenApi`和`@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})`

将DataSourceAutoConfiguration的在启动时候exclude掉，避免初始化因未配置数据库信息，导致错误。

```java
@EnableOpenApi
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PostgresMarkdownApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgresMarkdownApplication.class, args);
    }
}
```

2、controller层,添加swagger注解。

```java
@GetMapping("/generator")
@ApiImplicitParams({
        @ApiImplicitParam(name = "ip", value = "数据库地址", defaultValue = "localhost", required = true),
        @ApiImplicitParam(name = "port", value = "端口", defaultValue = "5432", required = true),
        @ApiImplicitParam(name = "databaseName", value = "数据库名", required = true),
        @ApiImplicitParam(name = "userName", value = "用户名", defaultValue = "root", required = true),
        @ApiImplicitParam(name = "password", value = "密码", defaultValue = "root", required = true)
})
public String generatorDatabaseDoc(@RequestParam(defaultValue = "localhost") String ip,
                                   @RequestParam(defaultValue = "5432") String port,
                                   @RequestParam(defaultValue = "postgres") String databaseName,
                                   @RequestParam(defaultValue = "root") String userName,
                                   @RequestParam(defaultValue = "root") String password) {
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
```

3、根据请求的数据库信息，动态创建jdbcTemplate，`难点在于动态切换数据库，由于springboot默认使用HikariDataSource数据源，导致不能动态直接切换，这里使用了动态代理方式来切换数据库。`

具体可以看 https://github.com/shenjianeng/dynamic-change-data-source 中的分析

```java
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
```

4、生成markdown文件的字符串结构

```java
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

```

获取数据源的所有表,jdbcTemplate语法还是比较简洁，需要注意一点如果使用` queryForStream 需要使用 try-with-resources 来释放连接`，不然会导致连接无法释放问题。

获取表字段信息代码雷同，就不贴了。具体看源码。

```java
public List<PgTable> getPgTableList(JdbcTemplate jdbcTemplate, TableTypeEnum tableTypeEnum) {
    Assert.notNull(jdbcTemplate, "jdbcTemplate must not be null");
    Assert.notNull(tableTypeEnum, "tableTypeEnum must not be null");
  // queryForStream 需要使用 try-with-resources 来释放连接
  // @return the result Stream, containing mapped objects, needing to be closed once fully processed (e.g. through a try-with-resources clause)
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
```

5、生成markdown文档

```java
private void writeMarkdownFile(String fileName, String str) {
    try {
        Files.write(Paths.get(SAVE_FILE_PATH, String.format("%s.md", fileName)), str.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
        LOGGER.error("写入文档失败！", e);
    }
}
```

6、实测效果

输入数据库访问信息

![image-20210424155455267](https://raw.githubusercontent.com/lqnasa/postgres-markdown/master/docs/images/image-20210424155455267.png)

执行生成markdown文档

![image-20210424155432089](https://raw.githubusercontent.com/lqnasa/postgres-markdown/master/docs/images/image-20210424155432089.png)

# 五、总结

Java实现导出Postgresql表结构生成MarkDown文档还是比较简单的。难度在于springboot切换数据库上比较麻烦。
源码访问：https://github.com/lqnasa/postgres-markdown

使用不足：

​       go编译成exe是最轻量，简单，原生命令支持。（`go build xxx`）

​        python编写开发会比go更快，编译exe不如go简洁，执行过程相对慢。（`pyinstaller`）

​		java打成exe相对麻烦。包会比较大。（`exe4j`）
