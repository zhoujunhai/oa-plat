package com.ccb.sc.oaplat.entity.activiti;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Auditor implements Serializable {
    /**
     * 审核人
     */
    private String auditor;
    private String result;
    private Date auditTime;

}
