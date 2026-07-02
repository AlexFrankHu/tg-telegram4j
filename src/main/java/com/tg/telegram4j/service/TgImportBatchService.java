package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgImportBatch;
import com.tg.telegram4j.mapper.TgImportBatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账号导入批次表 Service。
 */
@Slf4j
@Service
public class TgImportBatchService {

    private final TgImportBatchMapper mapper;

    public TgImportBatchService(TgImportBatchMapper mapper) {
        this.mapper = mapper;
    }

    public TgImportBatch getById(Integer id) {
        return mapper.selectById(id);
    }

    public TgImportBatch getByBatchNo(String batchNo) {
        LambdaQueryWrapper<TgImportBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgImportBatch::getBatchNo, batchNo);
        return mapper.selectOne(wrapper);
    }

    public List<TgImportBatch> listAll() {
        LambdaQueryWrapper<TgImportBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TgImportBatch::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgImportBatch entity) {
        mapper.insert(entity);
    }

    public void update(TgImportBatch entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }
}
