package com.coder.lee.postgresmarkdown.enums;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 2:44
 *
 * @author coderLee23
 */
public enum TableTypeEnum {
    /**
     * base table
     */
    BASE_TABLE("BASE TABLE"),
    /**
     * view 视图
     */
    VIEW("VIEW");

    private String name;

    TableTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TableTypeEnum valueOfByName(String name) {
        for (TableTypeEnum value : TableTypeEnum.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
