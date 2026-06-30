package com.tg.telegram4j.model;

import lombok.Data;

/**
 * 通讯录导入的单条联系人信息。
 */
@Data
public class ImportContactItem {

    /**
     * 手机号（国际格式，如 "+8613800138000"）。
     */
    private String phone;

    /**
     * 姓
     */
    private String firstName;

    /**
     * 名
     */
    private String lastName;
}
