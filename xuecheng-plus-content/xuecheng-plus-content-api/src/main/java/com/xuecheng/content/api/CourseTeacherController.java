package com.xuecheng.content.api;

import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("根据课程id查询教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> selectCourseTeacher(@PathVariable Long courseId){
        return courseTeacherService.selectCourseTeacher(courseId);
    }

    @ApiOperation("新增课程教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.saveCourseTeacher(courseTeacher);
    }

    @ApiOperation("修改课程教师")
    @PutMapping("/courseTeacher")
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.updateCourseTeacher(courseTeacher);
    }

    @ApiOperation("删除课程教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId,@PathVariable Long courseTeacherId){
        courseTeacherService.deleteCourseTeacher(courseId,courseTeacherId);
    }
}
