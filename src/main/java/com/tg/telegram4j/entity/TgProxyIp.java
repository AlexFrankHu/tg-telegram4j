package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代理IP表实体类。
 */
@Data
@TableName("tg_proxy_ip")
public class TgProxyIp {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 组号 */
    private String groupNo;

    /** 协议: socks5/socks4/http */
    private String protocol;

    /** 代理地址 */
    private String host;

    /** 代理端口 */
    private Integer port;

    /** 认证用户名 */
    private String username;

    /** 认证密码 */
    private String password;

    /** 完整代理URL */
    private String proxyUrl;

    /** 最大可绑定账号数 */
    private Integer maxBindable;

    /** 当前绑定数 */
    private Integer currentBindCount;

    /** 历史绑定数 */
    private Integer historyBindCount;

    /** 状态 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
