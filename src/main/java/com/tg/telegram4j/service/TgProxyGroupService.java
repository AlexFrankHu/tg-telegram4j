package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgProxyGroup;
import com.tg.telegram4j.mapper.TgProxyGroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 代理IP组表 Service。
 */
@Slf4j
@Service
public class TgProxyGroupService {

    private final TgProxyGroupMapper mapper;

    public TgProxyGroupService(TgProxyGroupMapper mapper) {
        this.mapper = mapper;
    }

    public TgProxyGroup getById(Integer id) {
        return mapper.selectById(id);
    }

    public TgProxyGroup getByGroupNo(String groupNo) {
        LambdaQueryWrapper<TgProxyGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgProxyGroup::getGroupNo, groupNo);
        return mapper.selectOne(wrapper);
    }

    public List<TgProxyGroup> listAll() {
        LambdaQueryWrapper<TgProxyGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TgProxyGroup::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgProxyGroup entity) {
        mapper.insert(entity);
    }

    public void update(TgProxyGroup entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    public void deleteById(Integer id) {
        mapper.deleteById(id);
    }
}
