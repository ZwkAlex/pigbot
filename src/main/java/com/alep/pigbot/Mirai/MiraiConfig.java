package com.alep.pigbot.Mirai;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alep.pigbot.dao.BotSessionMapper;
import com.alep.pigbot.entity.BotSession;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "mirai")
@Configuration
@Data
@Slf4j
public class MiraiConfig {
    @Resource
    BotSessionMapper botSessionMapper;
    private String qq;
    private String key;
    private String url;
    private String session;


    @PostConstruct
    public void init() {
        log.info("[Mirai] 读取配置文件:url-{},key-{},qq-{}", url, key, qq);
        log.info("[Mirai] 初始化mirai...");
        String res = HttpUtil.get(url + "/about");
        JSONObject resJson = JSONObject.parseObject(res);
        if (resJson.getInteger(MiraiConstant.CODE).equals(MiraiConstant.CodeEnum.SUCCESS.getCode())) {
            log.info("[Mirai] 服务器版本信息:{}", resJson.getJSONObject(MiraiConstant.DATA).toJSONString());
            //判断数据库中有没有session,有直接查出来
            BotSession botSession = botSessionMapper.selectOne(new LambdaQueryWrapper<BotSession>().eq(BotSession::getBotQq, qq));
            if (botSession != null) {
                log.info("[Mirai] 已有session:{}", botSession.getSession());
                //校验是否失效了
                Map<String, Object> bind = new HashMap<>();
                bind.put("sessionKey", botSession.getSession());
                bind.put("qq", qq);
                HttpResponse resBind = HttpUtil.createPost(url + "/bind").body(JSONObject.toJSONString(bind)).execute();
                JSONObject resBindObject = JSONObject.parseObject(resBind.body());
                if (resBindObject.getInteger(MiraiConstant.CODE) == null || !resBindObject.getInteger(MiraiConstant.CODE).equals(MiraiConstant.CodeEnum.SUCCESS.getCode())) {
                    //失效重新认证
                    VerifyAndBindSession();
                } else {
                    this.session = botSession.getSession();
                }
            } else {
                VerifyAndBindSession();
            }
        } else {
            throw new RuntimeException("mirai服务器连接失败。");
        }
    }

    private void VerifyAndBindSession() {
        //验证session
        Map<String, Object> verify = new HashMap<>();
        verify.put("verifyKey", key);
        HttpResponse resp = HttpUtil.createPost(url + "/verify").body(JSONObject.toJSONString(verify)).execute();
        JSONObject resJson = JSONObject.parseObject(resp.body());
        log.info("[Mirai] 验证回复:{}", resJson.toJSONString());
        if (resJson.getInteger(MiraiConstant.CODE).equals(MiraiConstant.CodeEnum.SUCCESS.getCode())) {
            String repSession = resJson.getString("session");
            //绑定session
            Map<String, Object> bind = new HashMap<>();
            bind.put("sessionKey", repSession);
            bind.put("qq", qq);
            HttpResponse resBind = HttpUtil.createPost(url + "/bind").body(JSONObject.toJSONString(bind)).execute();
            JSONObject resBindJson = JSONObject.parseObject(resBind.body());
            log.info("bindRep:{}", resBindJson.toJSONString());
            if (resBindJson.getInteger(MiraiConstant.CODE).equals(MiraiConstant.CodeEnum.SUCCESS.getCode())) {
                //存入数据库
                BotSession botSession = botSessionMapper.selectOne(new LambdaQueryWrapper<BotSession>().eq(BotSession::getBotQq, qq));
                if (botSession != null) {
                    //session失效了
                    log.error("[Mirai] 重新获取session:{},{}", repSession, botSession.getBotQq());
                    this.session = repSession;
                    botSession.setSession(repSession);
                    botSessionMapper.updateById(botSession);
                } else {
                    BotSession newBotSession = BotSession.builder().botQq(qq).session(repSession).build();
                    botSessionMapper.insert(newBotSession);
                    this.session = repSession;
                }
            } else {
                throw new RuntimeException("[Mirai] 认证失败");
            }
        } else {
            throw new RuntimeException("[Mirai] 认证失败");
        }
    }

    private void ReleaseSession(String session ,String qq){
        Map<String, Object> release = new HashMap<>();
        release.put("sessionKey", session);
        release.put("qq", qq);
        HttpResponse resRelease = HttpUtil.createPost(url + "/release").body(JSONObject.toJSONString(release)).execute();
        JSONObject resReleaseJson = JSONObject.parseObject(resRelease.body());
        log.info("[Mirai] 释放session:{}", resReleaseJson.toJSONString());
        botSessionMapper.deleteById(session);
    }

}
