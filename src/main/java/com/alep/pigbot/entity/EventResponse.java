package com.alep.pigbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventResponse {
    private String sessionKey;
    private Long eventId;
    private Long fromId;
    private Long groupId;
    private Integer operate;
    private String message;

}