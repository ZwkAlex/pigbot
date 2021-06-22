package com.alep.pigbot.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class Jx3SocketData {
    private Integer type;
    private JSONObject data;
    private long echo;
}