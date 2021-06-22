package com.alep.pigbot.Mirai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class MiraiConstant {
    public static final String DATA = "data";
    public static final String MESSAGE_TYPE_TEXT = "Plain";
    public static final String MESSAGE_TYPE_IMG = "Image";
    public static final String MESSAGE_TYPE_AT = "At";
    public static final String MESSAGE_TYPE_SOURCE = "Source";
    public static final String MESSAGE_TYPE_FRIEND= "FriendMessage";
    public static final String MESSAGE_TYPE_GROUP = "GroupMessage";
    public static final String EVENT_TYPE_NEW_FRIEND = "NewFriendRequestEvent";
    public static final String EVENT_TYPE_NEW_GROUP = "BotInvitedJoinGroupRequestEvent";
    public static String CODE = "code";

    @Getter
    @AllArgsConstructor
    public enum CodeEnum {
        SUCCESS(0, "正常"),
        AUTH_ERROR(1, "错误的auth key"),
        BOT_NOT_FOUND(2, "指定的Bot不存在"),
        SESSION_INVALID(3, "Session失效或不存在"),
        SESSION_NO_CERTIFIED(4, "Session未认证(未激活)"),
        TARGET_NOT_FOUND(5, "发送消息目标不存在(指定对象不存在)"),
        FILE_NOT_FOUND(6, "指定文件不存在，出现于发送本地图片"),
        AUTH_NO(10, "无操作权限，指Bot没有对应操作的权限"),
        MESSAGE_MUTE(20, "Bot被禁言，指Bot当前无法向指定群发送消息"),
        MESSAGE_TOO_LONG(30, "消息过长"),
        ERROR(400, "错误的访问，如参数错误等");
        private Integer code;
        private String message;

    }
}
