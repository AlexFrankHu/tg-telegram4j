package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgContact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友/联系人表 Mapper 接口。
 */
@Mapper
public interface TgContactMapper extends BaseMapper<TgContact> {
}
