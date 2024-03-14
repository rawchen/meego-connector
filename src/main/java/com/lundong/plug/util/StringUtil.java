package com.lundong.plug.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lundong.plug.entity.WorkItemField;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shuangquan.chen
 * @date 2023-11-24 14:40
 */
@Slf4j
public class StringUtil {

    public static Object dealField(List<WorkItemField> workItemFields, String str) {
        if (StrUtil.isEmpty(str)) {
            return null;

        }
        List<WorkItemField> collect = workItemFields.stream().filter(a -> str.equals(a.getFieldKey())).collect(Collectors.toList());
        if (collect.size() != 1) {
            return null;
        } else {
            WorkItemField field = collect.get(0);
            switch (field.getFieldTypeKey()) {
                case "text":
                case "link":
                    return field.getFieldValue();
                case "date":
                    // 毫秒级别时间戳
                    return Long.valueOf(field.getFieldValue());
                case "schedule":
                    JSONObject object = JSONObject.parseObject(field.getFieldValue());
                    Long startTime = object.getLong("start_time");
                    Long endTime = object.getLong("end_time");
                    String startTimeStr = timestampToYearMonthDayHourMinuteSecond(startTime, "yyyy/MM/dd HH:mm:ss");
                    String endTimeStr = timestampToYearMonthDayHourMinuteSecond(endTime, "yyyy/MM/dd HH:mm:ss");
                    return startTimeStr + "-" + endTimeStr;
                case "number":
                    return Double.valueOf(field.getFieldValue());
                case "work_item_related_select":
                    // 关联工作项
                    return field.getFieldValue();
                case "work_item_related_multi_select":
                    return JSONArray.parseArray(field.getFieldValue(), String.class).stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
                case "signal":
                case "bool":
                case "deleted":
                    return Boolean.valueOf(field.getFieldValue());
                case "radio":
                case "select":
                    JSONObject radioObject = JSONObject.parseObject(field.getFieldValue());
                    return radioObject.getString("label");
                case "multi_select":
                    JSONArray jsonArray = JSONArray.parseArray(field.getFieldValue());
                    List<String> multiSelectList = jsonArray.stream().map(o -> (JSONObject) o)
                            .map(o -> o.getString("value"))
                            .collect(Collectors.toList());
                    return multiSelectList;
                case "user":
                    return field.getFieldValue();
                case "multi_user":
                    if (field.getFieldValue() == null) {
                        return Collections.emptyList();
                    }
                    List<String> stringList = JSONArray.parseArray(field.getFieldValue()).toJavaList(String.class);
                    return stringList;
//                    if (field.getFieldValue() == null) {
//                        return "";
//                    }
//                    return String.join(",", JSONArray.parseArray(field.getFieldValue(), String.class));
                case "multi_text":
                    return field.getFieldValue();
                case "file":
                    JSONObject fileObject = JSONObject.parseObject(field.getFieldValue());
                    return fileObject.getString("name");
                case "aborted":
                    JSONObject abortedObject = JSONObject.parseObject(field.getFieldValue());
                    return abortedObject.getBoolean("is_aborted");
                case "linked_work_item":
                    JSONObject linkedWorkItemObject = JSONObject.parseObject(field.getFieldValue());
                    return linkedWorkItemObject.getString("name");
                case "business":
                case "chat_group":
                case "group_id":
                case "group_type":
                    return field.getFieldValue();
                case "work_item_template":
                    JSONObject workItemTemplateObject = JSONObject.parseObject(field.getFieldValue());
                    return workItemTemplateObject.getInteger("id");
                case "role_owners":
                    if (field.getFieldValue() == null) {
                        return Collections.emptyList();
                    }
                    JSONArray roleOwnersJsonArray = JSONArray.parseArray(field.getFieldValue());
                    List<String> roleOwnersMultiSelectList = roleOwnersJsonArray.stream().map(o -> (JSONObject) o)
                            .map(o -> o.getString("role"))
                            .collect(Collectors.toList());
                    return roleOwnersMultiSelectList;
            }
        }
        return null;
    }

    /**
     * 13位时间戳转自定义格式日期字符串
     *
     * @param instanceOperateTime   13位时间戳
     * @param pattern               yyyy/MM/dd HH:mm:ss
     * @return
     */
    public static String timestampToYearMonthDayHourMinuteSecond(Long instanceOperateTime, String pattern) {
        LocalDateTime operateTime;
        if (StrUtil.isEmpty(String.valueOf(instanceOperateTime)) || String.valueOf(instanceOperateTime).length() != 13) {
            log.error("13位时间戳格式出错：{}", instanceOperateTime);
            operateTime = LocalDateTime.now();
        } else {
            operateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(instanceOperateTime), ZoneId.systemDefault());
        }
        return operateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String yearMonthDayHourMinuteSecondToTimestamp(String instanceOperateTime) {
        LocalDateTime operateTime = LocalDateTime.now();
        try {
            operateTime = LocalDateTime.parse(instanceOperateTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        } catch (Exception e) {
            log.error("时间解析出错", e);
        }
        long milliseconds = operateTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
        return String.valueOf(milliseconds);
    }
}
