package com.alep.pigbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String type;
    private String id;
    private long time;
    //文本消息相关
    private String text;
    //图片消息相关
    private String imageId;
    private String url;
    private String path;
    //戳一戳相关
    private String target;
    private String subject;
    private String kind;
}