package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgContactImportRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 联系人导入记录表 Mapper 接口。
 */
@Mapper
public interface TgContactImportRecordMapper extends BaseMapper<TgContactImportRecord> {
}
