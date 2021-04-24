package com.coder.lee.postgresmarkdown.model;

import lombok.Builder;
import lombok.Data;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 1:46
 *
 * @author coderLee23
 */
@Builder
@Data
public class PgDatabase {

    private String datname;

    private String owner;

}
