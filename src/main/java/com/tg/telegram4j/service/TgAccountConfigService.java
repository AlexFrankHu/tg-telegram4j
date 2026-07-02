package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgAccountConfig;
import com.tg.telegram4j.mapper.TgAccountConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账号配置表 Service。
 */
@Slf4j
@Service
public class TgAccountConfigService {

    private final TgAccountConfigMapper mapper;

    public TgAccountConfigService(TgAccountConfigMapper mapper) {
        this.mapper = mapper;
    }

    public TgAccountConfig getById(Integer id) {
        return mapper.selectById(id);
    }

    public TgAccountConfig getByPhone(String phone) {
        LambdaQueryWrapper<TgAccountConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgAccountConfig::getPhone, phone);
        return mapper.selectOne(wrapper);
    }

    public List<TgAccountConfig> listAll() {
        return mapper.selectList(null);
    }

    public void insert(TgAccountConfig entity) {
        mapper.insert(entity);
    }

    public void update(TgAccountConfig entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    public void deleteById(Integer id) {
        mapper.deleteById(id);
    }
}
