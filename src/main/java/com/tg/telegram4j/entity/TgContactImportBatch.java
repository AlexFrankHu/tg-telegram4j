package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人导入批次表实体类。
 */
@Data
@TableName("tg_contact_import_batch")
public class TgContactImportBatch {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 批次号 */
    private String batchNo;

    /** 标题 */
    private String title;

    /** 导入类型: phone/username */
    private String importType;

    /** 文件名 */
    private String fileName;

    /** 总数 */
    private Integer totalCount;

    /** 已用数 */
    private Integer usedCount;

    /** 等待数 */
    private Integer waitingCount;

    /** 无效数 */
    private Integer invalidCount;

    /** 导入时间 */
    private LocalDateTime importTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
