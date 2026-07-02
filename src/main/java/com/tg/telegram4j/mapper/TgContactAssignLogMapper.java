package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgContactAssignLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友分配日志表 Mapper 接口。
 */
@Mapper
public interface TgContactAssignLogMapper extends BaseMapper<TgContactAssignLog> {
}
