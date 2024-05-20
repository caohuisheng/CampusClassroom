package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
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
//@Component
@Slf4j
public class MyTask {

    @Autowired
    private CoursePublishService coursePublishService;


    //课程发布任务入口
    @XxlJob("MyTask")
    public void CoursePublishJobHandler(){
        //获取分片信息
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //开始执行任务
        log.debug("shardIndex:{}, shardTotal:{}",shardIndex,shardTotal);
    }
}
