package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgGreeting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 广告问候语表 Mapper 接口。
 */
@Mapper
public interface TgGreetingMapper extends BaseMapper<TgGreeting> {
}
