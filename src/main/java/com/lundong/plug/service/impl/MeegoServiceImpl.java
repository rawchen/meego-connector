package com.lundong.plug.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lundong.plug.config.Constants;
import com.lundong.plug.entity.*;
import com.lundong.plug.entity.param.BitCommonContext;
import com.lundong.plug.entity.param.BitCommonParam;
import com.lundong.plug.entity.param.CommonReq;
import com.lundong.plug.entity.param.MeegoParam;
import com.lundong.plug.entity.result.*;
import com.lundong.plug.enums.FormTypeEnum;
import com.lundong.plug.mapper.TenantAuthMapper;
import com.lundong.plug.service.MeegoService;
import com.lundong.plug.service.TenantAuthService;
import com.lundong.plug.util.ArrUtil;
import com.lundong.plug.util.SignUtil;
import com.lundong.plug.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 10:37
 */
@Slf4j
@Service
public class MeegoServiceImpl implements MeegoService {

    @Autowired
    TenantAuthMapper tenantAuthMapper;

    @Autowired
    TenantAuthService tenantAuthService;

    public WorkItemPage queryWorkItemList(MeegoParam meegoParam, Long lineLimitNumber, String pageToken, String maxPageSize) throws RuntimeException {
        return SignUtil.workItemList(meegoParam, pageToken, maxPageSize);
//        if (workItems.size() >= lineLimitNumber) {
//            workItems = workItems.stream().limit(lineLimitNumber).collect(Collectors.toList());
//        }
//        return workItems;
    }

    @Override
    public TableMetaResp tableMeta(CommonReq req) {
        TableMetaResp resp = new TableMetaResp();
        BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
        MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
        switch (FormTypeEnum.toType(meegoParam.getDataType())) {
            case WORK_ITEM:
                List<Field> stockfields = new ArrayList<>();
                if (StrUtil.isEmpty(meegoParam.getTypeKey())) {
                    resp.setTableName("工作项");
                }
                List<ItemEntity> itemEntities = itemList(req);
                if (!ArrUtil.isEmpty(itemEntities)) {
                    for (ItemEntity itemEntity : itemEntities) {
                        if (itemEntity.getTypeKey().equals(meegoParam.getTypeKey())) {
                            resp.setTableName(itemEntity.getName());
                            break;
                        }
                    }
                }
                stockfields.add(new Field().setFieldName(resp.getTableName() + "ID").setFieldId("field_000001").setFieldType(Constants.biTableNum).setIsPrimary(true).setDescription("").setProperty(new Property().setFormatter("0")));
                List<WorkItemField> workItemFields = SignUtil.fieldAll(meegoParam);
                if(CollectionUtils.isEmpty(workItemFields)){
                    return null;
                }
                workItemFields = workItemFields.stream().filter(n -> !"vote_option".equals(n.getFieldTypeKey())).collect(Collectors.toList());
                List<WorkItemField> collectSomeOne = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains(meegoParam.getTypeKey())).collect(Collectors.toList());
                for (WorkItemField workItemField : collectSomeOne) {
                    stockfields.add(new Field().setFieldName(workItemField.getFieldName()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setIsPrimary(false).setDescription("").setFieldId(workItemField.getFieldKey()));
                }
                List<WorkItemField> collectAll = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains("_all")).collect(Collectors.toList());
                for (WorkItemField workItemField : collectAll) {
                    stockfields.add(new Field().setFieldName(workItemField.getFieldName()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setIsPrimary(false).setDescription("").setFieldId(workItemField.getFieldKey()));
                }

                List<RoleField> roleFields = SignUtil.fieldAllRole(meegoParam);
                dealRoleFields(roleFields, stockfields);

                stockfields = stockfields.stream().filter(s -> s.getFieldType() != -1).collect(Collectors.toList());
                stockfields.add(new Field().setFieldName("工作项URL链接").setFieldId("id88888888").setFieldType(Constants.biTableLink).setIsPrimary(false).setDescription("工作项URL链接"));
                resp.setFields(stockfields);
                break;
        }
        return resp;
    }

