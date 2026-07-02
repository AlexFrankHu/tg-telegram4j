package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tg.telegram4j.entity.TgContactImportRecord;
import com.tg.telegram4j.mapper.TgContactImportRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 联系人导入记录表 Service。
 */
@Slf4j
@Service
public class TgContactImportRecordService {

    private final TgContactImportRecordMapper mapper;

    public TgContactImportRecordService(TgContactImportRecordMapper mapper) {
        this.mapper = mapper;
    }

    public TgContactImportRecord getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgContactImportRecord> listByBatchNo(String batchNo) {
        LambdaQueryWrapper<TgContactImportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContactImportRecord::getBatchNo, batchNo);
        return mapper.selectList(wrapper);
    }

    public List<TgContactImportRecord> listUnusedByBatchNo(String batchNo) {
        LambdaQueryWrapper<TgContactImportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgContactImportRecord::getBatchNo, batchNo)
               .eq(TgContactImportRecord::getIsUsed, 0);
        return mapper.selectList(wrapper);
    }

    public void insert(TgContactImportRecord entity) {
        mapper.insert(entity);
    }

    public void insertBatch(List<TgContactImportRecord> entities) {
        for (TgContactImportRecord entity : entities) {
            mapper.insert(entity);
        }
    }

    public void markUsed(Integer id) {
        TgContactImportRecord entity = new TgContactImportRecord();
        entity.setId(id);
        entity.setIsUsed(1);
        mapper.updateById(entity);
    }
}
