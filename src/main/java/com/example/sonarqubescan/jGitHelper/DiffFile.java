package com.example.sonarqubescan.jGitHelper;

import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 11:34 上午
 */

public class DiffFile {
    List<String> addFiles;
    List<String> deleteFiles;
    Map<String, String> changeFiles;

    public DiffFile(List<String> addFiles, List<String> deleteFiles, Map<String, String> changeFiles) {
        this.addFiles = ImmutableList.copyOf(addFiles);
        this.deleteFiles = ImmutableList.copyOf(deleteFiles);
        this.changeFiles = ImmutableMap.copyOf(changeFiles);
    }
}
