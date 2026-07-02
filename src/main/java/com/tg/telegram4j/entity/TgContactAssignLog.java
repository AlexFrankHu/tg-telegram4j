package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友分配日志表实体类。
 */
@Data
@TableName("tg_contact_assign_log")
public class TgContactAssignLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 账号批次号 */
    private String accountBatchNo;

    /** 账号批次标题 */
    private String accountBatchTitle;

    /** 账号ID */
    private Integer accountId;

    /** 分配给的账号ID */
    private Integer tgAccountId;

    /** 账号手机号 */
    private String accountPhone;

    /** 好友批次号 */
    private String contactBatchNo;

    /** 好友批次标题 */
    private String contactBatchTitle;

    /** 待添加好友手机号 */
    private String contactPhone;

    /** 待添加好友用户名 */
    private String contactUsername;

    /** 导入类型: phone/username */
    private String importType;

    /** 联系人导入批次号 */
    private String batchNo;

    /** 状态: pending/processing/success/failed */
    private String status;

    /** 重试次数 */
    private Integer retryCount;

    /** 添加成功后的TG用户ID */
    private Long resultUserId;

    /** 失败原因 */
    private String errorReason;

    /** 备注 */
    private String remark;

    /** 添加方式(one_by_one逐个添加 contact_import联系人导入) */
    private String addMethod;

    /** 账号所属节点ID */
    private String nodeId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
