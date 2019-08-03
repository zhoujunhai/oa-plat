package com.ccb.sc.oaplat.controller.activiti;

import com.ccb.sc.oaplat.common.utils.UserUtils;
import com.ccb.sc.oaplat.entity.activiti.Auditor;
import com.ccb.sc.oaplat.entity.activiti.VacTask;
import com.ccb.sc.oaplat.entity.activiti.Vacation;
import com.ccb.sc.oaplat.entity.vo.Json;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@Slf4j
public class SuperviseController {
    /**
     *
     */
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
//    @Autowired
//    private IdentityService identityService;
    @Autowired
    private HistoryService historyService;


//    private static final String PROCESS_DEFINE_KEY = "myProcess_2";

    @GetMapping("/startProcessInstance")
    public Json startProcessInstance(@RequestParam String processDefineKey){
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

        //根据角色取出角色下所有用户，并设置到请求参数中Candidate
//        SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
//        identityService.setAuthenticatedUserId(user.getUname());
//        identityService.setAuthenticatedUserId(UserUtils.getUserName());

        //开始流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefineKey,vars);

        return Json.succ("申请请假成功",processInstance.getId());
    }
    @GetMapping("/showClaimTaskList")
    public Json showTaskList(){
        //查询待签收任务
        List<Task> taskList = taskService.createTaskQuery().taskCandidateUser(UserUtils.getUserName()).list();
        ////查询任务申请参数
        List<VacTask> vacTasksList = getVacTaskList(taskList);

        return Json.succ("查询待签收任务列表成功").data("vacationsList",vacTasksList);
    }
    @GetMapping("/showAssigneeTaskList")
    public Json showAssigneeTaskList(){
        //查询已签收任务
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(UserUtils.getUserName()).list();

        //查询任务申请参数
        List<VacTask> vacTasksList = getVacTaskList(taskList);

        return Json.succ("查询已签收任务列表成功").data("vacationsList",vacTasksList);
    }

    public List<VacTask> getVacTaskList(List<Task> taskList){
        //查询任务申请参数
        List<VacTask> vacTasksList = new ArrayList<>();
        for (Task task : taskList) {
            Map<String, Object> vars = new HashMap<>();
            vars = taskService.getVariables(task.getId());

            Vacation vacation = (Vacation) vars.get("vacation");
            VacTask vacTask = new VacTask();
            vacTask.setVac(vacation);
            vacTask.setId(task.getId());

            vacTasksList.add(vacTask);
        }

        return vacTasksList;
    }


    @GetMapping("/claimTask")
    public Json claimTask(@RequestParam String taskId){
        taskService.claim(taskId, UserUtils.getUserName());
        return Json.succ("签收任务成功");
    }
    @GetMapping("/completeTask")
    public Json completeTask(@RequestParam String taskId){
        Auditor auditor = new Auditor();
        auditor.setAuditor(UserUtils.getUserName());
        auditor.setResult(UserUtils.getUserName()+"审批通过");
        auditor.setAuditTime(new Date());
        Map<String, Object> vars = new HashMap<>();
        //设置申请参数信息
        vars.put(UserUtils.getUserName(),auditor);
        taskService.complete(taskId, vars);

        return Json.succ("审批任务成功");
    }

    @GetMapping("/showFinishedTask")
    public Json showFinishedTask(@RequestParam String processDefineKey){
        List<HistoricProcessInstance> hisProInstanceList = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefineKey).involvedUser(UserUtils.getUserName()).finished()
                .orderByProcessInstanceEndTime().desc().list();

        for (HistoricProcessInstance hisProInstance : hisProInstanceList) {
            List<HistoricTaskInstance> hisTaskInstanceList = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(hisProInstance.getId()).processFinished()
                    .taskAssignee(UserUtils.getUserName())
                    .orderByHistoricTaskInstanceEndTime().desc().list();

            List<HistoricVariableInstance> varInstanceList = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(hisProInstance.getId())
                    .variableName(UserUtils.getUserName()).list();

            System.out.println("test");

        }

        return Json.succ("审批任务成功");
    }


