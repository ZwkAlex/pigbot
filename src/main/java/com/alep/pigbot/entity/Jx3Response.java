package com.alep.pigbot.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class Jx3Response {
    private Integer code;
    private String msg;
    private String data;
    private long time;
}
