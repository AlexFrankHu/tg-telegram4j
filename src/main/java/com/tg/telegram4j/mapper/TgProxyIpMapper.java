package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgProxyIp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 代理IP表 Mapper 接口。
 */
@Mapper
public interface TgProxyIpMapper extends BaseMapper<TgProxyIp> {
}
