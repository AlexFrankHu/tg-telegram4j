package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 广告问候语表实体类。
 */
@Data
@TableName("tg_greeting")
public class TgGreeting {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 标题 */
    private String title;

    /** 问候语内容 */
    private String content;

    /** 状态 */
    private String state;

    /** 图片路径(可选) */
    private String imagePath;

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
