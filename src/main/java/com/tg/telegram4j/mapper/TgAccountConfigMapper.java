package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgAccountConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账号配置表 Mapper 接口。
 */
@Mapper
public interface TgAccountConfigMapper extends BaseMapper<TgAccountConfig> {
}
