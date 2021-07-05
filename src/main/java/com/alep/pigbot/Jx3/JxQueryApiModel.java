package com.alep.pigbot.Jx3;

import com.alep.pigbot.entity.Jx3Response;
import com.alibaba.fastjson.JSONObject;

public interface JxQueryApiModel {
    //需要获取当前时间 返回Jx3Response
    Jx3Response getServerStatus(String server);
    Jx3Response getGoldPrice(String server);
    Jx3Response getItemPrice(String name);

    //不需要当前时间 返回JSONObject
    JSONObject getMacro(String name);
    JSONObject getEquip(String name);

    //其他
    JSONObject getRandomTalk(int type);
}
