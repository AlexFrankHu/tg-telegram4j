package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记录表 Mapper 接口。
 */
@Mapper
public interface TgChatMessageMapper extends BaseMapper<TgChatMessage> {
}
