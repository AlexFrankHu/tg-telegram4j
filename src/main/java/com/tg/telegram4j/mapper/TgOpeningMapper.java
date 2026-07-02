package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgOpening;
import org.apache.ibatis.annotations.Mapper;

/**
 * 主动开场白表 Mapper 接口。
 */
@Mapper
public interface TgOpeningMapper extends BaseMapper<TgOpening> {
}
