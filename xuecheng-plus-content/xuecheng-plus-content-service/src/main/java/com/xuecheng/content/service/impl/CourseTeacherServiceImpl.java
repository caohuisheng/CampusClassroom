package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> selectCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(lqw);
    }

    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        courseTeacher.setCreateDate(LocalDateTime.now());
        courseTeacherMapper.insert(courseTeacher);
        return courseTeacher;
    }

    @Override
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher) {
        //courseTeacherMapper.selectById();
        courseTeacherMapper.updateById(courseTeacher);
        return courseTeacher;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long courseTeacherId) {
        courseTeacherMapper.deleteById(courseTeacherId);
    }

    @Override
    public void deleteAllCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseTeacher::getCourseId,courseId);
        courseTeacherMapper.delete(lqw);
    }


}
