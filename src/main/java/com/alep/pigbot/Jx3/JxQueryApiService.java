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

    @Override
    public Jx3Response getServerStatus(String server) {
        Map<String, Object> params = new HashMap<>();
        params.put("server", server);
        String rep = HttpUtil.get(SERVER_URL + "/check", params);
        log.info("rep:{}", rep);
        return getResponse(rep);
    }

    @Override
    public Jx3Response getGoldPrice(String server) {
        Map<String, Object> params = new HashMap<>();
        params.put("server", server);
        String rep = HttpUtil.get(SERVER_URL + "/gold", params);
        log.info("rep:{}", rep);
        return getResponse(rep);
    }

    @Override
    public Jx3Response getItemPrice(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        String rep = HttpUtil.get(SERVER_URL + "/price", params);
        log.info("rep:{}", rep);
        return getResponse(rep);
    }

    @Override
    public JSONObject getMacro(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        String rep = HttpUtil.get(SERVER_URL + "/macro", params);
        log.info("rep:{}", rep);
        return getResponseData(rep);
    }

    @Override
    public JSONObject getEquip(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        String rep = HttpUtil.get(SERVER_URL + "/equip", params);
        log.info("rep:{}", rep);
        return getResponseData(rep);
    }

    private Jx3Response getResponse(String rep){
        try{
            Jx3Response respond = JSONObject.parseObject(rep, Jx3Response.class);
            if (respond.getCode() != 200) return null;
            return respond;
        }catch(Exception e){
            log.info("Parse JSON Error :"+rep);
            return null;
        }
    }
    private JSONObject getResponseData(String rep){
        try{
            Jx3Response respond = JSONObject.parseObject(rep, Jx3Response.class);
            if (respond.getCode() != 200) return null;
            return JSONObject.parseObject(respond.getData());
        }catch(Exception e){
            log.info("Parse JSON Error :"+rep);
            return null;
        }
    }

}
