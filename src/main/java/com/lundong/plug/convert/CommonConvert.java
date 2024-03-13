package com.lundong.plug.convert;

import com.lundong.plug.entity.param.CustomParam;
import com.lundong.plug.entity.param.MeegoParam;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author shuangquan.chen
 * @date 2023-11-24 13:57
 */
@Mapper
public interface CommonConvert {

    CommonConvert INSTANCE = Mappers.getMapper(CommonConvert.class);

    MeegoParam customParamToMeegoParam(CustomParam customParam);
}
