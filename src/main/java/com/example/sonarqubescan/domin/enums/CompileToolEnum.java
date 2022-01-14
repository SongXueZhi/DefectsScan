package com.example.sonarqubescan.domin.enums;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 4:06 下午
 */
public enum CompileToolEnum {
    /**
     * 编译工具的名字
     *
     * @value 编译工具需要的文件名
     */
    MAVEN("pom.xml"),
    GRADLE("build.gradle");

    private final String compileFile;

    CompileToolEnum(String compileFile) {
        this.compileFile = compileFile;
    }

    public String compileFile() {
        return this.compileFile;
    }
}
