package com.alep.pigbot.Jx3;


import cn.hutool.http.HttpUtil;
import com.alep.pigbot.entity.Jx3Response;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JxQueryApiService implements JxQueryApiModel{

    private static final String SERVER_URL = "https://jx3api.com/app";
    private static final String RandomTalk_SERVER_URL = "https://api.shadiao.app";

    @Override
    public Jx3Response getServerStatus(String server) {
        Map<String, Object> params = new HashMap<>();
        params.put("server", server);
        String rep = HttpUtil.get(SERVER_URL + "/check", params);
        log.info("[服务器] rep:{}", rep);
        return getResponse(rep);
    }

    @Override
    public Jx3Response getGoldPrice(String server) {
        Map<String, Object> params = new HashMap<>();
        params.put("server", server);
        String rep = HttpUtil.get(SERVER_URL + "/gold", params);
        log.info("[金价] rep:{}", rep);
        return getResponse(rep);
    }

    @Override
    public Jx3Response getItemPrice(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        String rep = HttpUtil.get(SERVER_URL + "/price", params);
        log.info("[物价] rep:{}", rep);
        return getResponse(rep);
    }

    @Override
    public Jx3Response getSerendipity(String server, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("server", server);
        params.put("name", name);
        String rep = HttpUtil.get(SERVER_URL + "/serendipity", params);
        log.info("[奇遇查询] rep:{}", rep);
        return getResponse(rep);
    }

    @Override
    public JSONObject getMacro(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        String rep = HttpUtil.get(SERVER_URL + "/macro", params);
        log.info("[奇穴] rep:{}", rep);
        return getResponseData(rep);
    }

    @Override
    public JSONObject getEquip(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        String rep = HttpUtil.get(SERVER_URL + "/equip", params);
        log.info("[配装] rep:{}", rep);
        return getResponseData(rep);
    }

    @Override
    public JSONObject getRandomTalk(int type){
        try{
            JSONObject rep = null;
            String repString ;
            switch(type){
                case -1:
                    return null;
                case 0://骚话
                    repString = HttpUtil.get(SERVER_URL+"/random");
                    rep = getResponseData(repString);
                    log.info("[骚话] rep:{}", repString);
                    break;
                case 1://彩虹屁
                    repString = HttpUtil.get(RandomTalk_SERVER_URL+"/chp");
                    rep = JSONObject.parseObject(repString).getJSONObject("data");
                    log.info("[骚话] rep:{}", repString);
                    break;
                case 2://朋友圈
                    repString = HttpUtil.get(RandomTalk_SERVER_URL+"/pyq");
                    rep = JSONObject.parseObject(repString).getJSONObject("data");
                    log.info("[骚话] rep:{}", repString);
                    break;
                case 3://毒鸡汤
                    repString = HttpUtil.get(RandomTalk_SERVER_URL+"/du");
                    rep = JSONObject.parseObject(repString).getJSONObject("data");
                    log.info("[骚话] rep:{}", repString);
                    break;
                case 4://祖安
                    repString = HttpUtil.get(RandomTalk_SERVER_URL+"/nmsl?level=min");
                    rep = JSONObject.parseObject(repString).getJSONObject("data");
                    log.info("[骚话] rep:{}", repString);
                    break;

            }
            return rep;
        }catch(Exception e){
            log.info("[骚话] 获取骚话失败");
            return null;
        }
    }

    private Jx3Response getResponse(String rep){
        try{
            Jx3Response respond = JSONObject.parseObject(rep, Jx3Response.class);
            if (respond.getCode() != 200) return null;
            return respond;
        }catch(Exception e){
            log.info("[Error] Parse JSON Error :"+rep);
            return null;
        }
    }
    private JSONObject getResponseData(String rep){
        try{
            Jx3Response respond = JSONObject.parseObject(rep, Jx3Response.class);
            if (respond.getCode() != 200) return null;
            return JSONObject.parseObject(respond.getData());
        }catch(Exception e){
            log.info("[Error] Parse JSON Error :"+rep);
            return null;
        }
    }


}