//    @GetMapping(value = "/getFlowImg/{processInstanceId}")
//    public void getFlowImgByInstantId(@PathVariable("processInstanceId") String processInstanceId, HttpServletResponse response) {
//        try {
//            System.out.println("processInstanceId:" + processInstanceId);
//            getFlowImgByInstanceId(processInstanceId, response.getOutputStream());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 根据流程实例Id,获取实时流程图片
//     *
//     * @param processInstanceId
//     * @param outputStream
//     * @return
//     */
//    public void getFlowImgByInstanceId(String processInstanceId, OutputStream outputStream) {
//        try {
//            if (StringUtils.isEmpty(processInstanceId)) {
//                log.error("processInstanceId is null");
//                return;
//            }
//            // 获取历史流程实例
//            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
//            // 获取流程中已经执行的节点，按照执行先后顺序排序
//            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
//                    .orderByHistoricActivityInstanceId().asc().list();
//            // 高亮已经执行流程节点ID集合
//            List<String> highLightedActivitiIds = new ArrayList<>();
//            for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
//                highLightedActivitiIds.add(historicActivityInstance.getActivityId());
//            }
//
//            List<HistoricProcessInstance> historicFinishedProcessInstances = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).finished()
//                    .list();
//            ProcessDiagramGenerator processDiagramGenerator = null;
//            // 如果还没完成，流程图高亮颜色为绿色，如果已经完成为红色
//            if (!CollectionUtils.isEmpty(historicFinishedProcessInstances)) {
//                // 如果不为空，说明已经完成
//                processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
//            } else {
//                processDiagramGenerator = new CustomProcessDiagramGenerator();
//            }
//
//            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
//            // 高亮流程已发生流转的线id集合
//            List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstances);
//
//            // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
//            InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitiIds, highLightedFlowIds, "宋体", "微软雅黑", "黑体", null, 2.0);
//
//            // 输出图片内容
//            byte[] b = new byte[1024];
//            int len;
//            while ((len = imageStream.read(b, 0, 1024)) != -1) {
//                outputStream.write(b, 0, len);
//            }
//        } catch (Exception e) {
//            log.error("processInstanceId" + processInstanceId + "生成流程图失败，原因：" + e.getMessage(), e);
//        }
//
//    }
//
//    /**
//     * 获取已经流转的线
//     *
//     * @param bpmnModel
//     * @param historicActivityInstances
//     * @return
//     */
//    private List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
//        // 高亮流程已发生流转的线id集合
//        List<String> highLightedFlowIds = new ArrayList<>();
//        // 全部活动节点
//        List<FlowNode> historicActivityNodes = new ArrayList<>();
//        // 已完成的历史活动节点
//        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();
//
//        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
//            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
//            historicActivityNodes.add(flowNode);
//            if (historicActivityInstance.getEndTime() != null) {
//                finishedActivityInstances.add(historicActivityInstance);
//            }
//        }
//
//        FlowNode currentFlowNode = null;
//        FlowNode targetFlowNode = null;
//        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
//        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
//            // 获得当前活动对应的节点信息及outgoingFlows信息
//            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance.getActivityId(), true);
//            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();
//
//            /**
//             * 遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转： 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
//             */
//            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
//                // 遍历历史活动节点，找到匹配流程目标节点的
//                for (SequenceFlow sequenceFlow : sequenceFlows) {
//                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(), true);
//                    if (historicActivityNodes.contains(targetFlowNode)) {
//                        highLightedFlowIds.add(targetFlowNode.getId());
//                    }
//                }
//            } else {
//                List<Map<String, Object>> tempMapList = new ArrayList<>();
//                for (SequenceFlow sequenceFlow : sequenceFlows) {
//                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
//                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
//                            Map<String, Object> map = new HashMap<>();
//                            map.put("highLightedFlowId", sequenceFlow.getId());
//                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
//                            tempMapList.add(map);
//                        }
//                    }
//                }
//
//                if (!CollectionUtils.isEmpty(tempMapList)) {
//                    // 遍历匹配的集合，取得开始时间最早的一个
//                    long earliestStamp = 0L;
//                    String highLightedFlowId = null;
//                    for (Map<String, Object> map : tempMapList) {
//                        long highLightedFlowStartTime = Long.valueOf(map.get("highLightedFlowStartTime").toString());
//                        if (earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime) {
//                            highLightedFlowId = map.get("highLightedFlowId").toString();
//                            earliestStamp = highLightedFlowStartTime;
//                        }
//                    }
//
//                    highLightedFlowIds.add(highLightedFlowId);
//                }
//
//            }
//
//        }
//        return highLightedFlowIds;
//    }










