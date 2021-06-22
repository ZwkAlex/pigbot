package com.alep.pigbot.entity;

import lombok.Data;

@Data
public class EventRequest {
    private String type;
    private Long eventId;
    private Long fromId;
    private Long groupId;
    private String nick;
    private String message;
    private String groupName;
}