package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tg.telegram4j.entity.TgContactAssignLog;
import com.tg.telegram4j.mapper.TgContactAssignLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友分配日志表 Service。
 */
@Slf4j
@Service
public class TgContactAssignLogService {

    private final TgContactAssignLogMapper mapper;

    public TgContactAssignLogService(TgContactAssignLogMapper mapper) {
        this.mapper = mapper;
    }

    public TgContactAssignLog getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgContactAssignLog> listByAccountId(Integer tgAccountId) {
        LambdaQueryWrapper<TgContactAssignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContactAssignLog::getTgAccountId, tgAccountId)
               .orderByDesc(TgContactAssignLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public List<TgContactAssignLog> listByStatus(String status) {
        LambdaQueryWrapper<TgContactAssignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContactAssignLog::getStatus, status)
               .orderByAsc(TgContactAssignLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public List<TgContactAssignLog> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgContactAssignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContactAssignLog::getNodeId, nodeId)
               .orderByDesc(TgContactAssignLog::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgContactAssignLog entity) {
        mapper.insert(entity);
    }

    public void updateStatus(Integer id, String status, String errorReason) {
        LambdaUpdateWrapper<TgContactAssignLog> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgContactAssignLog::getId, id)
               .set(TgContactAssignLog::getStatus, status)
               .set(TgContactAssignLog::getErrorReason, errorReason)
               .set(TgContactAssignLog::getUpdateTime, LocalDateTime.now());
        mapper.update(null, wrapper);
    }
}
