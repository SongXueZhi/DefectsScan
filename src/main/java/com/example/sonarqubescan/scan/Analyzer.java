package com.example.sonarqubescan.scan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.sonarqubescan.component.RestInterfaceManager;
import com.example.sonarqubescan.dao.LocationDao;
import com.example.sonarqubescan.dao.RawIssueDao;
import com.example.sonarqubescan.domin.dbo.Location;
import com.example.sonarqubescan.domin.dbo.RawIssue;
import com.example.sonarqubescan.jGitHelper.JGitHelper;
import com.example.sonarqubescan.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 4:16 下午
 */
@Slf4j
@Component
public class Analyzer {

    private static RestInterfaceManager restInterfaceManager;
    private static RawIssueDao rawIssueDao;
    private LocationDao locationDao;


    @Value("${binHome}")
    public String binHome;



    private static final String COMPONENT = "component";


    public boolean invoke(String repoUuid, String repoPath, String commit) {
        log.info("binPath is " + binHome + "executeSonar.sh");
        return ShUtil.executeCommand(binHome + "executeSonar.sh " + repoPath + " " + repoUuid + "_" + commit + " " + commit, 300);
    }


    public void compileAndInvokeTool(String repoPath, String repoUuid, String commit, JGitHelper jGitHelper) throws IOException {

        // 后续增量编译可以删除
        DirExplorer.deleteRedundantTarget(repoPath);
        //compile
        long startTime = System.currentTimeMillis();
        if (!CompileUtil.isCompilable(repoPath, repoUuid, commit)) {
            log.error("compile failed!repo path is {}, commit is {}", repoPath, commit);
            return;
        }
        long compileTime = System.currentTimeMillis();
        log.info("compile time use {} s, compile success!", (compileTime - startTime) / 1000);
        List<String> fileToScan = jGitHelper.getFilesToScan(commit);
        List<String> targetFileToScans = new ArrayList<>();
        String allRepoPath = "/home/fdse/user/sxz/projects_meta/";
        File newRepo = new File(allRepoPath + repoUuid  + "_copy");
        if(newRepo.mkdir()){
            log.info("make repo_copy success "+ newRepo.getAbsolutePath());
        }


        for(String file : fileToScan){
            if(!file.contains("src/main/java")){
                log.warn("exclude------------------------->" + file);
                continue;
            }
            String secondPrefix = "target/classes";
            StringBuilder sb = new StringBuilder(secondPrefix);

            String sourcePath = allRepoPath + repoUuid + "/meta/" + file;
            File source = new File(sourcePath);
            log.info(source.getAbsolutePath());
            String filePath  = newRepo.getAbsolutePath();
            if(source.exists()){
                FileUtil.copyFileUsingFileChannels(source, FileUtil.createFile(filePath + "/" + file) );
            }

            String firstPrefix = "src/main/java";
            String fileName = StringUtil.removePrefix(file, firstPrefix);
            sb.append(fileName);
            String sb2 = sb.toString();
            sb2 = sb2.replace(".java",".class");
            String sourcePath2 = allRepoPath + repoUuid + "/meta/" + sb2;
            File source2 = new File(sourcePath2);
            log.info(source2.getAbsolutePath());
            String filePath2  = newRepo.getAbsolutePath();
            if(source2.exists()){
                FileUtil.copyFileUsingFileChannels(source, FileUtil.createFile(filePath2 + "/" + sb2) );
            }

        }

        //2 invoke tool
        long invokeToolStartTime = System.currentTimeMillis();
        boolean invokeToolResult = invoke(repoUuid, newRepo.getAbsolutePath(), commit);
        if (!invokeToolResult) {
            log.info("invoke tool failed!repo path is {}, commit is {}", repoPath, commit);
            return;
        }
        long invokeToolTime = System.currentTimeMillis();
        log.info("invoke tool use {} s,invoke tool success!", (invokeToolTime - invokeToolStartTime) / 1000);


        //3 analyze raw issues
        boolean analyzeResult = analyze(repoPath, repoUuid, commit);
        if (!analyzeResult) {
            log.error("analyze raw issues failed!repo path is {}, commit is {}", repoPath, commit);
            return;
        }
        long analyzeToolTime = System.currentTimeMillis();
        log.info("analyze raw issues use {} s, analyze success!", (analyzeToolTime - invokeToolTime) / 1000);


        FileUtil.deleteFile(newRepo);

    }

