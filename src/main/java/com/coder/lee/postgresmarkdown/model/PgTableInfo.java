package com.coder.lee.postgresmarkdown.model;

import lombok.Builder;
import lombok.Data;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 2:56
 *
 * @author coderLee23
 */
@Builder
@Data
public class PgTableInfo {

    private Integer ordinalPosition;
    private String columnName;
    private String udtName;
    private String lengthPrecision;
    private String isNullable;
    private String columnKey;
    private String colDescription;
    private String columnDefault;

}
