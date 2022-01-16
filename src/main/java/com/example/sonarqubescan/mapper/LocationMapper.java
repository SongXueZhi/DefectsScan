package com.example.sonarqubescan.mapper;

import com.example.sonarqubescan.domin.dbo.Location;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/16 9:36 上午
 */
@Repository
public interface LocationMapper {
    void insertLocationList(List<Location> locations);
}
