package com.tg.telegram4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfo {
    private String sessionName;
    private long userId;
    private String firstName;
    private String lastName;
    private String username;
    private String phone;
    private boolean connected;
}
