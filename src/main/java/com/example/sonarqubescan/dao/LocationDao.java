package com.example.sonarqubescan.dao;

import com.example.sonarqubescan.Mapper.LocationMapper;
import com.example.sonarqubescan.domin.dbo.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/16 9:42 上午
 */
@Repository
public class LocationDao {
    private LocationMapper locationMapper;

    void insertLocationList(List<Location> list){
        if (list.isEmpty()) {
            return;
        }
        locationMapper.insertLocationList(list);

    }
    @Autowired
    private void setLocationMapper(LocationMapper locationMapper){
        this.locationMapper = locationMapper;
    }

}
