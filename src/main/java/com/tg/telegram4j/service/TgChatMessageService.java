package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgChatMessage;
import com.tg.telegram4j.mapper.TgChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天记录表 Service。
 */
@Slf4j
@Service
public class TgChatMessageService {

    private final TgChatMessageMapper mapper;

    public TgChatMessageService(TgChatMessageMapper mapper) {
        this.mapper = mapper;
    }

    public TgChatMessage getById(Long id) {
        return mapper.selectById(id);
    }

    public List<TgChatMessage> listByAccountAndChat(Integer tgAccountId, Long chatId) {
        LambdaQueryWrapper<TgChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgChatMessage::getTgAccountId, tgAccountId)
               .eq(TgChatMessage::getChatId, chatId)
               .orderByAsc(TgChatMessage::getSendTime);
        return mapper.selectList(wrapper);
    }

    public List<TgChatMessage> listByAccountId(Integer tgAccountId) {
        LambdaQueryWrapper<TgChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgChatMessage::getTgAccountId, tgAccountId)
               .orderByDesc(TgChatMessage::getSendTime);
        return mapper.selectList(wrapper);
    }

    public List<TgChatMessage> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgChatMessage::getNodeId, nodeId)
               .orderByDesc(TgChatMessage::getSendTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgChatMessage entity) {
        mapper.insert(entity);
    }
}
