package com.example.sonarqubescan.domin.enums;

import lombok.Getter;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 4:04 下午
 */
@Getter
public enum JdkVersionEnum {
    /**
     * jdk版本
     */
    JDK_8("1.8"),
    JDK_11("11"),
    JDK_12("12"),
    JDK_16("16");

    private final String version;

    JdkVersionEnum(String version) {
        this.version = version;
    }
}
