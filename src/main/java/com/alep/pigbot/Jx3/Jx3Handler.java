package com.alep.pigbot.Jx3;

import com.alep.pigbot.Mirai.MiraiConstant;
import com.alep.pigbot.Mirai.MiraiHandler;
import com.alep.pigbot.dao.QqGroupMapper;
import com.alep.pigbot.entity.Jx3Response;
import com.alep.pigbot.entity.Jx3SocketData;
import com.alep.pigbot.entity.Message;
import com.alep.pigbot.entity.QqGroup;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class Jx3Handler {


    @Resource
    QqGroupMapper qqGroupMapper;
    @Resource
    JxQueryApiService jxQueryApiService;
    @Resource
    MiraiHandler miraHandler;

    /*
    * WebSocket 模块
    * */
    @Bean(name = "jx3WebSocketClient")
    public WebSocketClient jx3WebSocketClient() {
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("wss://socket.nicemoe.cn"), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshakeData) {
                    log.info("[JX3API] 连接成功");
                }

                @Override
                public void onMessage(String message) {
                    Jx3SocketData msg = JSONObject.parseObject(message, Jx3SocketData.class);
                    if(msg == null|| msg.getType() == null ){
                        log.info("[JX3API] [无法处理的消息] {}", message);
                        return;
                    }
                    //过滤2003奇遇播报log
                    if(msg.getType()!=2003) log.info("[JX3API] 收到消息:{}", msg);
                    switch(msg.getType()){
                        case 2001:
                            log.info("[JX3API] 开服数据:{}", msg);
                            //开服播报
                            List<Message> messagesList = new ArrayList<>();
                            if (msg.getData().getInteger("status") == 1) {
                                String s = TimestampToStringDetail(-1) +
                                        " " +msg.getData().getString("server") +
                                        "【开服】";
                                messagesList.add(Message.builder().type(MiraiConstant.MESSAGE_TYPE_TEXT).text(s).build());
                            }
                            miraHandler.getGroupSet().forEach(group -> {
                                if (msg.getData().getString("server").equals(group.getServer())) {
                                    miraHandler.sendGroupMessage(group.getGroupId(), messagesList);
                                }
                            });
                            break;
                        case 2003:
                            break;
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("[JX3API] 退出连接");
                }

                @Override
                public void onError(Exception ex) {
                    log.info("[JX3API] 连接错误:{}", ex.getMessage());
                }
            };

            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000 * 60);
                        if (webSocketClient.getReadyState().name().equals(ReadyState.CLOSED.name()) || webSocketClient.getReadyState().name().equals(ReadyState.CLOSING.name())) {
                            log.info("[JX3API] 失去连接，尝试重连...");
                            webSocketClient.reconnect();
                        }
                    } catch (Exception e) {
                        log.info("[JX3API] WebSocket监控线程出错 {}",e.getMessage());
                    }
                }
            }).start();
            //如果断线，则重连并重新发送验证信息
            webSocketClient.connect();
            return webSocketClient;
        } catch (URISyntaxException uriSyntaxException) {
            uriSyntaxException.printStackTrace();
        }
        return null;

    }

    /*
     * 业务模块
     * */

    //All Key Service
    public void getServerStatus(String groupId) {
        QqGroup qqGroup = qqGroupMapper.selectOne(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId, groupId));
        StringBuilder respText = new StringBuilder();
        if (qqGroup == null) {
            respText.append("没有本群服务器信息！请使用→ 服务器 服务器名 格式绑定本群服务器。");
        }else{
            Jx3Response serverStatus = jxQueryApiService.getServerStatus(qqGroup.getServer());
            respText.append(TimestampToStringDetail(serverStatus.getTime())).append(" - ").append(qqGroup.getServer());
            if (JSONObject.parseObject(serverStatus.getData()).getInteger("status") == 1) {
                respText.append(" - 已开服");
            } else {
                respText.append( " - 正在维护");
            }
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BulidTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getBindServer(String groupId){
        QqGroup qqGroup = qqGroupMapper.selectOne(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId,groupId));
        String respText;
        if (qqGroup == null) {
            respText = "没有本群服务器信息！请使用→ 服务器 服务器名 格式绑定本群服务器。";
        }else{
            respText = "本群绑定服务器为 【" + qqGroup.getServer() + "】";
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BulidTextMessage(respText));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getGoldPrice(String groupId){
        QqGroup qqGroup = qqGroupMapper.selectOne(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId, groupId));
        StringBuilder respText = new StringBuilder();
        if (qqGroup == null) {
            respText = new StringBuilder("没有本群服务器信息！请使用→ 服务器 服务器名 格式绑定本群服务器。");
        }else{
            Jx3Response goldPrice =jxQueryApiService.getGoldPrice(qqGroup.getServer());
            respText.append(TimestampToString(goldPrice.getTime()))
                    .append("\n服务器【")
                    .append(qqGroup.getServer())
                    .append("】金价：\n");
            for( Map.Entry<String, String> entry :Jx3Constant.Gold.entrySet()){
                JSONObject data=JSONObject.parseObject(goldPrice.getData());
                if(data.containsKey(entry.getKey())){
                    respText.append(String.format("%-9s", entry.getValue()+":"))
                            .append(data.getString(entry.getKey()))
                            .append("\n");
                }
            }
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BulidTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getManual(String groupId){
        StringBuilder respText = new StringBuilder();
        respText.append("目前包含以下功能:\n").append("- 以下功能无需提供目标\n");
        for(String key:Jx3Constant.allKey.keySet()){
            respText.append(key).append("\n");
        }
        respText.append("- 以下功能必须提供目标: 格式[关键词 目标]\n").append("- 必须包含空格\n");
        for(String key:Jx3Constant.noAllKey.keySet()){
            respText.append(key).append(" 目标").append("\n");
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BulidTextMessage(respText.toString()));
        miraHandler.sendFriendMessage(groupId, messagesList);
    }



    //No ALL Key Service
    public void setBindServer(String groupId,String text) {
        String server = TextExtract(text,"绑定服务器");
        String respText;
        if(isServerName(server)){
            server = getRealServerName(server);
            QqGroup qqGroup = qqGroupMapper.selectOne(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId, groupId));
            if (qqGroup == null) {
                qqGroup = new QqGroup();
                qqGroup.setGroupId(groupId);
                qqGroup.setServer(server);
                qqGroupMapper.insert(qqGroup);
                respText = "绑定群号：" + groupId + "\n绑定服务器： " + server;
            } else {
                qqGroup.setServer(server);
                qqGroupMapper.updateById(qqGroup);
                respText = "切换当前服务器至【" + qqGroup.getServer() + "】";
            }
        }else{
            respText = "绑定服务器失败";
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BulidTextMessage(respText));
        miraHandler.sendGroupMessage(groupId, messagesList);

    }

    public void getItemPrice(String groupId,String text){
        String item = TextExtract(text,"物价");
        StringBuilder respText = new StringBuilder();
        Jx3Response itemPrice =jxQueryApiService.getItemPrice(item);
        try{
            JSONArray priceList = JSONArray.parseArray(itemPrice.getData());
            StringBuilder itemname = new StringBuilder();
            for(Object a : priceList){
                respText.append("\n");
                for(Object b :JSONArray.parseArray(a.toString())){
                    JSONObject data = JSONObject.parseObject(b.toString());
                    if(itemname.toString().equals(""))
                        itemname.append(data.getString("exterior"))
                                .append("-")
                                .append(data.getString("itemname"))
                                .append("\n");
                    respText.append(data.getString("regionAlias"))
                            .append("\t").append(String.format("%-8s",data.getString("server")))
                            .append("\t").append(data.getString("price"))
                            .append("\t").append(data.getString("saleCode"))
                            .append("\t").append("(")
                            .append(data.getString("tradeTime"))
                            .append(")").append("\n");
                }
            }
            respText.insert(0,TimestampToString(itemPrice.getTime())+"\n"+itemname);
        }catch(Exception e){
            respText.append("没有找到物品【").append(item).append("】的价格。或许名称不正确");
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BulidTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getMarco(String groupId,String text){
        String name = TextExtract(text,"宏");
        StringBuilder respText = new StringBuilder();
        if(isJxName(name)){
            name = getRealJxName(name);
            JSONObject macro = jxQueryApiService.getMacro(name);
            if(macro != null){
                respText.append("心法:").append(macro.getString("name"))
                        .append("\n奇穴:\n").append(macro.getString("plan"))
                        .append("\n宏:\n").append(macro.getString("command"));
            }else{
                respText.append("找不到【").append(name).append("】的宏，请用正确的全称。");
            }
        }else{
            respText.append("【").append(name).append("】不是正确的心法称呼");
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BulidTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getEquip(String groupId,String text) {
        String name = TextExtract(text,"配装");
        StringBuilder respText = new StringBuilder();
        List<Message> messagesList = new ArrayList<>();
        if(isJxName(name)) {
            name = getRealJxName(name);
            if(text.contains("pve")||text.contains("PVE")){
                JSONObject equip = jxQueryApiService.getEquip(name);
                if(equip != null){
                    messagesList.add(Message.builder().type(MiraiConstant.MESSAGE_TYPE_IMG).url(equip.getString("pveUrl")).build());
                }else{
                    respText.append("找不到【").append(text).append("】的PVE配装，请用正确的全称");
                }
            }else if(text.contains("pvp")||text.contains("PVP")){
                JSONObject equip = jxQueryApiService.getEquip(name);
                if(equip != null){
                    messagesList.add(Message.builder().type(MiraiConstant.MESSAGE_TYPE_IMG).url(equip.getString("pvpUrl")).build());
                }else{
                    respText.append("找不到【").append(text).append("】的PVP配装，请用正确的全称");
                }
            }else{
                respText.append("请指定PVE或PVP配装");
            }
        }else{
            respText.append("【").append(text).append("】不是正确的心法称呼");
        }

        messagesList.add(BulidTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    /*
    * 其他工具
    * */

    private static String TimestampToString(long ts){
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        if(ts == -1){
            return df.format(new Date());
        }
        return df.format(ts*1000);
    }

    private static String TimestampToStringDetail(long ts){
        DateFormat df = new SimpleDateFormat("MM/dd HH:mm");
        if(ts == -1){
            return df.format(new Date());
        }
        return df.format(ts*1000);
    }

    private String TextExtract(String text,String head){
        return text.replace(head,"").
                replace("\n","")
                .replace("\t","")
                .replace(" ","")
                .replace("pve","")
                .replace("PVE","")
                .replace("pvp","")
                .replace("PVP","");
    }

    private boolean isServerName(String server) {
        for(Jx3Constant.ServerEnum e: Jx3Constant.ServerEnum.values()){
            if(e.getName().equals(server)){
                return true;
            }
            for(String alter:e.getAlterNames()){
                if(alter.equals(server)){
                    return true;
                }
            }
        }
        return false;
    }

    private String getRealServerName(String server) {
        for(Jx3Constant.ServerEnum e: Jx3Constant.ServerEnum.values()){
            if(e.getName().equals(server)){
                return server;
            }else{
                for(String alter:e.getAlterNames()){
                    if(alter.equals(server)){
                        return e.getName();
                    }
                }
            }
        }
        return null;
    }

    private boolean isJxName(String name) {
        for(Jx3Constant.Jx3NameEnum e: Jx3Constant.Jx3NameEnum.values()){
            if(e.getName().equals(name)){
                return true;
            }
            for(String alter:e.getAlterNames()){
                if(alter.equals(name)){
                    return true;
                }
            }
        }
        return false;
    }


    private String getRealJxName(String jxName) {
        for(Jx3Constant.Jx3NameEnum e: Jx3Constant.Jx3NameEnum.values()){
            if(e.getName().equals(jxName)){
                return jxName;
            }else{
                for(String alter:e.getAlterNames()){
                    if(alter.equals(jxName)){
                        return e.getName();
                    }
                }
            }
        }
        return null;
    }
    private Message BulidTextMessage(String text){
        return Message.builder().type(MiraiConstant.MESSAGE_TYPE_TEXT).text(text).build();
    }

    public Jx3Handler getInstance(){
        return this;
    }

}
