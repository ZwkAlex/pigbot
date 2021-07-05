package com.alep.pigbot.entity;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RandomTalkSetting {
    private DateTime date;
    private int type;
}
