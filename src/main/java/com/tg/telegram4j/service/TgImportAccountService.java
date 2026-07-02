package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tg.telegram4j.entity.TgImportAccount;
import com.tg.telegram4j.mapper.TgImportAccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入账号明细表 Service。
 */
@Slf4j
@Service
public class TgImportAccountService {

    private final TgImportAccountMapper mapper;

    public TgImportAccountService(TgImportAccountMapper mapper) {
        this.mapper = mapper;
    }

    public TgImportAccount getById(Integer id) {
        return mapper.selectById(id);
    }

    public List<TgImportAccount> listByBatchNo(String batchNo) {
        LambdaQueryWrapper<TgImportAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgImportAccount::getBatchNo, batchNo);
        return mapper.selectList(wrapper);
    }

    public List<TgImportAccount> listByStatus(String status) {
        LambdaQueryWrapper<TgImportAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgImportAccount::getStatus, status);
        return mapper.selectList(wrapper);
    }

    public List<TgImportAccount> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgImportAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgImportAccount::getNodeId, nodeId);
        return mapper.selectList(wrapper);
    }

    public void insert(TgImportAccount entity) {
        mapper.insert(entity);
    }

    public void updateStatus(Integer id, String status, String reason) {
        LambdaUpdateWrapper<TgImportAccount> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgImportAccount::getId, id)
               .set(TgImportAccount::getStatus, status)
               .set(TgImportAccount::getReason, reason)
               .set(TgImportAccount::getUpdateTime, LocalDateTime.now());
        mapper.update(null, wrapper);
    }
}
