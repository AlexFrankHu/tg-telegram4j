package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgContactImportBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 联系人导入批次表 Mapper 接口。
 */
@Mapper
public interface TgContactImportBatchMapper extends BaseMapper<TgContactImportBatch> {
}
