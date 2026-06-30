package com.tg.telegram4j.model;

import lombok.Data;

import java.util.List;

/**
 * 批量导入通讯录请求参数。
 */
@Data
public class ImportContactsRequest {

    /**
     * 待导入的联系人列表。
     */
    private List<ImportContactItem> contacts;
}
