package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgAutoReplyLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自动回复日志表 Mapper 接口。
 */
@Mapper
public interface TgAutoReplyLogMapper extends BaseMapper<TgAutoReplyLog> {
}
