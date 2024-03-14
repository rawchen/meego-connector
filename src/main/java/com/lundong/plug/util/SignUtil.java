package com.lundong.plug.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lundong.plug.config.Constants;
import com.lundong.plug.entity.ProjectUser;
import com.lundong.plug.entity.WorkItem;
import com.lundong.plug.entity.WorkItemField;
import com.lundong.plug.entity.param.MeegoParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 10:18
 */
@Slf4j
public class SignUtil {

    public static String token(MeegoParam param) {
        String bodyStr = "";
        String loginUrl = Constants.MEEGO_URL + Constants.PLUGIN_TOKEN;
        String loginJson = "{\n" +
                "    \"plugin_id\": \"" + param.getPluginId() + "\",\n" +
                "    \"plugin_secret\": \"" + param.getPluginSecret() + "\"\n" +
                "}";
        try {
            HttpResponse tokenResponse = HttpRequest.post(loginUrl)
                    .body(loginJson)
                    .execute();
            if (tokenResponse != null && StrUtil.isNotEmpty(tokenResponse.body())) {
                bodyStr = tokenResponse.body();
            } else {
                return "";
            }
//            log.info("plugin_token接口: {}", bodyStr);
            JSONObject jsonObject = JSONObject.parseObject(tokenResponse.body());
            if (jsonObject.getJSONObject("error").getInteger("code") == 0) {
                return jsonObject.getJSONObject("data").getString("token");
            } else {
                log.error("plugin_token接口出错: {}", bodyStr);
                return "";
            }
        } catch (Exception e) {
            log.error("plugin_token接口出错", e);
            return "";
        }

    }

    public static String tokenBody(MeegoParam param) {
        String bodyStr = "";
        String loginUrl = Constants.MEEGO_URL + Constants.PLUGIN_TOKEN;
        String loginJson = "{\n" +
                "    \"plugin_id\": \"" + param.getPluginId() + "\",\n" +
                "    \"plugin_secret\": \"" + param.getPluginSecret() + "\"\n" +
                "}";
        try {
            HttpResponse tokenResponse = HttpRequest.post(loginUrl)
                    .body(loginJson)
                    .execute();
            if (tokenResponse != null && StrUtil.isNotEmpty(tokenResponse.body())) {
                bodyStr = tokenResponse.body();
            } else {
                return "";
            }
//            log.info("plugin_token接口: {}", bodyStr);
            JSONObject jsonObject = JSONObject.parseObject(tokenResponse.body());
            if (jsonObject.getJSONObject("error").getInteger("code") == 0) {
                return jsonObject.getJSONObject("data").getString("token");
            } else {
                log.error("tokenBody方法出错: {}", bodyStr);
                return jsonObject.getJSONObject("error").getString("msg");
            }
        } catch (Exception e) {
            log.error("plugin_token接口出错: {}", e.getMessage());
            return e.getMessage();
        }

    }

    public static String spaceVerify(MeegoParam meegoParam) {
        String token = SignUtil.token(meegoParam);
//            String resultString = api.executeBillQueryJson("");
        String paramDetailJson = "{\n" +
                "    \"user_key\": \"" + meegoParam.getUserKey() + "\"\n" +
                "}";
        String resultString = HttpRequest.post(Constants.MEEGO_URL + Constants.PROJECTS)
                .body(paramDetailJson)
                .header("X-PLUGIN-TOKEN", token)
                .header("X-USER-KEY", meegoParam.getUserKey())
                .execute().body();

//            log.info("金蝶组织列表查询参数: {}", paramDetailJson);
        log.info("spaceVerify方法空间列表接口: {}", resultString);
        JSONObject object = JSONObject.parseObject(resultString);
        return object.getString("err_msg");
    }

    /**
     * 获取指定的工作项列表(非跨空间)
     *
     * @param meegoParam
     * @return
     */
    public static List<WorkItem> workItemList(MeegoParam meegoParam) {
        List<WorkItem> workItemList = new ArrayList<>();
        String token = SignUtil.token(meegoParam);
        try {
            boolean haseMore = true;
            JSONArray resultArrayNew = new JSONArray();
            int pageNumber = 1;
            while (haseMore) {
                String paramDetailJson = "{\n" +
                        "    \"work_item_type_keys\": [\"" + meegoParam.getTypeKey() + "\"],\n" +
                        "    \"page_size\": 50,\n" +
                        "    \"page_num\": " + pageNumber + "\n" +
                        "}";

                String resultString = HttpRequest.post(Constants.MEEGO_URL + "/open_api/" + meegoParam.getProjectKey() + Constants.WORK_ITEM_FILTER)
                        .body(paramDetailJson)
                        .header("X-PLUGIN-TOKEN", token)
                        .header("X-USER-KEY", meegoParam.getUserKey())
                        .execute().body();

                //            log.info("金蝶组织列表查询参数: {}", paramDetailJson);
                log.info("workItemList方法获取指定的工作项列表(非跨空间)接口: {}", resultString);
                JSONObject jsonObject = JSONObject.parseObject(resultString);
                JSONArray resultArray;
                if (jsonObject.getInteger("err_code") == 0) {
                    if (jsonObject.getJSONArray("data") != null && !ArrUtil.isEmpty(jsonObject.getJSONArray("data"))) {
                        resultArray = jsonObject.getJSONArray("data");
                    } else {
                        haseMore = false;
                        resultArray = new JSONArray();
                    }
                } else {
                    log.error("workItemList方法获取指定的工作项列表(非跨空间)接口: {}", resultString);
                    return Collections.emptyList();
                }
                if (resultArray != null && !resultArray.isEmpty()) {
                    resultArrayNew.addAll(resultArray);
                }
                pageNumber++;
            }

            // 解析
            for (int i = 0; i < resultArrayNew.size(); i++) {
                WorkItem workItem = JSONArray.toJavaObject(resultArrayNew.getJSONObject(i), WorkItem.class);
                System.out.println(workItem);
                workItemList.add(workItem);
            }
        } catch (Exception e) {
            log.error("workItemList方法异常：", e);
        }
        return workItemList;
    }

