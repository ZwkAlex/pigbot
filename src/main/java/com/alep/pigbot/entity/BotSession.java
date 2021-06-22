package com.alep.pigbot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("bot_session")
@Builder
public class BotSession implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String botQq;
    private String session;

}