package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgContactImportBatch;
import com.tg.telegram4j.mapper.TgContactImportBatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 联系人导入批次表 Service。
 */
@Slf4j
@Service
public class TgContactImportBatchService {

    private final TgContactImportBatchMapper mapper;

    public TgContactImportBatchService(TgContactImportBatchMapper mapper) {
        this.mapper = mapper;
    }

    public TgContactImportBatch getById(Integer id) {
        return mapper.selectById(id);
    }

    public TgContactImportBatch getByBatchNo(String batchNo) {
        LambdaQueryWrapper<TgContactImportBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContactImportBatch::getBatchNo, batchNo);
        return mapper.selectOne(wrapper);
    }

    public List<TgContactImportBatch> listAll() {
        LambdaQueryWrapper<TgContactImportBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TgContactImportBatch::getCreateTime);
        return mapper.selectList(wrapper);
    }

    public void insert(TgContactImportBatch entity) {
        mapper.insert(entity);
    }

    public void update(TgContactImportBatch entity) {
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }
}
