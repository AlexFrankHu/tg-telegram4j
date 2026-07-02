package com.tg.telegram4j.model;

<<<<<<< Updated upstream
import com.alibaba.fastjson2.JSONObject;
import com.tg.telegram4j.utils.JsonUtil;
import lombok.Data;

/**
 * 账号JSON文件内容解析类。
 */
@Data
public class JsonFileContent {

    /** session文件名 */
    private String sessionFile;

    /** 手机号 */
    private String phone;

    /** API ID */
    private Integer appId;

    /** API Hash */
    private String appHash;

    /** SDK/系统版本描述 */
    private String sdk;

    /** APP版本 */
    private String appVersion;

    /** 设备型号 */
    private String device;

    /** 语言包 */
    private String langPack;

    /** 系统语言包 */
    private String systemLangPack;

    /** 用户名 */
    private String username;

    /** 是否启用IPv6 */
    private Boolean ipv6;

    /** 名 */
    private String firstName;

    /** 姓 */
    private String lastName;

    /** 注册时间(时间戳) */
    private Long registerTime;

    /** 性别 */
    private String sex;

    /** 最后检查时间(时间戳) */
    private Long lastCheckTime;

    /** 语言代码 */
    private String langCode;

    /** 头像路径 */
    private String avatar;

    /** 代理信息 */
    private String proxy;

    /** 二步验证密码 */
    private String twoFA;

    /** 是否被封 */
    private Boolean block;

    /** 系统语言代码 */
    private String systemLangCode;

    /** Telegram用户ID */
    private Long id;

    /**
     * 通过JSON字符串构造。
     */
    public JsonFileContent(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return;
        }
        JSONObject json = JsonUtil.parseObject(jsonStr);
        this.sessionFile = JsonUtil.getString(json, "session_file");
        this.phone = JsonUtil.getString(json, "phone");
        this.appId = JsonUtil.getInt(json, "app_id");
        this.appHash = JsonUtil.getString(json, "app_hash");
        this.sdk = JsonUtil.getString(json, "sdk");
        this.appVersion = JsonUtil.getString(json, "app_version");
        this.device = JsonUtil.getString(json, "device");
        this.langPack = JsonUtil.getString(json, "lang_pack");
        this.systemLangPack = JsonUtil.getString(json, "system_lang_pack");
        this.username = JsonUtil.getString(json, "username");
        this.ipv6 = JsonUtil.getBoolean(json, "ipv6");
        this.firstName = JsonUtil.getString(json, "first_name");
        this.lastName = JsonUtil.getString(json, "last_name");
        this.registerTime = JsonUtil.getLong(json, "register_time");
        this.sex = JsonUtil.getString(json, "sex");
        this.lastCheckTime = JsonUtil.getLong(json, "last_check_time");
        this.langCode = JsonUtil.getString(json, "lang_code");
        this.avatar = JsonUtil.getString(json, "avatar");
        this.proxy = JsonUtil.getString(json, "proxy");
        this.twoFA = JsonUtil.getString(json, "twoFA");
        this.block = JsonUtil.getBoolean(json, "block");
        this.systemLangCode = JsonUtil.getString(json, "system_lang_code");
        this.id = JsonUtil.getLong(json, "id");
    }
=======
import lombok.Data;

@Data
public class JsonFileContent {

    private boolean hasData = false;



    public JsonFileContent(String content) {
        if (content == null || content.trim().length() == 0) {
            return ;
        }
        try {


            hasData = true;
        } catch (Exception e) {
            e.printStackTrace();
            hasData = false;
        }
    }

>>>>>>> Stashed changes
}
