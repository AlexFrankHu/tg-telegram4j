package com.tg.telegram4j.model;

import lombok.Data;

/**
 * 给陌生人发消息的请求参数。
 * 支持通过用户名或手机号定位目标用户（无需先加好友）。
 */
@Data
public class SendStrangerRequest {

    /**
     * 目标用户名（可带或不带 @）。通过用户名发送时必填。
     */
    private String username;

    /**
     * 目标手机号（国际格式，如 "+8613800138000"）。通过手机号发送时必填。
     */
    private String phone;

    /**
     * 通过手机号发送时，导入通讯录用的备注名-姓（可选）。
     */
    private String firstName;

    /**
     * 通过手机号发送时，导入通讯录用的备注名-名（可选）。
     */
    private String lastName;

    /**
     * 消息内容。
     */
    private String text;
}
