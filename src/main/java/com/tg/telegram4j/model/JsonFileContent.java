package com.tg.telegram4j.model;

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

}
