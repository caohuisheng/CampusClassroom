package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
//import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程信息编辑接口
 */
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
@Slf4j
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation(value = "课程分页查询接口")
    @PostMapping("/course/list")
//    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')") //设置权限，拥有该权限才可以访问
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        //得到companyId
//        SecurityUtil.XcUser user = SecurityUtil.getUser();
//        String companyId = user.getCompanyId();
        long companyId = 1232141425L;
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseInfo(companyId,pageParams,queryCourseParamsDto);
        return pageResult;
    }

    @ApiOperation(value = "新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDto saveCourseBaseInfo(@RequestBody @Validated({ValidationGroups.Insert.class}) AddCourseDto courseDto){
        long companyId = 1232141425L;
        return courseBaseInfoService.saveCourseBaseInfo(companyId,courseDto);
    }

    @ApiOperation(value = "根据id查询课程信息接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto queryCourseBase(@PathVariable Long courseId){
        //获取当前用户身份
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

//        SecurityUtil.XcUser user = SecurityUtil.getUser();
//        System.out.println(user);

        return courseBaseInfoService.getCourseBaseInfoDto(courseId);
    }

    @ApiOperation(value = "更新课程信息接口")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourseBase(@RequestBody UpdateCourseDto dto){
        long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,dto);
    }

    @ApiOperation(value = "删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourseBaseInfo(@PathVariable Long courseId){
        courseBaseInfoService.deleteCourseBaseInfo(courseId);
    }

}
