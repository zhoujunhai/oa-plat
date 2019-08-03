package com.ccb.sc.oaplat.entity.activiti;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class VacTask implements Serializable {

    private String id;
    private String name;
    private Vacation vac;
    private Date createTime;

}
