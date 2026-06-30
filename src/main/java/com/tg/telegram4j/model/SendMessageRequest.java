package com.tg.telegram4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private String chatId;
    private String text;
    private String imageUrl;
    private byte[] imageData;
    private String imageFileName;
    private String caption;
}
