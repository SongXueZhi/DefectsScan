package com.example.sonarqubescan.dao;

import com.example.sonarqubescan.mapper.RawIssueMapper;
import com.example.sonarqubescan.domin.dbo.RawIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/16 9:28 上午
 */
@Repository
public class RawIssueDao {

    RawIssueMapper rawIssueMapper;

    public void insertRawIssueList(List<RawIssue> list) {
        if (list.isEmpty()) {
            return;
        }
        rawIssueMapper.insertRawIssueList(list);
    }
    @Autowired
    private void setRawIssueMapper(RawIssueMapper rawIssueMapper){
        this.rawIssueMapper = rawIssueMapper;
    }
}