    public boolean analyze(String repoPath, String repoUuid, String commit) {

        long analyzeStartTime = System.currentTimeMillis();
        boolean isChanged = false;
        try {
            // 最多等待200秒
            for (int i = 1; i <= 100; i++) {
                TimeUnit.SECONDS.sleep(2);
                JSONObject sonarIssueResults = restInterfaceManager.getSonarIssueResults(repoUuid + "_" + commit, null, 1, false, 0);
                if (sonarIssueResults.getInteger("total") != 0) {
                    isChanged = true;
                    long analyzeEndTime2 = System.currentTimeMillis();
                    log.info("It takes {}s to wait for the latest sonar result ", (analyzeEndTime2 - analyzeStartTime) / 1000);
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        //判断是否确实issue为0,还是没获取到这个commit的sonar结果
        if (!isChanged) {
            JSONObject sonarAnalysisTime = restInterfaceManager.getSonarAnalysisTime(repoUuid + "_" + commit);
            if (sonarAnalysisTime.containsKey(COMPONENT)) {
                isChanged = true;
                try {
                    log.info("200s past,the number of issue is 0,but get sonar analysis time,sonar result should be changed");
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //此时isChanged == false则认为解析失败
        if (!isChanged) {
            log.error("get commit {} latest sonar result failed!", commit);
            // todo return 之前存储所有的issue 结果 存储解析失败

            return false;
        }
        //解析sonar的issues为平台的rawIssue
        boolean getRawIssueSuccess = getSonarResult(repoUuid, commit, repoPath);
        //删除本次sonar库
        deleteSonarProject(repoUuid + "_" + commit);

        // todo return 之前存储所有的issue 结果

        return getRawIssueSuccess;
    }

    private void deleteSonarProject(String projectName) {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = binHome + "deleteSonarProject.sh " + projectName + " " + DatatypeConverter.printBase64Binary((restInterfaceManager.sonarLogin + ":" + restInterfaceManager.sonarPassword).getBytes(StandardCharsets.UTF_8));
            log.info("command -> {}", command);
            if (rt.exec(command).waitFor() == 0) {
                log.info("delete sonar project:{} success! ", projectName);
            }
        } catch (Exception e) {
            log.error("delete sonar project:{},cause:{}", projectName, e.getMessage());
        }
    }


    private boolean getSonarResult(String repoUuid, String commit, String repoPath) {

        //获取issue数量
        JSONObject sonarIssueResult = restInterfaceManager.getSonarIssueResults(repoUuid + "_" + commit, null, 1, false, 0);
        List<RawIssue> resultRawIssues = new ArrayList<>();
        try {
            List<Location> allLocations = new ArrayList<>();
            int pageSize = 100;
            int issueTotal = sonarIssueResult.getIntValue("total");
            log.info("Current commit {}, issueTotal in sonar result is {}", commit, issueTotal);
            //分页取sonar的issue
            int pages = issueTotal % pageSize > 0 ? issueTotal / pageSize + 1 : issueTotal / pageSize;
            for (int i = 1; i <= pages; i++) {
                JSONObject sonarResult = restInterfaceManager.getSonarIssueResults(repoUuid + "_" + commit, null, pageSize, false, i);
                JSONArray sonarRawIssues = sonarResult.getJSONArray("issues");
                //解析sonar的issues为平台的rawIssue
                for (int j = 0; j < sonarRawIssues.size(); j++) {
                    JSONObject sonarIssue = sonarRawIssues.getJSONObject(j);
                    //仅解析java文件且非test文件夹
                    String component = sonarIssue.getString(COMPONENT);
                    String rawIssueUuid = UUID.randomUUID().toString();
                    //解析location
                    List<Location> locations = getLocations(rawIssueUuid, sonarIssue, repoPath, allLocations);
                    locationDao.insertLocationList(locations);
                    if (FileFilter.javaFilenameFilter(component) || locations.isEmpty()) {
                        continue;
                    }
                    //解析rawIssue
                    RawIssue rawIssue = getRawIssue(repoUuid, commit, rawIssueUuid, sonarIssue, repoPath);
                    rawIssue.setLocations(locations);
                    resultRawIssues.add(rawIssue);
                }
            }
            rawIssueDao.insertRawIssueList(resultRawIssues);
            log.info("Current commit {}, rawIssue total is {}", commit, resultRawIssues.size());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
    private RawIssue getRawIssue(String repoUuid, String commit,  String rawIssueUuid, JSONObject issue, String repoPath) {
        //根据ruleId获取rule的name
        String issueName = null;
        JSONObject rule = restInterfaceManager.getRuleInfo(issue.getString("rule"), null, null);
        if (rule != null) {
            issueName = rule.getJSONObject("rule").getString("name");
        }
        //获取文件路径
        String[] sonarComponents;
        String sonarPath = issue.getString(COMPONENT);
        String filePath = null;
        if (sonarPath != null) {
            sonarComponents = sonarPath.split(":");
            if (sonarComponents.length >= 2) {
                filePath = sonarComponents[sonarComponents.length - 1];
            }
        }

        RawIssue rawIssue = new RawIssue();
        rawIssue.setUuid(rawIssueUuid);
        rawIssue.setType(issueName);
        rawIssue.setFileName(filePath);
        rawIssue.setDetail(issue.getString("message") + "---" + issue.getString("severity"));
        // fixme 待改，因为数据库不可为空
        rawIssue.setCommitId(commit);
        rawIssue.setRepoUuid(repoUuid);

//        String developerUniqueName = developerUniqueNameUtil.getDeveloperUniqueName(repoPath, commit, repoUuid);
//
//        rawIssue.setDeveloperName(developerUniqueName);
//        rawIssue.setPriority(getPriorityByRawIssue(rawIssue));
        return rawIssue;
    }

    public List<Location> getLocations(String rawIssueUuid, JSONObject issue, String repoPath, List<Location> allLocations) throws Exception {
        int startLine = 0;
        int endLine = 0;
        String sonarPath;
        String[] sonarComponents;
        String filePath = null;
        List<Location> locations = new ArrayList<>();
        JSONArray flows = issue.getJSONArray("flows");
        if (flows.size() == 0) {
            //第一种针对issue中的textRange存储location
            JSONObject textRange = issue.getJSONObject("textRange");
            if (textRange != null) {
                startLine = textRange.getIntValue("startLine");
                endLine = textRange.getIntValue("endLine");
            } else {
                // 无 location 行号信息的 issue 过滤掉
                return new ArrayList<>();
            }

            sonarPath = issue.getString(COMPONENT);
            if (sonarPath != null) {
                sonarComponents = sonarPath.split(":");
                if (sonarComponents.length >= 2) {
                    filePath = sonarComponents[sonarComponents.length - 1];
                }
            }

            Location mainLocation = getLocation(startLine, endLine, rawIssueUuid, filePath, repoPath);
            locations.add(mainLocation);
        } else {
            //第二种针对issue中的flows中的所有location存储
            for (int i = 0; i < flows.size(); i++) {
                JSONObject flow = flows.getJSONObject(i);
                JSONArray flowLocations = flow.getJSONArray("locations");
                //一个flows里面有多个locations， locations是一个数组，目前看sonar的结果每个locations都是一个location，但是不排除有多个。
                for (int j = 0; j < flowLocations.size(); j++) {
                    JSONObject flowLocation = flowLocations.getJSONObject(j);
                    String flowComponent = flowLocation.getString(COMPONENT);
                    JSONObject flowTextRange = flowLocation.getJSONObject("textRange");
                    if (flowTextRange == null || flowComponent == null) {
                        continue;
                    }
                    int flowStartLine = flowTextRange.getIntValue("startLine");
                    int flowEndLine = flowTextRange.getIntValue("endLine");
                    String flowFilePath = null;

                    String[] flowComponents = flowComponent.split(":");
                    if (flowComponents.length >= 2) {
                        flowFilePath = flowComponents[flowComponents.length - 1];
                    }

                    Location location = getLocation(flowStartLine, flowEndLine, rawIssueUuid, flowFilePath, repoPath);
                    locations.add(location);
                }
            }
        }

        allLocations.addAll(locations);
        return locations;
    }

    private Location getLocation(int startLine, int endLine, String rawIssueId, String filePath, String repoPath) {
        Location location = new Location();
        String locationUuid = UUID.randomUUID().toString();
        //获取相应的code
        String code = null;
        try {
            code = AstUtil.getCode(startLine, endLine, repoPath + "/" + filePath);
        } catch (Exception e) {
            log.info("file path --> {} file deleted", repoPath + "/" + filePath);
            log.error("rawIssueId --> {}  get code failed.", rawIssueId);

        }

        location.setCode(code);
        location.setUuid(locationUuid);
        location.setStartLine(startLine);
        location.setEndLine(endLine);
        if (startLine > endLine) {
            log.error("startLine > endLine,fileName is {},startLine is {},endLine is {}", filePath, startLine, endLine);
            int temp = startLine;
            startLine = endLine;
            endLine = temp;
        }
        location.setBugLines(startLine + "-" + endLine);
        location.setFilePath(filePath);
        location.setRawIssueId(rawIssueId);
        // todo location 方法名解析
//        Object[] methodNameAndOffset = AstParserUtil.findMethodNameAndOffset(repoPath + "/" + filePath, startLine, endLine);
//        if (methodNameAndOffset != null) {
//            location.setMethodName((String) methodNameAndOffset[0]);
//            location.setOffset((int) methodNameAndOffset[1]);
//        }

        return location;
    }

    public void scan(String repoPath, String repoUuid, String commit) throws IOException {
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        if (jGitHelper.checkout(commit)) {
            compileAndInvokeTool(jGitHelper.getRepoPath(), repoUuid, commit, jGitHelper);
        }



    }

    @Autowired
    private void setRawIssueDao(RawIssueDao rawIssueDao){
        Analyzer.rawIssueDao = rawIssueDao;
    }

    @Autowired
    private void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        Analyzer.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    private void setLocationDao(LocationDao locationDao){
        this.locationDao = locationDao;
    }
}
