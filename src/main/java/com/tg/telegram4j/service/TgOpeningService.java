package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgOpening;
import com.tg.telegram4j.mapper.TgOpeningMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 主动开场白表 Service。
 */
@Slf4j
@Service
public class TgOpeningService {

    private final TgOpeningMapper mapper;

    public TgOpeningService(TgOpeningMapper mapper) {
        this.mapper = mapper;
    }

    public TgOpening getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgOpening> listEnabled() {
        LambdaQueryWrapper<TgOpening> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgOpening::getIsEnabled, 1)
               .orderByAsc(TgOpening::getSortOrder);
        return mapper.selectList(wrapper);
    }

    public List<TgOpening> listAll() {
        LambdaQueryWrapper<TgOpening> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TgOpening::getSortOrder);
        return mapper.selectList(wrapper);
    }

    public void insert(TgOpening entity) {
        mapper.insert(entity);
    }

    public void update(TgOpening entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    public void deleteById(Integer id) {
        mapper.deleteById(id);
    }
}
