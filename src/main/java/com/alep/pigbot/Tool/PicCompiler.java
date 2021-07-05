package com.alep.pigbot.Tool;

import com.alep.pigbot.Jx3.Jx3Constant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Slf4j
public class PicCompiler {
    private static BufferedImage img;
    private static int y;
    boolean header;

    public PicCompiler(){
        y = 0;
        header = false;
        img = new BufferedImage(512,1000,BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        g.setColor(new Color(255,250,240));
        g.fillRect(0,0 ,512,1000);
        g.dispose();
    }


    public String TimestampToString(long ts , String pattern){
        DateFormat df = new SimpleDateFormat(pattern);
        if(ts == -1){
            return df.format(new Date());
        }
        return df.format(ts*1000);
    }

    public void WriteStringWithY(int x, int param_y, String text, Jx3Constant.StringStyle style){
        if(img == null) return;
        Graphics2D g = img.createGraphics();
        if(x == -1) x = (img.getWidth()-getWordWidth(text,style.getFont()))/2;//居中
        else if(x == -2) x = img.getWidth()-getWordWidth(text,style.getFont()) - 20;//右对齐
        else if(x == -3) x = 20;//左对齐
        if(param_y == -1) param_y = y;
        g.setColor(style.getColor());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setFont(style.getFont());
        g.drawString(text,x,param_y);
        g.dispose();
    }

    public void WriteString (int x, String text, Jx3Constant.StringStyle style){
        WriteStringWithY(x,-1,text,style);
    }

    public void WriteStringCenterWithY (int param_y, String text, Jx3Constant.StringStyle style){
        WriteStringWithY(-1,param_y,text,style);
    }

    public void WriteStringCenter (String text, Jx3Constant.StringStyle style){
        WriteStringCenterWithY(-1,text,style);
    }

    public void WriteStringRightWithY (int param_y, String text, Jx3Constant.StringStyle style){
        WriteStringWithY(-2,param_y,text,style);
    }

    public void WriteStringRight (String text, Jx3Constant.StringStyle style){
        WriteStringRightWithY(-1,text,style);
    }

    public void WriteStringLeftWithY (int param_y, String text, Jx3Constant.StringStyle style){
        WriteStringWithY(-3,param_y,text,style);
    }

    public void WriteStringLeft (String text, Jx3Constant.StringStyle style){
        WriteStringLeftWithY(-1,text,style);
    }

    public void NewLine(int add){
        y += add;
    }

    public void DrawLine(int param_y){
        if(param_y==-1) param_y = y;
        Graphics2D g = img.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(20,param_y,img.getWidth()-20,param_y);
        g.dispose();
    }

    public void DrawLine(){
        DrawLine(-1);
    }

    public BufferedImage Finish(){
        img = img.getSubimage(0,0,img.getWidth(),y+20);
        return img;
    }

    private int getWordWidth(String text,Font font){
        Graphics2D g = img.createGraphics();
        FontMetrics  metrics = g.getFontMetrics(font);
        int wordWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            wordWidth += metrics.charWidth(text.charAt(i));
        }
        return wordWidth;
    }


    public BufferedImage getImage() {
        return img;
    }

    public int getY() {
        return y;
    }

    public boolean getHeader() {
        return header;
    }

    public void finishHeader() {
        header = true;
    }
}