    @Override
    public RecordResp records(CommonReq req) throws RuntimeException {

        Long lineLimitNumber = 0L;
        log.info("req params: {}", req.getParams());
        BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
        MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
        BitCommonContext context = JSONUtil.toBean(req.getContext(), BitCommonContext.class);
        String tenantKey = context.getTenantKey();
        log.info("records tenantKey: {}", tenantKey);
        log.info("userTenantKey: {}", context.getUserTenantKey());
        TenantAuth tenantAuth = tenantAuthMapper.selectOne(
                new LambdaQueryWrapper<TenantAuth>().eq(TenantAuth::getTenantKey, context.getUserTenantKey()).last("limit 1"));
        if (tenantAuth != null) {
            lineLimitNumber = tenantAuthService.rowNumberLimit(tenantAuth.getTenantKey());
        } else {
            log.error("根据租户获取不到行数规则");
            lineLimitNumber = 100000L;
        }

        // 1工作项列表 2插件工时 3任务工时
        Integer formType = meegoParam.getDataType();

        RecordResp recordResp = new RecordResp();
        switch (FormTypeEnum.toType(formType)) {
            case WORK_ITEM:
                // 封装字段和文本
                WorkItemPage workItemPage = queryWorkItemList(meegoParam, lineLimitNumber, params.getPageToken(), params.getMaxPageSize());
                List<WorkItem> workItems = workItemPage.getWorkItemList();
                log.info("workItems size: {}", workItems.size());
                List<Record> stockRecords = new ArrayList<>();
                List<SpaceEntity> spaceList = spaceList(req);
                List<Field> stockfields = new ArrayList<>();
                stockfields.add(new Field().setFieldId("field_000001").setFieldKey("id").setFieldType(Constants.biTableNum));
                List<WorkItemField> workItemFields = SignUtil.fieldAll(meegoParam);
                workItemFields = workItemFields.stream().filter(n -> !"vote_option".equals(n.getFieldTypeKey())).collect(Collectors.toList());
                List<WorkItemField> collectSomeOne = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains(meegoParam.getTypeKey())).collect(Collectors.toList());
                for (WorkItemField workItemField : collectSomeOne) {
                    stockfields.add(new Field().setFieldKey(workItemField.getFieldKey()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setFieldName(workItemField.getFieldTypeKey()).setFieldId(workItemField.getFieldKey()));
                }
                List<WorkItemField> collectAll = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains("_all")).collect(Collectors.toList());
                for (WorkItemField workItemField : collectAll) {
                    stockfields.add(new Field().setFieldKey(workItemField.getFieldKey()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setFieldName(workItemField.getFieldTypeKey()).setFieldId(workItemField.getFieldKey()));
                }

                List<RoleField> roleFields = SignUtil.fieldAllRole(meegoParam);
                dealRoleFields(roleFields, stockfields);

                stockfields = stockfields.stream().filter(s -> s.getFieldType() != -1).collect(Collectors.toList());

                // 处理所有用户映射和关联工作项映射
                List<String> userIds = new ArrayList<>();
                List<String> workItemIds = new ArrayList<>();
                for (WorkItem workItem : workItems) {
                    userIds.add(workItem.getCreatedBy());
                    userIds.add(workItem.getUpdatedBy());
                    userIds.add(workItem.getDeletedBy());
                    for (WorkItemField workItemField : workItem.getWorkItemFields()) {
                        if ("user".equals(workItemField.getFieldTypeKey())) {
                            userIds.add(workItemField.getFieldValue());
                        }
                        if ("role_owners".equals(workItemField.getFieldTypeKey())) {
                            if (workItemField.getFieldValue() != null) {
                                JSONArray roleOwnersJsonArray = JSONArray.parseArray(workItemField.getFieldValue());
                                List<RoleFieldValue> list = roleOwnersJsonArray.toJavaList(RoleFieldValue.class);
                                for (RoleFieldValue roleFieldValue : list) {
                                    if (!ArrUtil.isEmpty(roleFieldValue.getOwners())) {
                                        userIds.addAll(roleFieldValue.getOwners());
                                    }
                                }
                            }
                        } else if ("work_item_related_select".equals(workItemField.getFieldTypeKey())) {
                            if (StrUtil.isNotEmpty(workItemField.getFieldValue())) {
                                workItemIds.add(workItemField.getFieldValue());
                            }
                        } else if ("work_item_related_multi_select".equals(workItemField.getFieldTypeKey())) {
                            if (workItemField.getFieldValue() != null) {
                                workItemIds.addAll(JSONArray.parseArray(workItemField.getFieldValue(), String.class).stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toList()));
                            }
                        }
                    }
                }
                List<String> distinctUserIds = userIds.stream().filter(a -> StrUtil.isNotEmpty(a) && !"0".equals(a)).distinct().collect(Collectors.toList());
                List<String> distinctWorkItemIds = workItemIds.stream().distinct().collect(Collectors.toList());
                List<ProjectUser> projectUsers  = SignUtil.user(meegoParam, distinctUserIds);
                log.info("需要转换的itemIds：{}", distinctWorkItemIds.size());
//                List<WorkItemTemp> workItemTemps  = SignUtil.workItemTemp(meegoParam, distinctWorkItemIds);
                List<WorkItemTemp> workItemTemps  = SignUtil.workItemRelationFieldsDetail(meegoParam, workItems);

                for (int i = 0; i < workItems.size(); i++) {

                    // 定义一个所属空间简单名称待会获取后用于拼接项目跳转链接
                    String spaceSimpleName = "";

                    Record employeeRecord = new Record();
                    Map<String, Object> map = new HashMap<>();

                    List<WorkItemField> workItemFieldTemps = workItems.get(i).getWorkItemFields();
                    for (int j = 0; j < stockfields.size(); j++) {
                        if ("id".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getId());
                        } else if ("created_at".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getCreatedAt());
                        } else if ("updated_at".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getUpdatedAt());
                        } else if ("deleted_at".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealTimestamp(workItems.get(i).getDeletedAt()));
                        } else if ("created_by".equals(stockfields.get(j).getFieldKey())) {
//                            System.out.println(workItems.get(i).getCreatedBy());
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItems.get(i).getCreatedBy()));
                        } else if ("updated_by".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItems.get(i).getUpdatedBy()));
                        } else if ("deleted_by".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItems.get(i).getDeletedBy()));
                        } else if ("name".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getName());
                        } else if ("work_item_status".equals(stockfields.get(j).getFieldKey())) {
                            out:
                            for (WorkItemField workItemField : workItemFields) {
                                if (workItemField.getFieldKey().equals("work_item_status") || workItemField.getFieldKey().equals("work_item_type_key")) {
                                    List<Option> options = workItemField.getOptions();
                                    for (Option option : options) {
                                        if (option.getValue().equals(workItems.get(i).getWorkItemStatus().getStateKey())) {
                                            map.put(stockfields.get(j).getFieldId(), option.getLabel());
                                            break out;
                                        }
                                    }
                                }
                            }
                        } else if ("work_item_type_key".equals(stockfields.get(j).getFieldKey())) {
                            out:
                            for (WorkItemField workItemField : workItemFields) {
                                if (workItemField.getFieldKey().equals("work_item_type_key")) {
                                    List<Option> options = workItemField.getOptions();
                                    for (Option option : options) {
                                        if (option.getValue().equals(meegoParam.getTypeKey())) {
                                            map.put(stockfields.get(j).getFieldId(), option.getLabel());
                                            break out;
                                        }
                                    }
                                }
                            }
                        } else if ("owned_project".equals(stockfields.get(j).getFieldKey())) {
                            String projectKey = workItems.get(i).getProjectKey();
                            for (SpaceEntity spaceEntity : spaceList) {
                                if (spaceEntity.getProjectKey().equals(projectKey)) {
                                    map.put(stockfields.get(j).getFieldId(), spaceEntity.getName());
                                    spaceSimpleName = spaceEntity.getSimpleName();
                                    break;
                                }
                            }
                        } else if ("template_type".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getTemplateType());
                        }
