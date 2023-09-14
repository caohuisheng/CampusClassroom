package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 课程发布惹怒处理类
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private SearchServiceClient searchServiceClient;

    //课程发布任务入口
    @XxlJob("CoursePublishJobHandler")
    public void CoursePublishJobHandler(){
        //获取分片信息
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //开始执行任务
        this.process(shardIndex,shardTotal,"course_publish",30,60);
    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        //得到课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //1.课程静态化
        generateCourseHtml(mqMessage,courseId);

        //2.保存课程索引
        saveCourseIndex(mqMessage,courseId);

        //3.课程缓存


        return true;
    }

    /**
     * 生成课程详情静态页面
     */
    private void generateCourseHtml(MqMessage mqMessage,Long courseId){
        //得到消息任务id，即课程id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne > 0){
            log.debug("课程静态页面已生成,直接返回：{}",taskId);
            return ;
        }
        //开始进行课程静态化
        File htmlfile = coursePublishService.generateCourseHtml(courseId);

        if(htmlfile != null){
            coursePublishService.uploadCourseHtml(courseId,htmlfile);
        }

        //保存第一阶段状态
        mqMessageService.completedStageOne(taskId);
    }

    /**
     * 保存课程索引到es
     */
    private void saveCourseIndex(MqMessage mqMessage,Long courseId){
        //得到消息任务id，即课程id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageTwo(taskId);
        if(stageOne > 0){
            log.debug("课程索引已添加,直接返回：{}",taskId);
            return ;
        }

        //查询课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);

        //添加索引
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XuechengPlusException.cast("添加索引失败");
            return ;
        }

        //保存第二阶段状态
        mqMessageService.completedStageTwo(taskId);
    }

}
