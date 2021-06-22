package com.alep.pigbot.entity;

import lombok.Data;

@Data
public class Sender {
    private String id;
    private String memberName;
    private String permission;
    private GroupInfo group;
}