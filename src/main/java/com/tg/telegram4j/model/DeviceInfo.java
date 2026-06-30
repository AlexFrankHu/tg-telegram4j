package com.tg.telegram4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfo {
    private String deviceModel;
    private String systemVersion;
    private String appVersion;
    private String langCode;
    private String systemLangCode;
    private String langPack;
}
