package com.alep.pigbot.entity;

import lombok.Data;

import java.util.List;

@Data
public class MessageData {
    private String type;
    private List<Message> messageChain;
    private Sender sender;
}