package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * 课程教师服务
 */
public interface CourseTeacherService {
    /**
     * 根据课程id查询课程教师
     * @param courseId 课程id
     */
    List<CourseTeacher> selectCourseTeacher(Long courseId);

    /**
     * 新增课程教师
     * @param courseTeacher 教师信息
     */
    CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 修改课程教师
     * @param courseTeacher
     * @return
     */
    CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 删除课程教师
     * @param courseId
     * @param courseTeacherId
     */
    void deleteCourseTeacher(Long courseId,Long courseTeacherId);

    /**
     * 删除课程的所有教师
     */
    void deleteAllCourseTeacher(Long courseId);
}
