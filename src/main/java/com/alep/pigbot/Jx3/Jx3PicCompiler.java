package com.alep.pigbot.Jx3;

import com.alep.pigbot.Tool.PicCompiler;
import com.alep.pigbot.entity.Jx3Response;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/*
* 图片合成
* */
@Component
@Slf4j
public class Jx3PicCompiler {
    @Getter
    @Value("${server.url}")
    private String url;

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
                                Jx3Constant.StringStyle.Header);
                        picCompiler.NewLine(20);
                        picCompiler.DrawLine();
                        picCompiler.finishHeader();
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
        String urlPath = null;
        try{
            PicCompiler picCompiler = new PicCompiler();
            for( Map.Entry<String, String> entry :Jx3Constant.Gold.entrySet()) {
                JSONObject data = JSONObject.parseObject(goldPrice.getData());
                if(!picCompiler.getHeader()){
                    picCompiler.NewLine(Jx3Constant.HEADER_SIZE + 20);
                    picCompiler.WriteStringCenter(data.getString("server")+"·今日金价",Jx3Constant.StringStyle.Header);
                    picCompiler.NewLine(20);
                    picCompiler.DrawLine();
                    picCompiler.finishHeader();
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
            urlPath = SaveImage(picCompiler,Long.toString(goldPrice.getTime()));
        }catch(Exception e){
            e.printStackTrace();
        }
        return urlPath;
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

    private String SaveImage(PicCompiler picCompiler, String timestamp) throws IOException {
        String fileName = timestamp +"_"+ (int) (Math.random() * 100) ;
        String filePath = "./local_data/" + fileName + ".png";
        String urlPath = url + "?id="+ fileName;
        ImageIO.write(picCompiler.getImage(), "png", new File(filePath));
        return urlPath;
    }

}
