package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * 我的课程表服务接口
 */
public interface MyCourseTablesService {
    /**
     * 添加选课
     * @param userId
     * @param courseId
     * @return
     */
    public XcChooseCourseDto addChooseCourse(String userId,Long courseId);

    /**
     * 获取学习状态
     */
    public XcCourseTablesDto getLearningStatus(String userId,Long courseId);

    /**
     * @description 我的课程表
     * @param params
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     * @author Mr.M
     * @date 2022/10/27 9:24
     */
    public PageResult<XcCourseTables> getMyCoursetables(MyCourseTableParams params);

    boolean saveChooseCourseStatus(String choosecourseId);
}
