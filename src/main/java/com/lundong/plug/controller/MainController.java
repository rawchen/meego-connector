package com.lundong.plug.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lundong.plug.config.Constants;
import com.lundong.plug.convert.TenantAuthConvert;
import com.lundong.plug.entity.Account;
import com.lundong.plug.entity.ItemEntity;
import com.lundong.plug.entity.SpaceEntity;
import com.lundong.plug.entity.TenantAuth;
import com.lundong.plug.entity.param.BitCommonContext;
import com.lundong.plug.entity.param.CommonReq;
import com.lundong.plug.entity.param.MeegoParam;
import com.lundong.plug.entity.result.R;
import com.lundong.plug.entity.result.RecordResp;
import com.lundong.plug.entity.result.TableMetaResp;
import com.lundong.plug.entity.vo.TenantAuthVo;
import com.lundong.plug.mapper.AccountMapper;
import com.lundong.plug.mapper.TenantAuthMapper;
import com.lundong.plug.service.MeegoService;
import com.lundong.plug.service.TenantAuthService;
import com.lundong.plug.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-11-21 11:46
 */
@Slf4j
@RestController
public class MainController {

    @Autowired
    MeegoService meegoService;

    @Autowired
    TenantAuthService tenantAuthService;

    @Autowired
    TenantAuthMapper tenantAuthMapper;

    @Autowired
    AccountMapper accountMapper;


    @RequestMapping(value = "/meego/table_meta")
    public R<TableMetaResp> tableMeta(@RequestBody CommonReq req, HttpServletRequest request) {
        // 该接口返回 数据源的表结构，指明数据源的数据有哪些字段，每个字段是什么类型
        // 工作项
        Integer formType = 1;
        String timestamp = request.getHeader("X-Base-Request-Timestamp");
        String nonce = request.getHeader("X-Base-Request-Nonce");
        String signature = request.getHeader("X-Base-Signature");
        String s = SignUtil.genPostRequestSignature(nonce, timestamp, JSONUtil.toJsonStr(req), Constants.SECRET_KEY);
        log.info("签名: {}", s);
//        if (signature == null || !signature.equals(s)) {
//            return R.fail("签名不正确", null);
//        }

        log.info("req: {}", req);
//        BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
//        MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
//        BitCommonContext context = JSONUtil.toBean(req.getContext(), BitCommonContext.class);
        TableMetaResp tableMetaResp = meegoService.tableMeta(req);
        return R.ok(tableMetaResp);
    }

