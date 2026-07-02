package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 集群节点信息表实体类。
 */
@Data
@TableName("tg_cluster_node")
public class TgClusterNode {

    /** 节点ID (MD5中间16位) */
    @TableId
    private String nodeId;

    /** 节点公网IP */
    private String publicIp;

    /** 节点内网IP */
    private String privateIp;

    /** 历史总账号数(分配到该节点的) */
    private Integer totalAccountCount;

    /** 当前在线账号数 */
    private Integer onlineAccountCount;

    /** 节点目录 */
    private String nodeDir;

    /** 节点端口 */
    private Integer nodePort;

    /** 节点状态(1开启 0关闭) */
    private String nodeStatus;

    /** 最后活跃时间 */
    private Date lastActiveTime;

    /** 最大账号数 */
    private Integer maxAccountCount;

    /** 创建时间 */
    private Date createTime;

    private Date updateTime;

    /** 节点类型 */
    private String nodeType;
}
