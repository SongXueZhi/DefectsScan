package com.example.sonarqubescan.domin.dbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 4:33 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
    private String uuid;
    private int startLine;
    private int endLine;
    private String bugLines;
    private int startToken;
    private int endToken;
    private String filePath;
    private String className;
    private String methodName;
    private String rawIssueId;
    private String code;

    private String repoUuid;

    /**
     * location 起始位置相对于 所在方法或者属性起始位置的偏移量
     */
    private int offset = 0;

    private boolean matched = false;
    private int matchedIndex = -1;

    private List<Byte> tokens = null;




    public boolean isSame(Location location) {
        if (StringUtils.isEmpty(methodName) || StringUtils.isEmpty(code) ||
                StringUtils.isEmpty(location.getMethodName()) || StringUtils.isEmpty(location.getCode())) {
            return false;
        }

        return methodName.equals(location.getMethodName()) && code.equals(location.getCode());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location location = (Location) obj;
        if (this.className != null && location.className != null
                && this.methodName != null && location.methodName != null) {
            if (bugLines == null && location.bugLines == null) {
                return location.className.equals(className) &&
                        location.methodName.equals(methodName) &&
                        location.filePath.equals(filePath);
            } else if (bugLines != null && location.bugLines != null) {

                return location.className.equals(className) &&
                        location.methodName.equals(methodName) &&
                        location.filePath.equals(filePath) &&
                        bugLines.split(",").length == location.bugLines.split(",").length;

            }

        }
        return false;
    }

}
