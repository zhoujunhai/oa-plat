package com.ccb.sc.oaplat.entity.activiti;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Vacation implements Serializable {

    /**
     * 申请人
     */
    private String applyUser;
    private int days;
    private String reason;
    private Date applyTime;
    private String applyStatus;
}
