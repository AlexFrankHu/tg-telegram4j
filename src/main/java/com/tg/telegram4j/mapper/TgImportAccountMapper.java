package com.tg.telegram4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tg.telegram4j.entity.TgImportAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导入账号明细表 Mapper 接口。
 */
@Mapper
public interface TgImportAccountMapper extends BaseMapper<TgImportAccount> {
}
