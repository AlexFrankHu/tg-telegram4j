package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgImportBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账号导入批次表 Mapper 接口。
 */
@Mapper
public interface TgImportBatchMapper extends BaseMapper<TgImportBatch> {
}
