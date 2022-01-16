package com.example.sonarqubescan.Mapper;

import com.example.sonarqubescan.domin.dbo.RawIssue;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/16 8:48 上午
 */
@Repository
public interface RawIssueMapper {
    void insertRawIssueList(List<RawIssue> rawIssueList);
}
