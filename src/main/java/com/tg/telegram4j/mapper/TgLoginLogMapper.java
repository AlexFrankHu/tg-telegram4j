package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志表 Mapper 接口。
 */
@Mapper
public interface TgLoginLogMapper extends BaseMapper<TgLoginLog> {
}
