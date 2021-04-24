package com.coder.lee.postgresmarkdown.model;

import com.coder.lee.postgresmarkdown.enums.TableTypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 2:39
 *
 * @author coderLee23
 */
@Builder
@Data
public class PgTable {

    private String tableName;

    private String tableCatalog;

    private TableTypeEnum tableType;


}
