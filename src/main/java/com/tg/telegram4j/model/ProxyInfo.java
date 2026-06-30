package com.tg.telegram4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProxyInfo {
    private String host;
    private int port;
    private String username;
    private String password;
}