//    /**
//     * @Author 葛明
//     * @Note 读取流程资源
//     * @Date 2017-1-3 15:11
//     * @param processDefinitionId 流程定义ID
//     * @param resourceName        资源名称
//     */
//    @GetMapping("/read-resource")
//    public void readResource(String processDefinitionId, String resourceName,String pProcessInstanceId, HttpServletResponse response)
//            throws Exception {
//        // 设置页面不缓存
//        response.setHeader("Pragma", "No-cache");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setDateHeader("Expires", 0);
//
//        ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
//        ProcessDefinition pd = pdq.processDefinitionId(processDefinitionId).singleResult();
//
//        if(resourceName.endsWith(".png") && StringUtils.isEmpty(pProcessInstanceId) == false)
//        {
//            getActivitiProccessImage(pProcessInstanceId,response);
//            //ProcessDiagramGenerator.generateDiagram(pde, "png", getRuntimeService().getActiveActivityIds(processInstanceId));
//        }
//        else
//        {
//            // 通过接口读取
//            InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), resourceName);
//
//            // 输出资源内容到相应对象
//            byte[] b = new byte[1024];
//            int len = -1;
//            while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
//                response.getOutputStream().write(b, 0, len);
//            }
//        }
//    }
//
//
//    /**
//     * 获取流程图像，已执行节点和流程线高亮显示
//     */
//    public void getActivitiProccessImage(String pProcessInstanceId, HttpServletResponse response)
//    {
//        //log.info("[开始]-获取流程图图像");
//        try {
//            //  获取历史流程实例
//            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
//                    .processInstanceId(pProcessInstanceId).singleResult();
//
//            if (historicProcessInstance == null) {
//                //throw new BusinessException("获取流程实例ID[" + pProcessInstanceId + "]对应的历史流程实例失败！");
//            }
//            else
//            {
//                // 获取流程定义
//                ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
//                        .getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());
//
//                // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
//                List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
//                        .processInstanceId(pProcessInstanceId).orderByHistoricActivityInstanceId().asc().list();
//
//                // 已执行的节点ID集合
//                List<String> executedActivityIdList = new ArrayList<String>();
//                int index = 1;
//                //log.info("获取已经执行的节点ID");
//                for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
//                    executedActivityIdList.add(activityInstance.getActivityId());
//
//                    //log.info("第[" + index + "]个已执行节点=" + activityInstance.getActivityId() + " : " +activityInstance.getActivityName());
//                    index++;
//                }
//
//                BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
//
//                // 已执行的线集合
//                List<String> flowIds = new ArrayList<String>();
//                // 获取流程走过的线 (getHighLightedFlows是下面的方法)
//                flowIds = getHighLightedFlows(bpmnModel,processDefinition, historicActivityInstanceList);
//
//
//
//                // 获取流程图图像字符流
//                ProcessDiagramGenerator pec = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
//                //配置字体
//                InputStream imageStream = pec.generateDiagram(bpmnModel, "png", executedActivityIdList, flowIds,"宋体","微软雅黑","黑体",null,2.0);
//
//                response.setContentType("image/png");
//                OutputStream os = response.getOutputStream();
//                int bytesRead = 0;
//                byte[] buffer = new byte[8192];
//                while ((bytesRead = imageStream.read(buffer, 0, 8192)) != -1) {
//                    os.write(buffer, 0, bytesRead);
//                }
//                os.close();
//                imageStream.close();
//            }
//            //log.info("[完成]-获取流程图图像");
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            //log.error("【异常】-获取流程图失败！" + e.getMessage());
//            //throw new BusinessException("获取流程图失败！" + e.getMessage());
//        }
//    }
//
//    public List<String> getHighLightedFlows(BpmnModel bpmnModel,ProcessDefinitionEntity processDefinitionEntity,List<HistoricActivityInstance> historicActivityInstances) {
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //24小时制
//        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
//
//        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
//            // 对历史流程节点进行遍历
//            // 得到节点定义的详细信息
//            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());
//
//
//            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();// 用以保存后续开始时间相同的节点
//            FlowNode sameActivityImpl1 = null;
//
//            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);// 第一个节点
//            HistoricActivityInstance activityImp2_;
//
//            for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
//                activityImp2_ = historicActivityInstances.get(k);// 后续第1个节点
//
//                if (activityImpl_.getActivityType().equals("userTask") && activityImp2_.getActivityType().equals("userTask") &&
//                        df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_.getStartTime()))) //都是usertask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
//                {
//
//                } else {
//                    sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());//找到紧跟在后面的一个节点
//                    break;
//                }
//
//            }
//            sameStartTimeNodes.add(sameActivityImpl1); // 将后面第一个节点放在时间相同节点的集合里
//            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
//                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
//                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
//
//                if (df.format(activityImpl1.getStartTime()).equals(df.format(activityImpl2.getStartTime()))) {// 如果第一个节点和第二个节点开始时间相同保存
//                    FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
//                    sameStartTimeNodes.add(sameActivityImpl2);
//                } else {// 有不相同跳出循环
//                    break;
//                }
//            }
//            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows(); // 取出节点的所有出去的线
//
//            for (SequenceFlow pvmTransition : pvmTransitions) {// 对所有的线进行遍历
//                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
//                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
//                    highFlows.add(pvmTransition.getId());
//                }
//            }
//
//        }
//        return highFlows;
//    }
}
