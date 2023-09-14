package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * 课程基本信息服务
 */
public interface CourseBaseInfoService {
    /**
     * 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询参数
     * @return 课程分页结果
     */
    PageResult<CourseBase> queryCourseBaseInfo(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程接口
     */
    CourseBaseInfoDto saveCourseBaseInfo(long companyId,AddCourseDto courseDto);

    /**
     * 根据id查询课程详细信息
     * @param courseId
     * @return
     */
    CourseBaseInfoDto getCourseBaseInfoDto(long courseId);

    /**
     * 更新课程信息
     * @param companyId
     * @param updateCourseDto
     * @return
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, UpdateCourseDto updateCourseDto);

    /**
     * 删除课程基本信息、营销信息、课程计划、课程教师信息
     * @param courseId
     */
    void deleteCourseBaseInfo(Long courseId);
}
