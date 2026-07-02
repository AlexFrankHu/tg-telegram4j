package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代理分配日志表实体类。
 */
@Data
@TableName("tg_proxy_assign_log")
public class TgProxyAssignLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 账号批次号 */
    private String accountBatchNo;

    /** 账号批次标题 */
    private String accountBatchTitle;

    /** 账号ID */
    private Integer accountId;

    /** 账号ID */
    private Integer tgAccountId;

    /** 账号手机号 */
    private String accountPhone;

    /** 代理IP的ID */
    private Integer proxyIpId;

    /** 代理URL */
    private String proxyUrl;

    /** 代理组号 */
    private String proxyGroupNo;

    /** 代理组标题 */
    private String proxyGroupTitle;

    /** 分配类型: auto/manual/config */
    private String assignType;

    /** 账号所属节点ID */
    private String nodeId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
