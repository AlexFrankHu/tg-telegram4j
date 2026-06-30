package com.tg.telegram4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLoginRequest {
    private String sessionName;
    private byte[] sessionData;
    private ProxyInfo proxy;
    private DeviceInfo device;
    @Builder.Default
    private boolean autoReadMessages = false;
    private Integer apiId;
    private String apiHash;
}