    @RequestMapping(value = "/meego/records")
    public R<RecordResp> records(@RequestBody CommonReq req, HttpServletRequest request) {
        String timestamp = request.getHeader("X-Base-Request-Timestamp");
        String nonce = request.getHeader("X-Base-Request-Nonce");
        String signature = request.getHeader("X-Base-Signature");
        String s = SignUtil.genPostRequestSignature(nonce, timestamp, JSONUtil.toJsonStr(req), Constants.SECRET_KEY);
        log.info("签名: {}", s);
//        if (signature == null || !signature.equals(s)) {
//            return R.fail("签名不正确", null);
//        }

        log.info("req: {}", req);
        RecordResp resp;
        try {
            resp = meegoService.records(req);
            return R.ok(resp);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage(), null);
        }
    }

    /**
     * 获取空间下工作项类型
     *
     * @param req
     * @param request
     * @return
     */
    @RequestMapping(value = "/meego/itemList")
    public R<List<ItemEntity>> itemList(@RequestBody CommonReq req, HttpServletRequest request) {
        List<ItemEntity> itemEntityList = meegoService.itemList(req);
        log.info("itemEntityList size: {}", itemEntityList.size());
        return R.ok(itemEntityList);
    }

    /**
     * 空间列表
     *
     * @param req
     * @param request
     * @return
     */
    @RequestMapping(value = "/meego/spaceList")
    public R<List<SpaceEntity>> orgList(@RequestBody CommonReq req, HttpServletRequest request) {
        List<SpaceEntity> orgList = meegoService.spaceList(req);
        log.info("spaceList size: {}", orgList.size());
        return R.ok(orgList);
    }

    @RequestMapping(value = "/meta.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object metaJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("meta.json");
        String json = StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
        return JSONUtil.toBean(json, Object.class);
    }

    /**
     * 查看租户套餐（1试用带剩余天数 2普通 3高级）
     *
     * @param req
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryPackage")
    public R<TenantAuthVo> select(@RequestBody CommonReq req, HttpServletRequest request) {
        TenantAuthVo vo;
        BitCommonContext context = JSONUtil.toBean(req.getContext(), BitCommonContext.class);
        String tenantKey = context.getBitable().getTenantKey();
        log.info("queryPackage tenantKey: {}", tenantKey);
        TenantAuth tenantAuth = tenantAuthMapper.selectOne(new LambdaQueryWrapper<TenantAuth>().eq(TenantAuth::getTenantKey, tenantKey).last("limit 1"));
        if (tenantAuth != null) {
            tenantAuthService.rowNumberLimit(tenantAuth.getTenantKey());
            tenantAuth = tenantAuthMapper.selectOne(new LambdaQueryWrapper<TenantAuth>().eq(TenantAuth::getTenantKey, tenantKey).last("limit 1"));
        }
        if (tenantAuth == null) {
            // 通过租户获取tenant_auth，如果空的就说明该租户没用过插件，新建实体，设置套餐id为试用1
            TenantAuth tenantAuthNew = new TenantAuth().setTenantKey(tenantKey).setAuthorizationId(1L).setCreatedAt(LocalDateTime.now()).setUpdatedAt(LocalDateTime.now());
            tenantAuthMapper.insert(tenantAuthNew);
            vo = TenantAuthConvert.INSTANCE.tenantAuthToTenantAuthVo(tenantAuthNew);
        } else {
            vo = TenantAuthConvert.INSTANCE.tenantAuthToTenantAuthVo(tenantAuth);
        }
        return R.ok(vo);
    }

    @RequestMapping(value = "/insert")
    public R insert() {
        TenantAuth tenantAuth = new TenantAuth();
        tenantAuth.setTenantKey("ou_123");
        tenantAuth.setAuthorizationId(1L);
        tenantAuth.setCreatedAt(LocalDateTime.now());
        tenantAuth.setUpdatedAt(LocalDateTime.now());
        tenantAuthMapper.insert(tenantAuth);
        return R.ok();
    }

    @RequestMapping(value = "/meego/list")
    public R accountList(@RequestBody Account account) {
        List<Account> accounts = accountMapper.selectList(new LambdaQueryWrapper<Account>().eq(Account::getOpenId, account.getOpenId()));
        return R.ok(accounts);
    }

    @RequestMapping(value = "/meego/insert")
    public R accountInsert(@RequestBody Account account) {
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        R<RecordResp> recordRespR = meegoService.meegoLogin(
                new MeegoParam()
                        .setPluginId(account.getPluginId())
                        .setPluginSecret(account.getPluginSecret())
                        .setUserKey(account.getUserKey()));
        if (recordRespR.getCode() != 0) {
            return recordRespR;
        }
        int insert = accountMapper.insert(account);
        if (insert == 0) {
            return R.fail("添加失败", null);
        } else {
            return R.ok();
        }
    }

    @RequestMapping(value = "/meego/delete")
    public R accountdelete(@RequestBody Account account) {
        int update = accountMapper.deleteById(account.getId());
        if (update == 0) {
            return R.fail("修改失败", null);
        } else {
            return R.ok();
        }
    }

    @RequestMapping(value = "/meego/update")
    public R accountUpdate(@RequestBody Account account) {
        account.setUpdatedAt(LocalDateTime.now());
        int update = accountMapper.updateById(account);
        if (update == 0) {
            return R.fail("修改失败", null);
        } else {
            return R.ok();
        }
    }

}
