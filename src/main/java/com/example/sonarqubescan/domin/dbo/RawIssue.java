package com.example.sonarqubescan.domin.dbo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 5:31 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RawIssue {
    private String uuid;
    private String type;
    private String tool;
    private String detail;
    private String fileName;
    private String scanId;

    private String commitId;
    private String repoUuid;
    private int codeLines;
    private Date commitTime;
    private List<Location> locations;
    private int version = 1;
    private int priority;


    @Override
    public String toString() {
        return "{uuid=" + uuid + ",type=" + type + ",tool=" + tool + ",detail=" + detail + "}";
    }

    /**
     * 因为在bugMapping中被作为key，故不可以随意删除,且不可加入mapped
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + detail.hashCode();
        result = 31 * result + scanId.hashCode();
        result = 31 * result + commitId.hashCode();
        return result;
    }

    /**
     * 因为在bugMapping中被作为key，故不可以随意删除，且不可加入mapped
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RawIssue)) {
            return false;
        }

        return this.getUuid().equals(((RawIssue) obj).getUuid());
    }
}