//                        } else if ("work_item_type_key".equals(stockfields.get(j).getFieldKey())) {
//                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getWorkItemTypeKey());
//                        }
                        for (WorkItemField workItemFieldTemp : workItemFieldTemps) {
                            if (stockfields.get(j).getFieldKey().equals(workItemFieldTemp.getFieldKey())) {
                                if ("user".equals(stockfields.get(j).getFieldName())) {
                                    map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItemFieldTemp.getFieldValue()));
                                } else if ("multi_user".equals(stockfields.get(j).getFieldName())) {
                                    map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserNameMulti(projectUsers, workItemFieldTemp.getFieldValue()));
                                } else {
                                    if ("work_item_related_select".equals(workItemFieldTemp.getFieldTypeKey())) {
                                        map.put(stockfields.get(j).getFieldId(), StringUtil.dealWorkItemRelated(workItemTemps, workItemFieldTemp.getFieldValue(), "select"));
                                    } else if ("work_item_related_multi_select".equals(workItemFieldTemp.getFieldTypeKey())) {
                                        map.put(stockfields.get(j).getFieldId(), StringUtil.dealWorkItemRelated(workItemTemps, workItemFieldTemp.getFieldValue(), "multi_select"));
                                    } else {
                                        map.put(stockfields.get(j).getFieldId(), StringUtil.dealField(workItemFieldTemp));
                                    }
                                }
                            }
                            if ("role_owners".equals(workItemFieldTemp.getFieldKey())) {
                                List<Field> collect = stockfields.stream().filter(a -> a.getIsRoleField() != null && a.getIsRoleField()).collect(Collectors.toList());
                                for (Field field : collect) {
                                    map.put(field.getFieldId(), StringUtil.dealFieldRole(projectUsers, field.getFieldKey(), workItemFieldTemp));
                                }
                            }
                        }
                    }

                    ProjectLinkEntity linkEntity = new ProjectLinkEntity();
                    String link = "https://project.feishu.cn/" + spaceSimpleName + "/" + meegoParam.getTypeKey() + "/detail/" + workItems.get(i).getId();
                    linkEntity.setName(link);
                    linkEntity.setUrl(link);
                    map.put("id88888888", linkEntity);
                    employeeRecord.setData(map);
                    employeeRecord.setPrimaryID("fid_" + workItems.get(i).getId());
                    stockRecords.add(employeeRecord);

                }
                recordResp.setRecords(stockRecords);
                recordResp.setNextPageToken(workItemPage.getNextPageToken());
                recordResp.setHasMore(workItemPage.getHasMore());
                break;
        }

        return recordResp;
