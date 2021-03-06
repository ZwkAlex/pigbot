package com.alep.pigbot.Jx3;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Jx3Constant {

    public static final Map<String,String> Gold = Map.of(
            "wanbaolou","万宝楼",
            "uu898","uu898",
            "dd373","dd373",
            "5173","5173",
            "7881","7881"
    );

    @Getter
    @AllArgsConstructor
    public enum ServerEnum {
        JDTJ("绝代天骄", asList("绝代","电八")),
        MJN("梦江南", asList("双梦","双梦镇")),
        PZZ("破阵子", asList("念破","念奴娇")),
        TEP("天鹅坪", asList("纵月")),
        FLZT("飞龙在天", asList("飞龙","双二")),
        QMZJ("青梅煮酒", asList("青梅","双四")),
        FTZD("奉天证道", asList("奉天")),
        CAC("长安城", asList("长安")),
        LZHD("龙争虎斗", asList("龙争")),
        DLH("蝶恋花", asList("蝶服")),
        DZXY("斗转星移", asList("姨妈服")),
        QKYZ("乾坤一掷", asList("华乾")),
        WWDZ("唯我独尊", asList("唯满侠","鹅满侠")),
        YYL("幽月轮", asList("六合一")),
        ZZZS("执子之手", asList()),
        JDQX("剑胆琴心", asList("剑胆","煎蛋","金蛋莱")),
        XZCG("侠者成歌",asList());

        private String name;
        private List<String> alterNames;

    }

    public static final Map<String, Method> allKey = new HashMap<>();
    public static final Map<String, Method> noAllKey = new HashMap<>();
    static {
        try {
            allKey.put("开服", Jx3Handler.class.getDeclaredMethod("getServerStatus",String.class));
            allKey.put("服务器", Jx3Handler.class.getDeclaredMethod("getBindServer",String.class));
            allKey.put("金价", Jx3Handler.class.getDeclaredMethod("getGoldPrice",String.class));
            allKey.put("使用说明",Jx3Handler.class.getDeclaredMethod("getFunction",String.class));
            noAllKey.put("金价", Jx3Handler.class.getDeclaredMethod("getGoldPriceWithServer",String.class,String.class));
            noAllKey.put("绑定服务器", Jx3Handler.class.getDeclaredMethod("setBindServer",String.class,String.class));
            noAllKey.put("物价", Jx3Handler.class.getDeclaredMethod("getItemPrice",String.class,String.class));
            noAllKey.put("宏", Jx3Handler.class.getDeclaredMethod("getMarco",String.class,String.class));
            noAllKey.put("配装", Jx3Handler.class.getDeclaredMethod("getEquip",String.class,String.class));
            noAllKey.put("骚话", Jx3Handler.class.getDeclaredMethod("setRandomTalk",String.class,String.class));
            noAllKey.put("奇遇查询", Jx3Handler.class.getDeclaredMethod("getSerendipity", String.class, String.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Jx3NameEnum {
        JY("惊羽诀", asList("鲸鱼","jy")),
        TLGD("天罗诡道", asList("田螺","tl")),
        ZXG("紫霞功", asList("紫霞","气纯","备胎","qc")),
        TXJY("太虚剑意",asList("太虚","剑纯","渣男","jc")),
        HJY("花间游", asList("花间","万花","hj")),
        LJYD("离经易道", asList("离经","奶花","nh")),
        BXJ("冰心诀", asList("冰心","雷电法王","七秀","bx")),
        YSXJ("云裳心经", asList("云裳","奶秀","nx")),
        XSJ("洗髓经", asList("洗髓","和尚t","和尚T","hst")),
        YJJ("易筋经", asList("易筋","和尚","hs")),
        TLL("铁牢律", asList("铁牢","天策t","天策T","tct")),
        AXZY("傲血战意", asList("傲血","傲雪","天策","tc")),
        SJJY("山居剑意", asList("藏剑","山居","黄鸡","cj")),
        WSJ("问水诀", asList("问水")),
        DJ("毒经", asList("五毒","毒毒","dj")),
        BTJ("补天诀", asList("奶毒","补天","dn")),
        FYSJ("焚影圣诀", asList("焚影","fy")),
        MZLLT("明尊琉璃体", asList("明尊","喵t","喵T","mt")),
        XCJ("笑尘诀",asList("丐帮","丐丐","gb")),
        TGY("铁骨衣", asList("铁骨","苍云t","苍云T","铁王八","乌龟","cyt")),
        FSJ("分山劲", asList("分山","岔劲","苍云","cy")),
        MW("莫问", asList("长歌","咕咕","鸽子","mw")),
        XZ("相知", asList("奶歌","奶咕","xz")),
        BAJ("北傲诀", asList("霸刀","貂貂","bd")),
        LHJ("凌海诀",asList("蓬莱","伞","pl")),
        YLJ("隐龙诀", asList("凌雪","凌雪阁","lx")),
        TXJ("太玄经",asList("衍天","衍天宗","演员","yt"));

        private String name;
        private List<String> alterNames;

    }

    @Getter
    @AllArgsConstructor
    public enum DayPattern {
        LONG( "yyyy/MM/dd"),
        SHORT("MM/dd HH:mm"),
        DETAIL("HH:mm");

        private String pattern;
    }


    public static final int HEADER_SIZE = 45;
    public static final int TITLE_SIZE = 35;
    public static final int CONTEXT_SIZE = 25;
    public static final int TAIL_SIZE = 15;

    @Getter
    @AllArgsConstructor
    public enum StringStyle {
        HEADER(new Font("微软雅黑" , Font.BOLD, HEADER_SIZE),new Color(207,181,59)),
        TITLE(new Font("微软雅黑" , Font.BOLD, TITLE_SIZE),Color.BLACK),
        CONTEXT(new Font("微软雅黑" , Font.BOLD, CONTEXT_SIZE),Color.BLACK),
        TAIL(new Font("微软雅黑" , Font.BOLD, TAIL_SIZE),Color.GRAY);

        private Font font;
        private Color color;
    }
}

