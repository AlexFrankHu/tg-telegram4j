package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgProxyGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 代理IP组表 Mapper 接口。
 */
@Mapper
public interface TgProxyGroupMapper extends BaseMapper<TgProxyGroup> {
}
