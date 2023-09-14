package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 课程发布服务
 */
public interface CoursePublishService {
    /**
     * 课程预览
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 课程审核
     * @param companyId
     * @param courseId
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    public void publish(Long companyId,Long courseId);

    /**
     * 课程静态化
     * @param courseId
     * @return
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     * @param courseId
     * @param file
     */
    public void uploadCourseHtml(Long courseId,File file);

    /**
     * 查询课程发布信息
     * @return
     */
    public CoursePublish getCoursePublish(Long courseId);

    /**
     * @description 查询缓存中的课程信息
     * @param courseId
     * @return com.xuecheng.content.model.po.CoursePublish
     * @author Mr.M
     * @date 2022/10/22 16:15
     */
    public CoursePublish getCoursePublishCache(Long courseId);
}
