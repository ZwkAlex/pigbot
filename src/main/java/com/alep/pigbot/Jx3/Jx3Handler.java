package com.alep.pigbot.Jx3;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alep.pigbot.Mirai.MiraiConstant;
import com.alep.pigbot.Mirai.MiraiHandler;
import com.alep.pigbot.dao.QqGroupMapper;
import com.alep.pigbot.entity.*;
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

    private static Map<String, RandomTalkSetting> randomTalk = new HashMap<>();
    @Resource
    QqGroupMapper qqGroupMapper;
    @Resource
    JxQueryApiService jxQueryApiService;
    @Resource
    MiraiHandler miraHandler;
    @Resource
    Jx3PicCompiler jx3PicCompiler;

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
                                String s = TimestampToString(-1, Jx3Constant.DayPattern.DETAIL.getPattern()) +
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
                        log.info("[JX3API] WebSocket监控线程Error {}",e.getMessage());
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
            respText.append(TimestampToString(serverStatus.getTime(),Jx3Constant.DayPattern.SHORT.getPattern()))
                    .append(" - ").append(qqGroup.getServer());
            if (JSONObject.parseObject(serverStatus.getData()).getInteger("status") == 1) {
                respText.append(" - 已开服");
            } else {
                respText.append( " - 正在维护");
            }
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BuildTextMessage(respText.toString()));
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
        messagesList.add(BuildTextMessage(respText));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getGoldPrice(String groupId){
        QqGroup qqGroup = qqGroupMapper.selectOne(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId, groupId));
        /*
        //旧版文字回复
        StringBuilder respText = new StringBuilder();
        if (qqGroup == null) {
            respText = new StringBuilder("没有本群服务器信息！请使用→ 服务器 服务器名 格式绑定本群服务器。");
        }else{
            Jx3Response goldPrice =jxQueryApiService.getGoldPrice(qqGroup.getServer());
            respText.append(TimestampToString(goldPrice.getTime(),Jx3Constant.DayPattern.LONG.getPattern()))
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
        messagesList.add(BuildTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
        */
        List<Message> messagesList = new ArrayList<>();
        if (qqGroup == null) {
            messagesList.add(BuildTextMessage("没有本群服务器信息！请使用[ 服务器 服务器名 ]格式绑定本群服务器。"));
        }else{
            Jx3Response goldPrice =jxQueryApiService.getGoldPrice(qqGroup.getServer());
            String url = jx3PicCompiler.GoldPriceImageCompiler(goldPrice);
            messagesList.add(BuildImageMessage(url));
        }
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getFunction(String groupId){
        /*
        //旧版文字回复
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
        messagesList.add(BuildTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
         */
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BuildImageMessage(jx3PicCompiler.FunctionPicCompiler()));
        miraHandler.sendGroupMessage(groupId, messagesList);
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
                qqGroup.setRandomTalk(1);
                qqGroupMapper.insert(qqGroup);
                respText = "绑定群号：" + groupId + "\n绑定服务器： " + server;
            } else {
                qqGroup.setServer(server);
                qqGroupMapper.updateById(qqGroup);
                respText = "切换当前服务器至【" + qqGroup.getServer() + "】";
            }
        }else{
            respText = server + "服务器名称未收录，请尽量使用全称";
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BuildTextMessage(respText));
        miraHandler.sendGroupMessage(groupId, messagesList);

    }

    public void getItemPrice(String groupId,String text){
        String item = TextExtract(text,"物价");
        Jx3Response itemPrice =jxQueryApiService.getItemPrice(item);
        /*
        //旧版文字回复
        StringBuilder respText = new StringBuilder();
        try{
            JSONArray priceList = JSONArray.parseArray(itemPrice.getData());
            StringBuilder itemName = new StringBuilder();
            for(Object a : priceList){
                respText.append("\n");
                for(Object b :JSONArray.parseArray(a.toString())){
                    JSONObject data = JSONObject.parseObject(b.toString());
                    if(itemName.toString().equals(""))
                        itemName.append(data.getString("exterior"))
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
            respText.insert(0,TimestampToString(itemPrice.getTime(),Jx3Constant.DayPattern.LONG.getPattern())
                    +"\n"+itemName);
        }catch(Exception e){
            respText.append("没有找到物品【").append(item).append("】的价格。或许名称不正确");
        }
        List<Message> messagesList = new ArrayList<>();
        messagesList.add(BuildTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
        */
        String url = jx3PicCompiler.ItemPriceImageCompiler(itemPrice);
        List<Message> messagesList = new ArrayList<>();
        if(url == null){
            messagesList.add(BuildTextMessage("没有找到物品【"+item+"】的价格。或许名称不正确"));
        }else{
            messagesList.add(BuildImageMessage(url));
        }
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
        messagesList.add(BuildTextMessage(respText.toString()));
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
                    messagesList.add(BuildImageMessage(equip.getString("pveUrl")));
                }else{
                    respText.append("找不到【").append(text).append("】的PVE配装，请用正确的全称");
                }
            }else if(text.contains("pvp")||text.contains("PVP")){
                JSONObject equip = jxQueryApiService.getEquip(name);
                if(equip != null){
                    messagesList.add(BuildImageMessage(equip.getString("pvpUrl")));
                }else{
                    respText.append("找不到【").append(text).append("】的PVP配装，请用正确的全称");
                }
            }else{
                respText.append("请指定PVE或PVP配装");
            }
        }else{
            respText.append("【").append(text).append("】不是正确的心法称呼");
        }

        messagesList.add(BuildTextMessage(respText.toString()));
        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void getGoldPriceWithServer(String groupId,String text){
        String server = TextExtract(text,"金价");
        List<Message> messagesList = new ArrayList<>();
        if(isServerName(server)){
            Jx3Response goldPrice =jxQueryApiService.getGoldPrice(getRealServerName(server));
            String url = jx3PicCompiler.GoldPriceImageCompiler(goldPrice);
            messagesList.add(BuildImageMessage(url));
        }else{
            messagesList.add(BuildTextMessage("服务器名称未收录，请尽量使用全称"));
        }

        miraHandler.sendGroupMessage(groupId, messagesList);
    }

    public void setRandomTalk(String groupId,String text){
        String s = TextExtract(text,"骚话");
        List<Message> messagesList = new ArrayList<>();
        QqGroup qqGroup = qqGroupMapper.selectOne(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId, groupId));
        if(qqGroup != null){
            if(s.contains("开")){
                qqGroup.setRandomTalk(1);
                qqGroupMapper.updateById(qqGroup);
                randomTalk.put(groupId,new RandomTalkSetting(new DateTime(),1));
                messagesList.add(BuildTextMessage("随机骚话模式已开启"));
            }else if(s.contains("关")){
                qqGroup.setRandomTalk(0);
                qqGroupMapper.updateById(qqGroup);
                randomTalk.put(groupId,new RandomTalkSetting(new DateTime(),0));
                messagesList.add(BuildTextMessage("随机骚话模式已关闭"));
            }else if(s.contains("祖安")){
                qqGroup.setRandomTalk(2);
                qqGroupMapper.updateById(qqGroup);
                randomTalk.put(groupId,new RandomTalkSetting(new DateTime(),2));
                messagesList.add(BuildTextMessage("随机骚话 【祖安】 模式已开启 - [口吐芬芳 请慎开] "));
            }else{
                messagesList.add(BuildTextMessage("请使用 [开] 或者 [关] 或者 [祖安] 表示你想要的操作，关闭祖安模式请使用 [骚话 开] "));
            }
            log.info("[骚话] 群"+groupId+"设置随机骚话,模式："+qqGroup.getRandomTalk());
        }else{
            messagesList.add(BuildTextMessage("请先绑定服务器后使用 格式[ 绑定服务器 服务器名 ]"));
        }
        miraHandler.sendGroupMessage(groupId, messagesList);
    }


    //特殊
    public void TalkRandom(String groupId){
        if(!randomTalk.containsKey(groupId)){
            QqGroup qqGroup = qqGroupMapper.selectOne(new LambdaQueryWrapper<QqGroup>().eq(QqGroup::getGroupId, groupId));
            if(qqGroup == null) return;
            randomTalk.put(groupId,new RandomTalkSetting(new DateTime(),qqGroup.getRandomTalk()));
            log.info("[骚话] 群"+groupId+"设置随机骚话,模式："+qqGroup.getRandomTalk());
        }else {
            if (DateUtil.between(randomTalk.get(groupId).getDate(), new DateTime(), DateUnit.MINUTE) > 3) {
                int mode = randomTalk.get(groupId).getMode();
                int random = -1;
                switch(mode){
                    case 0:
                        return;
                    case 1:
                        random = RandomUtil.randomInt(100);
                        if(random <= 50){//一半机率为骚话
                            random = 0;
                        }else{
                            random = RandomUtil.randomInt(1,4);
                        }
                        break;
                    case 2:
                        random = RandomUtil.randomInt(100);
                        if(random <= 50){//一半机率为祖安
                            random = 4;
                        }else{
                            random = RandomUtil.randomInt(0,4);
                        }
                        break;
                }
                JSONObject data = jxQueryApiService.getRandomTalk(random);
                if(data == null) return;
                randomTalk.put(groupId, new RandomTalkSetting(new DateTime(),mode));
                List<Message> messagesList = new ArrayList<>();
                messagesList.add(BuildTextMessage(data.getString("text")));
                miraHandler.sendGroupMessage(groupId, messagesList);
                log.info("[骚话] 在群" + groupId + "进行骚话");
            }
        }
    }


    /*
    * 其他工具
    * */

    private static String TimestampToString(long ts , String pattern){
        DateFormat df = new SimpleDateFormat(pattern);
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

    private Message BuildTextMessage(String text){
        return Message.builder().type(MiraiConstant.MESSAGE_TYPE_TEXT).text(text).build();
    }

    private Message BuildImageMessage(String url){
        return Message.builder().type(MiraiConstant.MESSAGE_TYPE_IMG).url(url).build();
    }

    public Jx3Handler getInstance(){
        return this;
    }

}
