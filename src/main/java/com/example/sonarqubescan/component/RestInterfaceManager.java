package com.example.sonarqubescan.component;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 4:28 下午
 */
@Component
@Slf4j
public class RestInterfaceManager {


    protected RestTemplate restTemplate = new RestTemplate();

    @Value("${sonar.service.path}")
    private String sonarServicePath;
    @Value("${sonar.login}")
    public String sonarLogin;
    @Value("${sonar.password}")
    public String sonarPassword;

    private boolean initSonarAuth = false;
    private HttpEntity<HttpHeaders> sonarAuthHeader;


    private void initSonarAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        String encoding = DatatypeConverter.printBase64Binary((sonarLogin + ":" + sonarPassword).getBytes(StandardCharsets.UTF_8));
        headers.add("Authorization", "Basic " + encoding);
        this.sonarAuthHeader = new HttpEntity<>(headers);
        initSonarAuth = true;
    }
    public JSONObject getSonarIssueResults(String repoName, String type, int pageSize, boolean resolved, int page) {

        if (!initSonarAuth) {
            initSonarAuthorization();
        }

        Map<String, String> map = new HashMap<>(16);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(sonarServicePath).append("/api/issues/search?componentKeys={componentKeys}&additionalFields={additionalFields}&s={s}&resolved={resolved}");
        map.put("additionalFields", "_all");
        map.put("s", "FILE_LINE");
        map.put("componentKeys", repoName);
        map.put("resolved", String.valueOf(resolved));
        if (type != null) {
            String[] types = type.split(",");
            StringBuilder stringBuilder = new StringBuilder();
            for (String typeSb : types) {
                if ("CODE_SMELL".equals(typeSb) || "BUG".equals(typeSb) || "VULNERABILITY".equals(typeSb) || "SECURITY_HOTSPOT".equals(typeSb)) {
                    stringBuilder.append(typeSb).append(",");
                }
            }
            if (!stringBuilder.toString().isEmpty()) {
                urlBuilder.append("&componentKeys={componentKeys}");
                String requestTypes = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
                map.put("types", requestTypes);
            } else {
                log.error("this request type --> {} is not available in sonar api", type);
                return null;
            }
        }

        if (page > 0) {
            urlBuilder.append("&p={p}");
            map.put("p", String.valueOf(page));
        }
        if (pageSize > 0) {
            urlBuilder.append("&ps={ps}");
            map.put("ps", String.valueOf(pageSize));
        }

        String url = urlBuilder.toString();

        try {
            ResponseEntity<JSONObject> entity = restTemplate.exchange(url, HttpMethod.POST, sonarAuthHeader, JSONObject.class, map);
            return JSONObject.parseObject(Objects.requireNonNull(entity.getBody()).toString());
        } catch (RuntimeException e) {
            log.error("repo name : {}  ----> request sonar api failed", repoName);
            throw e;
        }
    }
    public JSONObject getSonarAnalysisTime(String projectName) {

        if (!initSonarAuth) {
            initSonarAuthorization();
        }

        JSONObject error = new JSONObject();
        error.put("errors", "Component key " + projectName + " not found");

        try {
            String urlPath = sonarServicePath + "/api/components/show?component=" + projectName;
            log.debug(urlPath);
            return restTemplate.exchange(urlPath, HttpMethod.GET, sonarAuthHeader, JSONObject.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("projectName: {} ---> request sonar api failed 获取最新版本时间API 失败", projectName);
        }

        return error;
    }
    public JSONObject getRuleInfo(String ruleKey, String actives, String organizationKey) {

        if (!initSonarAuth) {
            initSonarAuthorization();
        }

        Map<String, String> map = new HashMap<>(64);

        String baseRequestUrl = sonarServicePath + "/api/rules/show";
        if (ruleKey == null) {
            log.error("ruleKey is missing");
            return null;
        } else {
            map.put("key", ruleKey);
        }
        if (actives != null) {
            map.put("actives", actives);
        }
        if (organizationKey != null) {
            map.put("organization", organizationKey);
        }

        try {
            return restTemplate.exchange(baseRequestUrl + "?key=" + ruleKey, HttpMethod.GET, sonarAuthHeader, JSONObject.class).getBody();
        } catch (RuntimeException e) {
            log.error("ruleKey : {}  ----> request sonar  rule information api failed", ruleKey);
            throw e;
        }

    }




}
