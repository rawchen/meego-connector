package com.lundong.plug.convert;

import com.lundong.plug.entity.TenantAuth;
import com.lundong.plug.entity.vo.TenantAuthVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author shuangquan.chen
 * @date 2023-11-28 17:29
 */
@Mapper
public interface TenantAuthConvert {

    TenantAuthConvert INSTANCE = Mappers.getMapper(TenantAuthConvert.class);

    @Mapping(target = "remainDays", source = "tenantAuth", qualifiedByName = "remainDaysConverse")
    TenantAuthVo tenantAuthToTenantAuthVo(TenantAuth tenantAuth);

    @Named("remainDaysConverse")
    default Long userAvatarConverse(TenantAuth tenantAuth) {
        if (tenantAuth.getCreatedAt() == null) {
            return 0L;
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = tenantAuth.getCreatedAt().plusDays(31);
            long days = Duration.between(now, expireTime).toDays();
            return days;
        }
    }
}
