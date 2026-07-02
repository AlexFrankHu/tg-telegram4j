package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgLoginLog;
import com.tg.telegram4j.mapper.TgLoginLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录日志表 Service。
 */
@Slf4j
@Service
public class TgLoginLogService {

    private final TgLoginLogMapper mapper;

    public TgLoginLogService(TgLoginLogMapper mapper) {
        this.mapper = mapper;
    }

    public TgLoginLog getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgLoginLog> listByPhone(String phone) {
        LambdaQueryWrapper<TgLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgLoginLog::getPhone, phone)
               .orderByDesc(TgLoginLog::getLoginTime);
        return mapper.selectList(wrapper);
    }

    public List<TgLoginLog> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgLoginLog::getNodeId, nodeId)
               .orderByDesc(TgLoginLog::getLoginTime);
        return mapper.selectList(wrapper);
    }

    public List<TgLoginLog> listByResult(String result) {
        LambdaQueryWrapper<TgLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgLoginLog::getResult, result)
               .orderByDesc(TgLoginLog::getLoginTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgLoginLog entity) {
        mapper.insert(entity);
    }
}
