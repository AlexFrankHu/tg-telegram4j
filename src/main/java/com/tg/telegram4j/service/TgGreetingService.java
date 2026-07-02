package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgGreeting;
import com.tg.telegram4j.mapper.TgGreetingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 广告问候语表 Service。
 */
@Slf4j
@Service
public class TgGreetingService {

    private final TgGreetingMapper mapper;

    public TgGreetingService(TgGreetingMapper mapper) {
        this.mapper = mapper;
    }

    public TgGreeting getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgGreeting> listEnabled() {
        LambdaQueryWrapper<TgGreeting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgGreeting::getIsEnabled, 1)
               .orderByAsc(TgGreeting::getSortOrder);
        return mapper.selectList(wrapper);
    }

    public List<TgGreeting> listAll() {
        LambdaQueryWrapper<TgGreeting> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TgGreeting::getSortOrder);
        return mapper.selectList(wrapper);
    }

    public void insert(TgGreeting entity) {
        mapper.insert(entity);
    }

    public void update(TgGreeting entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    public void deleteById(Integer id) {
        mapper.deleteById(id);
    }
}
