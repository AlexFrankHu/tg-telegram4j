package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志表实体类。
 */
@Data
@TableName("tg_login_log")
public class TgLoginLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 手机号 */
    private String phone;

    /** 登录结果: success/failed/banned/logout */
    private String result;

    /** 失败原因 */
    private String reason;

    /** Telegram用户ID */
    private Long tgUserId;

    /** 昵称 */
    private String nickname;

    /** 代理信息 */
    private String proxyInfo;

    /** 节点ID */
    private String nodeId;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
