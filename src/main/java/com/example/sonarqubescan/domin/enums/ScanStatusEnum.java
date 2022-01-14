package com.example.sonarqubescan.domin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 4:24 下午
 */
@Getter
@AllArgsConstructor
public enum ScanStatusEnum {
    /**
     * issue scan status
     */
    DOING("doing"),
    CHECKOUT_FAILED("checkout failed"),
    COMPILE_FAILED("compile failed"),
    INVOKE_TOOL_FAILED("invoke tool failed"),
    ANALYZE_FAILED("analyze failed"),
    PERSIST_FAILED("persist failed"),
    MATCH_FAILED("match failed"),
    STATISTICAL_FAILED("statistical failed"),
    DONE("done");

    private final String type;
}
