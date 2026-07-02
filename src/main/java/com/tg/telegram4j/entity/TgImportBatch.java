package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号导入批次表实体类。
 */
@Data
@TableName("tg_import_batch")
public class TgImportBatch {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 批次号 */
    private String batchNo;

    /** 批次标题 */
    private String title;

    /** 文件名 */
    private String fileName;

    /** 总数 */
    private Integer totalCount;

    /** 成功数 */
    private Integer successCount;

    /** 失败数 */
    private Integer failedCount;

    /** 等待数 */
    private Integer waitingCount;

    /** 导入时间 */
    private LocalDateTime importTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
