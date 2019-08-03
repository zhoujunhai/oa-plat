package com.ccb.sc.oaplat.controller.activiti;

import com.ccb.sc.oaplat.common.utils.SecurityUtil;
import com.ccb.sc.oaplat.entity.activiti.Content;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Slf4j
public class ApiBaseFullController implements CommandLineRunner {
    private final ProcessRuntime processRuntime;

    private final TaskRuntime taskRuntime;

    private final SecurityUtil securityUtil;

    public ApiBaseFullController(ProcessRuntime processRuntime, TaskRuntime taskRuntime, SecurityUtil securityUtil) {
        this.processRuntime = processRuntime;
        this.taskRuntime = taskRuntime;
        this.securityUtil = securityUtil;
    }

    @Override
    public void run(String... args) {
        securityUtil.logInAs("system");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 10));
        log.info("> Available Process definitions: " + processDefinitionPage.getTotalItems());
        for (ProcessDefinition pd : processDefinitionPage.getContent()) {
            log.info("\t > Process definition: " + pd);
        }

    }

//    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    @GetMapping("/processText")
    public void processText() {

        securityUtil.logInAs("system");

        Content content = pickRandomString();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

        log.info("> Starting process to process content: " + content + " at " + formatter.format(new Date()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("content", content);

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("categorizeHumanProcess")
                .withVariables(vars)
                .build());
        log.info(">>> Created Process Instance: " + processInstance);


    }

//    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    @GetMapping("/checkAndWorkOnTasksWhenAvailable")
    public void checkAndWorkOnTasksWhenAvailable() {
        securityUtil.logInAs("salaboy");

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
        if (tasks.getTotalItems() > 0) {
            for (Task t : tasks.getContent()) {

                log.info("> Claiming task: " + t.getId());
                taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(t.getId()).build());

                List<VariableInstance> variables = taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(t.getId()).build());
                VariableInstance variableInstance = variables.get(0);
                if (variableInstance.getName().equals("content")) {
                    Content contentToProcess = variableInstance.getValue();
                    log.info("> Content received inside the task to approve: " + contentToProcess);

                    if (contentToProcess.getBody().contains("activiti")) {
                        log.info("> User Approving content");
                        contentToProcess.setApproved(true);
                    } else {
                        log.info("> User Discarding content");
                        contentToProcess.setApproved(false);
                    }
                    taskRuntime.complete(TaskPayloadBuilder.complete()
                            .withTaskId(t.getId()).withVariable("content", contentToProcess).build());
                }


            }

        } else {
            log.info("> There are no task for me to work on.");
        }

    }

    private Content pickRandomString() {
        String[] texts = {"hello from london", "Hi there from activiti!", "all good news over here.", "I've tweeted about activiti today.",
                "other boring projects.", "activiti cloud - Cloud Native Java BPM"};
        return new Content(texts[new Random().nextInt(texts.length)],false,null);
    }
}
