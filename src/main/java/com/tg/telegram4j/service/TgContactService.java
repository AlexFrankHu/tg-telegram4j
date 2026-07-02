package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgContact;
import com.tg.telegram4j.mapper.TgContactMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友/联系人表 Service。
 */
@Slf4j
@Service
public class TgContactService {

    private final TgContactMapper mapper;

    public TgContactService(TgContactMapper mapper) {
        this.mapper = mapper;
    }

    public TgContact getById(Integer id) {
        return mapper.selectById(id);
    }

    public TgContact getByAccountAndUser(Integer tgAccountId, Long userId) {
        LambdaQueryWrapper<TgContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContact::getTgAccountId, tgAccountId)
               .eq(TgContact::getUserId, userId);
        return mapper.selectOne(wrapper);
    }

    public List<TgContact> listByAccountId(Integer tgAccountId) {
        LambdaQueryWrapper<TgContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContact::getTgAccountId, tgAccountId);
        return mapper.selectList(wrapper);
    }

    public List<TgContact> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContact::getNodeId, nodeId);
        return mapper.selectList(wrapper);
    }

    public void insert(TgContact entity) {
        mapper.insert(entity);
    }

    public void update(TgContact entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    public void deleteById(Integer id) {
        mapper.deleteById(id);
    }
}
