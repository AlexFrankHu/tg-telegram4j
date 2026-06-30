package com.tg.telegram4j.model;

import lombok.Data;

/**
 * 添加好友请求参数。
 * 支持通过用户名(@username)或手机号添加。
 */
@Data
public class AddContactRequest {

    /**
     * 目标用户标识：可以是 username（如 "@zhangsan"）或 Telegram user_id（数字字符串）。
     */
    private String userId;

    /**
     * 目标手机号（国际格式，如 "+8613800138000"）。
     * 如果 userId 为空则通过手机号查找。
     */
    private String phone;

    /**
     * 备注名-姓
     */
    private String firstName;

    /**
     * 备注名-名
     */
    private String lastName;
}
