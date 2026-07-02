package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主动开场白表实体类。
 */
@Data
@TableName("tg_opening")
public class TgOpening {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 开场白内容(纯文本) */
    private String content;

    /** 是否启用 */
    private Integer isEnabled;

    /** 排序 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
