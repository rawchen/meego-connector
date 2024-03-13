package com.lundong.plug.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lundong.plug.entity.TenantAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author RawChen
 * @date 2023-11-28 10:10
 */
@Mapper
public interface TenantAuthMapper extends BaseMapper<TenantAuth> {

    default List<TenantAuth> selectAll() {
        return selectList(new LambdaQueryWrapper<>());
    }

    void insertBatch(@Param("tenantAuths") List<TenantAuth> tenantAuths);
}