//        return page(recordResp.getRecords(), params.getPageToken(), params.getMaxPageSize());
    }

    private void dealRoleFields(List<RoleField> roleFields, List<Field> stockfields) {
        if (!ArrUtil.isEmpty(roleFields)) {
            for (RoleField roleField : roleFields) {
                stockfields.add(new Field().setIsRoleField(true).setFieldKey(roleField.getId()).setFieldType(Constants.biTableText).setFieldName(roleField.getName()).setFieldId(roleField.getId()));
            }
        }
    }

    @Override
    public R<RecordResp> meegoLogin(MeegoParam meegoParam) {
        try {
//            BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
//            MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
//            String context = req.getContext();
            String login = SignUtil.tokenBody(meegoParam);
            log.info("login: {}", login);

            if (login.startsWith("p-")) {
                String spaceVerify = SignUtil.spaceVerify(meegoParam);
                if (StrUtil.isEmpty(spaceVerify)) {
                    return R.ok();
                } else {
                    return R.fail("同步失败，" + spaceVerify, null);
                }

            } else {
                return R.fail("同步失败，" + login, null);
            }
        } catch (Exception e) {
            log.error("登录异常: ", e);
            return R.fail("同步失败，网络连接异常。", null);
        }

//        log.info("stockEntityList size: {}", stockEntityList.size());
    }

    @Override
    public List<SpaceEntity> spaceList(CommonReq req) {
        try {
            BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
            MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
            String context = req.getContext();
            String token = SignUtil.token(meegoParam);
            String paramDetailJson = "{\n" +
                    "    \"user_key\": \"" + meegoParam.getUserKey() + "\"\n" +
                    "}";
            String resultString = HttpRequest.post(Constants.MEEGO_URL + Constants.PROJECTS)
                    .body(paramDetailJson)
                    .header("X-PLUGIN-TOKEN", token)
                    .header("X-USER-KEY", meegoParam.getUserKey())
                    .execute().body();

//            log.info("金蝶组织列表查询参数: {}", paramDetailJson);
            log.info("spaceList方法空间列表列表查询接口: {}", resultString);
            JSONObject resultObject = JSONObject.parseObject(resultString);
            JSONArray resultArray = resultObject.getJSONArray("data");
            List<String> projectKeys = resultArray.toJavaList(String.class);

            if (!ArrUtil.isEmpty(projectKeys)) {
                List<SpaceEntity> entities = new ArrayList<>();
//                String join = String.join(",", projectKeys);
                String join = JSONObject.toJSONString(projectKeys);
                String spaceDetailJson = "{\n" +
                        "    \"user_key\": \"" + meegoParam.getUserKey() + "\",\n" +
                        "     \"project_keys\": " + join + "\n" +
                        "}";
//                System.out.println(spaceDetailJson);
                String spaceDetailResultString = HttpRequest.post(Constants.MEEGO_URL + Constants.PROJECTS_DETAIL)
                        .body(spaceDetailJson)
                        .header("X-PLUGIN-TOKEN", token)
                        .header("X-USER-KEY", meegoParam.getUserKey())
                        .execute().body();
                log.info("spaceList方法空间详情接口: {}", spaceDetailResultString);
                JSONObject spaceDetailResultObject = JSONObject.parseObject(spaceDetailResultString);
                if (spaceDetailResultObject.getInteger("err_code") == 0) {
                    JSONObject dataObject = spaceDetailResultObject.getJSONObject("data");
                    for (String projectKey : projectKeys) {
                        String simpleName = dataObject.getJSONObject(projectKey).getString("simple_name");
                        String name = dataObject.getJSONObject(projectKey).getString("name");
                        SpaceEntity spaceEntity = new SpaceEntity().setSimpleName(simpleName).setName(name).setProjectKey(projectKey);
                        entities.add(spaceEntity);
                    }
                    return entities;
                }
            }
        } catch (Exception e) {
            log.error("接口调用失败: ", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<ItemEntity> itemList(CommonReq req) {
        try {
            BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
            MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
            String token = SignUtil.token(meegoParam);
            String itemResultString = HttpRequest.get(Constants.MEEGO_URL + "/open_api/" + meegoParam.getProjectKey() + "/work_item/all-types")
                    .header("X-PLUGIN-TOKEN", token)
                    .header("X-USER-KEY", meegoParam.getUserKey())
                    .execute().body();
            log.info("itemList方法获取空间下工作项类型接口: {}", itemResultString);
            JSONObject resultObject = JSONObject.parseObject(itemResultString);
            if (resultObject.getInteger("err_code") == 0) {
                JSONArray dataArray = resultObject.getJSONArray("data");
                List<ItemEntity> itemEntities = JSONArray.parseArray(JSONArray.toJSONString(dataArray), ItemEntity.class);
                return itemEntities;
            }
        } catch (Exception e) {
            log.error("itemList方法异常", e);
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public RecordResp page(List<Record> records, String pageToken, String maxPageSizeStr) {
        int maxPageSize;
        int currentPage;
        if (StrUtil.isEmpty(maxPageSizeStr) || "1000".equals(maxPageSizeStr)) {
            maxPageSize = 200;
        } else {
            maxPageSize = Integer.valueOf(maxPageSizeStr);
        }
        int maxPage = records.size() / maxPageSize;
        RecordResp recordResp = new RecordResp();
        if (StrUtil.isEmpty(pageToken)) {
            currentPage = 1;
        } else {
            currentPage = Integer.valueOf(pageToken);
        }
        if (records.size() / maxPageSize > 0 && currentPage <= maxPage) {
            // 至少有下一页
            recordResp.setNextPageToken(String.valueOf(currentPage + 1));
        }
        recordResp.setHasMore(currentPage <= maxPage);
        recordResp.setRecords(getPageData(records, maxPageSize, currentPage));
        return recordResp;
    }

    public static List<Record> getPageData(List<Record> data, int pageSize, int currentPage) {
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, data.size());
        if (startIndex >= data.size()) {
            return new ArrayList<>();
        }
        return data.subList(startIndex, endIndex);
    }

}
