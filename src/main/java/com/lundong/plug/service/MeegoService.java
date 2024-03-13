package com.lundong.plug.service;

import com.lundong.plug.entity.*;
import com.lundong.plug.entity.param.CommonReq;
import com.lundong.plug.entity.param.MeegoParam;
import com.lundong.plug.entity.result.R;
import com.lundong.plug.entity.result.RecordResp;
import com.lundong.plug.entity.result.TableMetaResp;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 10:35
 */
public interface MeegoService {

    /**
     * 结构接口
     *
     * @param req
     * @return
     */
    TableMetaResp tableMeta(CommonReq req);

    /**
     * 记录接口
     *
     * @param req
     * @return
     */
    RecordResp records(CommonReq req);

    /**
     * 登录
     *
     * @param meegoParam
     * @return
     */
    R<RecordResp> meegoLogin(MeegoParam meegoParam);

    /**
     * 空间列表
     *
     * @param req
     * @return
     */
    List<SpaceEntity> spaceList(CommonReq req);

    /**
     * 工作项列表
     *
     * @param req
     * @return
     */
    List<ItemEntity> itemList(CommonReq req);

}
