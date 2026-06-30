package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tg.telegram4j.entity.TgTelethonAccount;
import com.tg.telegram4j.mapper.TgTelethonAccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Telethon账号管理表 Service。
 */
@Slf4j
@Service
public class TgTelethonAccountService {

    private final TgTelethonAccountMapper accountMapper;

    public TgTelethonAccountService(TgTelethonAccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 根据ID查询账号。
     */
    public TgTelethonAccount getById(Integer id) {
        return accountMapper.selectById(id);
    }

    /**
     * 根据手机号查询账号。
     */
    public TgTelethonAccount getByPhone(String phone) {
        LambdaQueryWrapper<TgTelethonAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgTelethonAccount::getPhone, phone)
               .eq(TgTelethonAccount::getIsDeleted, 0);
        return accountMapper.selectOne(wrapper);
    }

    /**
     * 查询所有未删除的账号。
     */
    public List<TgTelethonAccount> listAll() {
        LambdaQueryWrapper<TgTelethonAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgTelethonAccount::getIsDeleted, 0);
        return accountMapper.selectList(wrapper);
    }

    /**
     * 按状态查询账号列表。
     */
    public List<TgTelethonAccount> listByStatus(String status) {
        LambdaQueryWrapper<TgTelethonAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgTelethonAccount::getStatus, status)
               .eq(TgTelethonAccount::getIsDeleted, 0);
        return accountMapper.selectList(wrapper);
    }

    /**
     * 按节点ID查询账号列表。
     */
    public List<TgTelethonAccount> listByNodeId(String nodeId) {
        LambdaQueryWrapper<TgTelethonAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgTelethonAccount::getNodeId, nodeId)
               .eq(TgTelethonAccount::getIsDeleted, 0);
        return accountMapper.selectList(wrapper);
    }

    /**
     * 按批次号查询账号列表。
     */
    public List<TgTelethonAccount> listByBatchNo(String batchNo) {
        LambdaQueryWrapper<TgTelethonAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgTelethonAccount::getBatchNo, batchNo)
               .eq(TgTelethonAccount::getIsDeleted, 0);
        return accountMapper.selectList(wrapper);
    }

    /**
     * 更新账号状态。
     */
    public void updateStatus(Integer id, String status) {
        LambdaUpdateWrapper<TgTelethonAccount> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgTelethonAccount::getId, id)
               .set(TgTelethonAccount::getStatus, status)
               .set(TgTelethonAccount::getUpdateTime, LocalDateTime.now());
        accountMapper.update(null, wrapper);
    }

    /**
     * 更新登录后的账号信息（tgUserId、nickname、username 等）。
     */
    public void updateAfterLogin(Integer id, Long tgUserId, String nickname, String username, String phone) {
        LambdaUpdateWrapper<TgTelethonAccount> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgTelethonAccount::getId, id)
               .set(TgTelethonAccount::getTgUserId, tgUserId)
               .set(TgTelethonAccount::getNickname, nickname)
               .set(TgTelethonAccount::getUsername, username)
               .set(TgTelethonAccount::getStatus, "online")
               .set(TgTelethonAccount::getLastLoginTime, LocalDateTime.now())
               .set(TgTelethonAccount::getUpdateTime, LocalDateTime.now());
        if (phone != null && !phone.isBlank()) {
            wrapper.set(TgTelethonAccount::getPhone, phone);
        }
        accountMapper.update(null, wrapper);
    }

    /**
     * 更新消息计数。
     */
    public void incrementMsgCount(Integer id, boolean isSent) {
        LambdaUpdateWrapper<TgTelethonAccount> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgTelethonAccount::getId, id)
               .setSql("total_msg_count = COALESCE(total_msg_count, 0) + 1")
               .set(TgTelethonAccount::getUpdateTime, LocalDateTime.now());
        if (isSent) {
            wrapper.setSql("sent_msg_count = COALESCE(sent_msg_count, 0) + 1");
        } else {
            wrapper.setSql("recv_msg_count = COALESCE(recv_msg_count, 0) + 1");
        }
        accountMapper.update(null, wrapper);
    }

    /**
     * 插入新账号。
     */
    public void insert(TgTelethonAccount account) {
        accountMapper.insert(account);
    }

    /**
     * 更新账号。
     */
    public void update(TgTelethonAccount account) {
        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);
    }

    /**
     * 逻辑删除账号。
     */
    public void deleteById(Integer id) {
        LambdaUpdateWrapper<TgTelethonAccount> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgTelethonAccount::getId, id)
               .set(TgTelethonAccount::getIsDeleted, 1)
               .set(TgTelethonAccount::getUpdateTime, LocalDateTime.now());
        accountMapper.update(null, wrapper);
    }
}
