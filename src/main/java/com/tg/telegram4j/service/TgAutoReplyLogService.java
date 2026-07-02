package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgAutoReplyLog;
import com.tg.telegram4j.mapper.TgAutoReplyLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自动回复日志表 Service。
 */
@Slf4j
@Service
public class TgAutoReplyLogService {

    private final TgAutoReplyLogMapper mapper;

    public TgAutoReplyLogService(TgAutoReplyLogMapper mapper) {
        this.mapper = mapper;
    }

    public TgAutoReplyLog getById(Long id) {
        return mapper.selectById(id);
    }

    public List<TgAutoReplyLog> listByAccountPhone(String accountPhone) {
        LambdaQueryWrapper<TgAutoReplyLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgAutoReplyLog::getAccountPhone, accountPhone)
               .orderByDesc(TgAutoReplyLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public List<TgAutoReplyLog> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgAutoReplyLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgAutoReplyLog::getNodeId, nodeId)
               .orderByDesc(TgAutoReplyLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgAutoReplyLog entity) {
        mapper.insert(entity);
    }
}
