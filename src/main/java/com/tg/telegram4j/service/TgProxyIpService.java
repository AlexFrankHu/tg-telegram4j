package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tg.telegram4j.entity.TgProxyIp;
import com.tg.telegram4j.mapper.TgProxyIpMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 代理IP表 Service。
 */
@Slf4j
@Service
public class TgProxyIpService {

    private final TgProxyIpMapper mapper;

    public TgProxyIpService(TgProxyIpMapper mapper) {
        this.mapper = mapper;
    }

    public TgProxyIp getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgProxyIp> listByGroupNo(String groupNo) {
        LambdaQueryWrapper<TgProxyIp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgProxyIp::getGroupNo, groupNo);
        return mapper.selectList(wrapper);
    }

    public List<TgProxyIp> listAvailable() {
        LambdaQueryWrapper<TgProxyIp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgProxyIp::getStatus, "active")
               .apply("current_bind_count < max_bindable");
        return mapper.selectList(wrapper);
    }

    public List<TgProxyIp> listAll() {
        return mapper.selectList(null);
    }

    public void insert(TgProxyIp entity) {
        mapper.insert(entity);
    }

    public void update(TgProxyIp entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    public void incrementBindCount(Integer id) {
        LambdaUpdateWrapper<TgProxyIp> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgProxyIp::getId, id)
               .setSql("current_bind_count = current_bind_count + 1")
               .setSql("history_bind_count = history_bind_count + 1")
               .set(TgProxyIp::getUpdateTime, LocalDateTime.now());
        mapper.update(null, wrapper);
    }

    public void decrementBindCount(Integer id) {
        LambdaUpdateWrapper<TgProxyIp> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgProxyIp::getId, id)
               .setSql("current_bind_count = GREATEST(current_bind_count - 1, 0)")
               .set(TgProxyIp::getUpdateTime, LocalDateTime.now());
        mapper.update(null, wrapper);
    }
}
