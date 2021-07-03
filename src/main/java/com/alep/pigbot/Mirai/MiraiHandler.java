package com.alep.pigbot.Mirai;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alep.pigbot.Jx3.Jx3Constant;
import com.alep.pigbot.Jx3.Jx3Handler;
import com.alep.pigbot.dao.QqGroupMapper;
import com.alep.pigbot.entity.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Component
@Slf4j
public class MiraiHandler {

    private static Set<QqGroup> groupSet = new HashSet<>();
    @Resource
    MiraiConfig miraiConfig;
    @Resource
    QqGroupMapper qqGroupMapper;
    @Resource
    Jx3Handler jx3Handler;


    /*
    * WebSocket模块
    * */

    @Bean(name = "qqWebSocketClient")
    private WebSocketClient qqWebSocketClient() {
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://" + miraiConfig.getUrl() + "/message?verifyKey=" + miraiConfig.getKey()+"&&sessionKey="+miraiConfig.getSession()), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log.info("[QQ-Message] 连接成功");
                    groupSet.addAll(qqGroupMapper.selectList(new QueryWrapper<>()));
                }

                @Override
                public void onMessage(String message) {
                    log.info("[QQ-Message] 收到消息:{}", message);
                    SocketData data = JSONObject.parseObject(message, SocketData.class);
                    if(data.getData() == null || data.getData().getSender() == null) {
                        log.info("[QQ-Message] [无法处理的消息] {}", message);
                        return;
                    }
                    switch(data.getData().getType()){
                        case MiraiConstant.MESSAGE_TYPE_FRIEND:
                            log.info("[QQ-Message] Pass private message ");
                            Test(data.getData().getSender().getId());
                            break;
                        case MiraiConstant.MESSAGE_TYPE_GROUP:
                            List<Message> messageChain = data.getData().getMessageChain();
                            messageChain.forEach(messageVo ->{
//                                if (messageVo.getType().equals(MiraiConstant.MESSAGE_TYPE_SOURCE)) {
//                                  预留
//                                }
                                if (messageVo.getType().equals(MiraiConstant.MESSAGE_TYPE_TEXT)) {
                                    String text = messageVo.getText();
                                    //完全匹配
                                    try {
                                        Method allMatchKeyMethod = Jx3Constant.allKey.get(text);
                                        String groupId = data.getData().getSender().getGroup().getId();
                                        if (allMatchKeyMethod != null) {
                                                allMatchKeyMethod.invoke(jx3Handler.getInstance(),groupId);
                                        } else {
                                            if(text.contains(" ")){
                                                for(Map.Entry<String, Method> entry : Jx3Constant.noAllKey.entrySet()){
                                                    if (text.contains(entry.getKey())) {
                                                        entry.getValue().invoke(jx3Handler.getInstance(), groupId, text);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    log.info("[QQ-Message] 退出连接");
                }

                @Override
                public void onError(Exception e) {
                    log.info("[QQ-Message] 连接错误:{}", e.getMessage());
                }
            };
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000 * 60);
                        if (webSocketClient.getReadyState().name().equals(ReadyState.CLOSED.name()) || webSocketClient.getReadyState().name().equals(ReadyState.CLOSING.name())) {
                            log.info("[QQ-Message] 失去连接，尝试重连...");
                            webSocketClient.reconnect();
                        }
                    } catch (Exception e) {
                        log.info("[QQ-Message] WebSocket监控线程出错 {}",e.getMessage());
                    }
                }
            }).start();
            webSocketClient.connect();
            return webSocketClient;
        }catch (URISyntaxException uriSyntaxException){
            uriSyntaxException.printStackTrace();
        }
        return null;
    }



    @Bean(name = "eventSocketClient")
    public WebSocketClient eventSocketClient() {
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://" + miraiConfig.getUrl() + "/event?verifyKey=" + miraiConfig.getKey()+"&&sessionKey="+miraiConfig.getSession()), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info("[QQ-Event] 连接成功");
                }

                @Override
                public void onMessage(String message) {
                    log.info("[QQ-Event] 收到消息:{}", message);
                    EventRequest eventRequest = JSONObject.parseObject(message, EventRequest.class);
                    if(eventRequest.getType()== null) return;
                    switch(eventRequest.getType()){
                        case MiraiConstant.EVENT_TYPE_NEW_FRIEND:
                            if(!eventRequest.getMessage().equals("ceshi")) break;
//                          sendFriendResponse(eventRequest);
                            log.info("[QQ-Event] 除”ceshi“好友申请忽略");
                            break;
                        case MiraiConstant.EVENT_TYPE_NEW_GROUP:
                            sendGroupResponse(eventRequest);
                            break;
                    }

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("[QQ-Event] 退出连接");
                }

                @Override
                public void onError(Exception ex) {
                    log.info("[QQ-Event] 连接错误:{}", ex.getMessage());
                }
            };
            webSocketClient.connect();
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000 * 60);
                        if (webSocketClient.getReadyState().name().equals(ReadyState.CLOSED.name()) || webSocketClient.getReadyState().name().equals(ReadyState.CLOSING.name())) {
                            log.info("[QQ-Event] 失去连接，尝试重连...");
                            webSocketClient.reconnect();
                        }
                    } catch (Exception e) {
                        log.info("[QQ-Event] WebSocket监控线程出错 {}",e.getMessage());
                    }
                }
            }).start();
            return webSocketClient;
        } catch (URISyntaxException uriSyntaxException) {
            uriSyntaxException.printStackTrace();
        }
        return null;

    }


    /*
    * 发送群聊、私聊 回复请求 模块
    * */


    private void Test(String id) {
        try{
//            Jx3Constant.allKey.get("使用说明").invoke(jx3Handler.getInstance(),id);
            Jx3Constant.noAllKey.get("物价").invoke(jx3Handler.getInstance(),id,"鼠金");
        }catch(Exception ignored){}

//        List<Message> messagesList = new ArrayList<>();
//        messagesList.add(Message.builder().type(MiraiConstant.MESSAGE_TYPE_TEXT).text("hello").build());
//        sendFriendMessage(id, messagesList);
    }

    public void sendGroupMessage(String target, List<Message> messageChainList) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionKey", miraiConfig.getSession());
        params.put("target", target);
        params.put("messageChain", messageChainList);
        HttpResponse resVerify = HttpUtil.createPost(miraiConfig.getUrl() + "/sendGroupMessage").body(JSONObject.toJSONString(params)).execute();
        JSONObject resVerifyJson = JSONObject.parseObject(resVerify.body());
        log.info("[发送群聊信息回执] {}", resVerifyJson.toJSONString());
    }

    public void sendFriendMessage(String target, List<Message> messageChainList) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionKey", miraiConfig.getSession());
        params.put("target", target);
        params.put("messageChain", messageChainList);
        HttpResponse resVerify = HttpUtil.createPost(miraiConfig.getUrl() + "/sendFriendMessage").body(JSONObject.toJSONString(params)).execute();
        JSONObject resVerifyJson = JSONObject.parseObject(resVerify.body());
        log.info("[发送私聊信息回执] {}", resVerifyJson.toJSONString());
    }

    private void sendGroupResponse(EventRequest eventRequest){
        EventResponse eventResponse =DefaultEventResponse(eventRequest);
        HttpResponse resVerify = HttpUtil.createPost(miraiConfig.getUrl() + "/resp/botInvitedJoinGroupRequestEvent").body(JSONObject.toJSONString(eventResponse)).execute();
        JSONObject resVerifyJson = JSONObject.parseObject(resVerify.body());
        log.info("[群事件回执] {}", resVerifyJson.toJSONString());
        //存入数据库
        if (qqGroupMapper.selectList(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId, eventRequest.getGroupId().toString())).isEmpty()) {
            QqGroup qqGroup = new QqGroup();
            qqGroup.setGroupId(eventRequest.getGroupId().toString());
            qqGroupMapper.insert(qqGroup);
            groupSet.add(qqGroup);
        }
    }

    private void sendFriendResponse(EventRequest eventRequest) {
        EventResponse eventResponse =DefaultEventResponse(eventRequest);
        log.info(JSONObject.toJSONString(eventResponse));
        HttpResponse resVerify = HttpUtil.createPost(miraiConfig.getUrl() + "/resp/newFriendRequestEvent").body(JSONObject.toJSONString(eventResponse)).execute();
        JSONObject resVerifyJson = JSONObject.parseObject(resVerify.body());
        log.info("[好友事件回执] {}", resVerifyJson.toJSONString());
    }


    /*
     * 其他工具
     * */


    public Set<QqGroup> getGroupSet(){
        return groupSet;
    }

    private EventResponse DefaultEventResponse(EventRequest eventRequest){
        return new EventResponse(miraiConfig.getSession(),
                eventRequest.getEventId(),
                eventRequest.getFromId(),
                eventRequest.getGroupId(),
                0,
                ""
        );
    }
}
