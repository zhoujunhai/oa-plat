package com.ccb.sc.oaplat.controller.activiti;

import com.ccb.sc.oaplat.common.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TaskRuntimeController {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/create")
    public String create(){

        securityUtil.logInAs("zjh.sc");

        log.info("> Creating a Group Task for 'activitiTeam'");
        taskRuntime.create(TaskPayloadBuilder.create()
                .withName("First Team Task")
                .withDescription("This is something really important")
                .withGroup("activitiTeam")
                .withPriority(10)
                .build());

        return "create success";
    }

    @GetMapping("/allTasks")
    public Page<Task> allTasks(){

        securityUtil.logInAs("zjh.sc");

        log.info("> Getting all the tasks");

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));

        return tasks;
    }
    @GetMapping("/claim")
    public String claim(String taskId){
        securityUtil.logInAs("zjh.sc");

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(taskId).build());

        return "claim success";
    }
    @GetMapping("/complete")
    public String complete(String taskId){
        securityUtil.logInAs("zjh.sc");

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(taskId).build());

        return "complete success";
    }
}
