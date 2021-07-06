package com.alep.pigbot.Jx3;

import com.alep.pigbot.Tool.PicCompiler;
import com.alep.pigbot.entity.Jx3Response;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Map;

/*
* 图片合成
* */
@Component
@Slf4j
public class Jx3PicCompiler {

    @Value("${server.ip}")
    private String ip;

    @Value("${server.port}")
    private String port;

    public String ItemPriceImageCompiler(Jx3Response itemPrice) {
        String urlPath = null;
        try{
            JSONArray priceList = JSONArray.parseArray(itemPrice.getData());
            PicCompiler picCompiler = new PicCompiler();
            for(Object dataObject : priceList){
                boolean region = false;
                JSONArray regionDataArray = JSONArray.parseArray(dataObject.toString());
                for(int i = 0; i < 3 && i < regionDataArray.size(); i++ ) {
                    JSONObject data = JSONObject.parseObject(regionDataArray.get(i).toString());
                    if(!picCompiler.getHeader()){
                        picCompiler.NewLine(Jx3Constant.HEADER_SIZE + 20);
                        picCompiler.WriteStringCenter(data.getString("exterior")+"·"+data.getString("itemname"),
                                Jx3Constant.StringStyle.HEADER);
                        picCompiler.NewLine(20);
                        picCompiler.DrawLine();
                        picCompiler.FinishHeader();
                    }
                    if(!region){
                        picCompiler.NewLine(Jx3Constant.TITLE_SIZE + 20) ;
                        picCompiler.WriteStringCenter(data.getString("regionAlias"),Jx3Constant.StringStyle.TITLE);
                        picCompiler.NewLine( 20) ;
                        region = true;
                    }
                    picCompiler.NewLine(Jx3Constant.CONTEXT_SIZE + 10) ;
                    picCompiler.WriteString(20, data.getString("server"),Jx3Constant.StringStyle.CONTEXT);
                    picCompiler.WriteString(120, "￥"+data.getString("price"), Jx3Constant.StringStyle.CONTEXT);
                    picCompiler.WriteString(250, data.getString("saleCode") ,Jx3Constant.StringStyle.CONTEXT);
                    picCompiler.WriteString(380, data.getString("tradeTime") ,Jx3Constant.StringStyle.CONTEXT);
                }
                picCompiler.NewLine(20);
            }
            MakeTailWithTime(picCompiler, picCompiler.TimestampToString(itemPrice.getTime(),Jx3Constant.DayPattern.LONG.getPattern()));
            picCompiler.Finish();
            urlPath = SaveImage(picCompiler,Long.toString(itemPrice.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlPath;
    }

    public String GoldPriceImageCompiler(Jx3Response goldPrice) {
        try{
            PicCompiler picCompiler = new PicCompiler();
            for( Map.Entry<String, String> entry :Jx3Constant.Gold.entrySet()) {
                JSONObject data = JSONObject.parseObject(goldPrice.getData());
                if(!picCompiler.getHeader()){
                    picCompiler.NewLine(Jx3Constant.HEADER_SIZE + 20);
                    picCompiler.WriteStringCenter(data.getString("server")+"·今日金价",Jx3Constant.StringStyle.HEADER);
                    picCompiler.NewLine(20);
                    picCompiler.DrawLine();
                    picCompiler.FinishHeader();
                }
                if (data.containsKey(entry.getKey())) {
                    picCompiler.NewLine(Jx3Constant.CONTEXT_SIZE+20);
                    picCompiler.WriteString(150,entry.getValue() ,
                            Jx3Constant.StringStyle.CONTEXT);
                    picCompiler.WriteString(300, data.getDouble(entry.getKey()).intValue()+" : 1",
                            Jx3Constant.StringStyle.CONTEXT);
                }
            }

            MakeTailWithTime(picCompiler,picCompiler.TimestampToString(goldPrice.getTime(),Jx3Constant.DayPattern.LONG.getPattern()));
            picCompiler.Finish();
            return SaveImage(picCompiler,Long.toString(goldPrice.getTime()));
        }catch(Exception e){
            e.printStackTrace();
            log.info("[PicCompiler] 合成图片出错 可能是JSON parse 错误");
            return null;
        }
    }

    public String FunctionPicCompiler(){
        PicCompiler picCompiler = new PicCompiler();
        picCompiler.NewLine(Jx3Constant.HEADER_SIZE);
        picCompiler.WriteStringCenter("使用说明",Jx3Constant.StringStyle.HEADER);
        picCompiler.NewLine(20);
        picCompiler.DrawLine();
        picCompiler.NewLine(Jx3Constant.TITLE_SIZE+20);
        picCompiler.WriteStringCenter("以下功能无需提供目标",Jx3Constant.StringStyle.TITLE);
        for(String key:Jx3Constant.allKey.keySet()){
            picCompiler.NewLine(Jx3Constant.CONTEXT_SIZE+20);
            picCompiler.WriteStringCenter(key,Jx3Constant.StringStyle.CONTEXT);
        }
        picCompiler.NewLine(Jx3Constant.TITLE_SIZE+20);
        picCompiler.WriteStringCenter("以下功能必须提供目标 ",Jx3Constant.StringStyle.TITLE);
        picCompiler.NewLine(Jx3Constant.TITLE_SIZE+20);
        picCompiler.WriteStringCenter("格式[关键词 目标] 必须包含空格",Jx3Constant.StringStyle.TITLE);
        for(String key:Jx3Constant.noAllKey.keySet()){
            picCompiler.NewLine(Jx3Constant.CONTEXT_SIZE+20);
            picCompiler.WriteStringCenter(key + " 目标",Jx3Constant.StringStyle.CONTEXT);
        }
        picCompiler.NewLine(20);
        picCompiler.DrawLine();
        picCompiler.NewLine(Jx3Constant.TAIL_SIZE+5);
        picCompiler.WriteStringRight("贰货猪PigBot",Jx3Constant.StringStyle.TAIL);
        picCompiler.Finish();
        return SaveImage(picCompiler,"Manual");
    }

    public String SerendipityPicCompiler(Jx3Response serendipity){
        try{
            PicCompiler picCompiler = new PicCompiler();
            JSONArray dataList = JSONArray.parseArray(serendipity.getData());
            for(Object obj:dataList){
                JSONObject data = JSONObject.parseObject(obj.toString());
                if(!picCompiler.getHeader()){
                    picCompiler.NewLine(Jx3Constant.HEADER_SIZE + 20);
                    picCompiler.WriteStringCenter(data.getString("server")+"·"+data.getString("name"), Jx3Constant.StringStyle.HEADER);
                    picCompiler.NewLine(Jx3Constant.TITLE_SIZE + 20);
                    picCompiler.WriteStringCenter("奇遇记录", Jx3Constant.StringStyle.TITLE);
                    picCompiler.NewLine(20);
                    picCompiler.DrawLine();
                    picCompiler.FinishHeader();
                }
                picCompiler.NewLine(Jx3Constant.CONTEXT_SIZE + 10);
                picCompiler.WriteStringCenter(
                        picCompiler.TimestampToString(data.getLong("time"),Jx3Constant.DayPattern.LONG.getPattern())
                                + data.getString("serendipity"), Jx3Constant.StringStyle.CONTEXT);
            }
            MakeTailWithTime(picCompiler,picCompiler.TimestampToString(serendipity.getTime(),Jx3Constant.DayPattern.LONG.getPattern()));
            picCompiler.Finish();
            return SaveImage(picCompiler,Long.toString(serendipity.getTime()));
        }catch(Exception e){
            e.printStackTrace();
            log.info("[PicCompiler] 合成图片出错 可能是JSON parse 错误");
            return null;
        }
    }

    private void MakeTailWithTime(PicCompiler picCompiler,String time){
        picCompiler.NewLine(20);
        picCompiler.DrawLine();
        picCompiler.NewLine(Jx3Constant.TAIL_SIZE + 5);
        String tailString = "贰货猪 Pigbot - 数据截止至:" + time;
        picCompiler.WriteStringRight(tailString,Jx3Constant.StringStyle.TAIL);
        picCompiler.NewLine(Jx3Constant.TAIL_SIZE + 5);
        tailString = "数据来源自 JX3API ";
        picCompiler.WriteStringRight(tailString,Jx3Constant.StringStyle.TAIL);
    }

    private String SaveImage(PicCompiler picCompiler, String fileNamePrefix){
        try{
            String fileName = fileNamePrefix +"_"+ (int) (Math.random() * 100) ;
            String filePath = "./local_data/" + fileName + ".png";
            String urlPath = "http://" + ip + ":"+ port +"/image?id="+ fileName;
            ImageIO.write(picCompiler.getImage(), "png", new File(filePath));
            return urlPath;
        }catch(Exception e){
            e.printStackTrace();
            log.info("[PicCompiler] 合成图片出错 可能是图片写入出错");
            return null;
        }
    }

}
