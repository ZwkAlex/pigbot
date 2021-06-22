package com.alep.pigbot.entity;

import lombok.Data;

@Data
public class SocketData {
    long syncId;
    MessageData data;
}
