package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgSendFailLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发送失败日志表 Mapper 接口。
 */
@Mapper
public interface TgSendFailLogMapper extends BaseMapper<TgSendFailLog> {
}
