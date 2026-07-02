package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgProxyAssignLog;
import com.tg.telegram4j.mapper.TgProxyAssignLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代理分配日志表 Service。
 */
@Slf4j
@Service
public class TgProxyAssignLogService {

    private final TgProxyAssignLogMapper mapper;

    public TgProxyAssignLogService(TgProxyAssignLogMapper mapper) {
        this.mapper = mapper;
    }

    public TgProxyAssignLog getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgProxyAssignLog> listByAccountId(Integer tgAccountId) {
        LambdaQueryWrapper<TgProxyAssignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgProxyAssignLog::getTgAccountId, tgAccountId)
               .orderByDesc(TgProxyAssignLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public List<TgProxyAssignLog> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgProxyAssignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgProxyAssignLog::getNodeId, nodeId)
               .orderByDesc(TgProxyAssignLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgProxyAssignLog entity) {
        mapper.insert(entity);
    }
}
