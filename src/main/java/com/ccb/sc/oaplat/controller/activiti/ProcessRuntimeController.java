package com.ccb.sc.oaplat.controller.activiti;

import com.ccb.sc.oaplat.common.utils.SecurityUtil;
import com.ccb.sc.oaplat.entity.activiti.Vacation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class ProcessRuntimeController {
    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 查询流程定义列表
     * @return
     */
    @GetMapping("/processDefinitions")
    public Page<ProcessDefinition> processDefinitions(){
        securityUtil.logInAs("system");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 10));

        return processDefinitionPage;
    }

    @GetMapping("/start")
    public String start(){

        securityUtil.logInAs("zjh.sc");

        Vacation vacation = new Vacation();
        vacation.setApplyUser("zjh");
        vacation.setDays(10);
        vacation.setReason("年假");
        vacation.setApplyTime(new Date());

        Date now = new Date();
        Date afterDate1 = new Date(now.getTime() + 20*1000);
        Date afterDate2 = new Date(now.getTime() + 60*1000);

        Map<String, Object> vars = new HashMap<>();
        //设置申请参数信息
        vars.put("vacation",vacation);
        //设置候选人信息
        vars.put("userIds1", "zjh.sc,zs,ls");
        vars.put("userIds2", "zjh.sc,zs,zl");
        vars.put("userIds3", "zjh.sc,zs,ww");
        vars.put("userIds4", "zjh.sc,zs,ww");
        vars.put("userIds5", "zjh.sc,zs,ww");
        vars.put("endtime1", afterDate1);
        vars.put("endtime2", afterDate2);
        vars.put("recv", "739516635@qq.com");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("SuperviseProcess")
                .withVariables(vars)
                .build());

        return processInstance.getId();
    }
}
