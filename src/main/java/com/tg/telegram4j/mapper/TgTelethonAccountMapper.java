package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgTelethonAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * Telethon账号管理表 Mapper 接口。
 */
@Mapper
public interface TgTelethonAccountMapper extends BaseMapper<TgTelethonAccount> {
}
