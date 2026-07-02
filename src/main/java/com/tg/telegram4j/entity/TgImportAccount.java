package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入账号明细表实体类。
 */
@Data
@TableName("tg_import_account")
public class TgImportAccount {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 批次号 */
    private String batchNo;

    /** 手机号 */
    private String phone;

    /** 状态: waiting/online/failed/banned */
    private String status;

    /** 原因 */
    private String reason;

    /** TG用户ID */
    private Long tgUserId;

    /** 昵称 */
    private String nickname;

    /** 用户名 */
    private String username;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 分配的节点ID */
    private String nodeId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
