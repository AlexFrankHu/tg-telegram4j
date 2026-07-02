package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代理IP组表实体类。
 */
@Data
@TableName("tg_proxy_group")
public class TgProxyGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 组号 */
    private String groupNo;

    /** 标题 */
    private String title;

    /** 国家 */
    private String country;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 最大可绑定数 */
    private Integer maxBindable;

    /** 总数 */
    private Integer totalCount;

    /** 导入时间 */
    private LocalDateTime importTime;

    /** 组名 */
    private String groupName;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
