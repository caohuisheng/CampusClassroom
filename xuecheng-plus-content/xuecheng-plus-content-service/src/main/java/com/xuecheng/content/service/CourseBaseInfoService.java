package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

public interface CourseBaseInfoService {
    /**
     * 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询参数
     * @return 课程分页结果
     */
    PageResult<CourseBase> queryCourseBaseInfo(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程接口
     */
    CourseBaseInfoDto saveCourseBaseInfo(long companyId,AddCourseDto courseDto);
}
