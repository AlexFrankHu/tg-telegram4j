package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人导入记录表实体类。
 */
@Data
@TableName("tg_contact_import_record")
public class TgContactImportRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 批次号 */
    private String batchNo;

    /** 联系人手机号 */
    private String phone;

    /** 联系人用户名 */
    private String username;

    /** 是否已使用 */
    private Integer isUsed;

    /** 创建时间 */
    private LocalDateTime createTime;
}
