package com.tg.telegram4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tg.telegram4j.entity.TgClusterNode;
import com.tg.telegram4j.mapper.TgClusterNodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 集群节点信息表 Service。
 */
@Slf4j
@Service
public class TgClusterNodeService {

    private final TgClusterNodeMapper mapper;

    public TgClusterNodeService(TgClusterNodeMapper mapper) {
        this.mapper = mapper;
    }

    public TgClusterNode getByNodeId(String nodeId) {
        return mapper.selectById(nodeId);
    }

    public List<TgClusterNode> listAll() {
        return mapper.selectList(null);
    }

    public List<TgClusterNode> listByStatus(String status) {
        LambdaQueryWrapper<TgClusterNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgClusterNode::getNodeStatus, status);
        return mapper.selectList(wrapper);
    }

    public void insert(TgClusterNode entity) {
        mapper.insert(entity);
    }

    public void update(TgClusterNode entity) {
        entity.setUpdateTime(new Date());
        mapper.updateById(entity);
    }

    public void updateOnlineCount(String nodeId, int onlineCount) {
        LambdaUpdateWrapper<TgClusterNode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TgClusterNode::getNodeId, nodeId)
               .set(TgClusterNode::getOnlineAccountCount, onlineCount)
               .set(TgClusterNode::getLastActiveTime, LocalDateTime.now())
               .set(TgClusterNode::getUpdateTime, LocalDateTime.now());
        mapper.update(null, wrapper);
    }

    public void deleteByNodeId(String nodeId) {
        mapper.deleteById(nodeId);
    }
}
