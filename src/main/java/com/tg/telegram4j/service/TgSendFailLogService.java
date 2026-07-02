package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgSendFailLog;
import com.tg.telegram4j.mapper.TgSendFailLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 发送失败日志表 Service。
 */
@Slf4j
@Service
public class TgSendFailLogService {

    private final TgSendFailLogMapper mapper;

    public TgSendFailLogService(TgSendFailLogMapper mapper) {
        this.mapper = mapper;
    }

    public TgSendFailLog getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgSendFailLog> listByPhone(String phone) {
        LambdaQueryWrapper<TgSendFailLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgSendFailLog::getPhone, phone)
               .orderByDesc(TgSendFailLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public List<TgSendFailLog> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgSendFailLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgSendFailLog::getNodeId, nodeId)
               .orderByDesc(TgSendFailLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgSendFailLog entity) {
        mapper.insert(entity);
    }
}