    /**
     * 获取指定的工作项列表(非跨空间)
     *
     * @param meegoParam
     * @return
     */
    public static List<WorkItemField> fieldAll(MeegoParam meegoParam) {
        List<WorkItemField> workItemFieldList = new ArrayList<>();
        String token = SignUtil.token(meegoParam);
        try {
            String resultString = HttpRequest.post(Constants.MEEGO_URL + "/open_api/" + meegoParam.getProjectKey() + Constants.FIELD_ALL)
                    .header("X-PLUGIN-TOKEN", token)
                    .header("X-USER-KEY", meegoParam.getUserKey())
                    .execute().body();

            //            log.info("金蝶组织列表查询参数: {}", paramDetailJson);
            log.info("fieldAll方法获取空间字段接口: {}", resultString);
            JSONObject jsonObject = JSONObject.parseObject(resultString);
            if (jsonObject.getInteger("err_code") == 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (jsonArray != null && !ArrUtil.isEmpty(jsonArray)) {
                    workItemFieldList = JSONArray.parseArray(jsonArray.toJSONString(), WorkItemField.class);
                    return workItemFieldList;
                }
            } else {
                log.error("fieldAll方法获取空间字段接口: {}", resultString);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("fieldAll方法异常：", e);
        }
        return workItemFieldList;
    }

    public static String genPostRequestSignature(String nonce, String timestamp, String body, String secretKey) {
        String str = "";
        str += timestamp;
        str += nonce;
        str += secretKey;
        str += body;

        byte[] bytes = str.getBytes();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(bytes);
            byte[] sha1Bytes = digest.digest();
            return bytesToHexString(sha1Bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String decrypt(String params) {
        try {
            if (StrUtil.isEmpty(params)) {
                return "";
            }
            DES des = new DES(Constants.SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            log.info("解密结果: {}", des.decryptStr(params, CharsetUtil.CHARSET_UTF_8));
            return des.decryptStr(params, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            log.error("解密异常", e);
            return "";
        }
    }

    public static String rsaDecrypt(String text) {
        try {
            String publicKey = StreamUtils.copyToString(new ClassPathResource("public.txt").getInputStream(), Charset.defaultCharset());
            String privateKey = StreamUtils.copyToString(new ClassPathResource("private.txt").getInputStream(), Charset.defaultCharset());

            RSA rsa = SecureUtil.rsa(privateKey, publicKey);
            String result = rsa.decryptStr(text, KeyType.PrivateKey);
            log.info("解密结果：{}", result);
            return result;
        } catch (IOException e) {
            log.error("解密异常：", e);
            return "";
        }
    }

    public static List<ProjectUser> user(MeegoParam meegoParam, List<String> distinctUserIds) {
        List<ProjectUser> projectUserList = new ArrayList<>();
        String token = SignUtil.token(meegoParam);
        try {
            JSONObject objectParam = new JSONObject();
            JSONArray jsonArrayParam = new JSONArray();
            jsonArrayParam.addAll(distinctUserIds);
            objectParam.put("user_keys", jsonArrayParam);
            String resultString = HttpRequest.post(Constants.MEEGO_URL + Constants.USER_QUERY)
                    .body(objectParam.toJSONString())
                    .header("X-PLUGIN-TOKEN", token)
                    .header("X-USER-KEY", meegoParam.getUserKey())
                    .execute().body();
            log.info("user方法获取用户详情列表接口: {}", resultString);
            JSONObject jsonObject = JSONObject.parseObject(resultString);
            if (jsonObject.getInteger("err_code") == 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (jsonArray != null && !ArrUtil.isEmpty(jsonArray)) {
                    projectUserList = JSONArray.parseArray(jsonArray.toJSONString(), ProjectUser.class);
                    return projectUserList;
                }
            } else {
                log.error("user方法获取用户详情列表接口出错: {}", resultString);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("user方法异常：", e);
        }
        return projectUserList;
    }
}
