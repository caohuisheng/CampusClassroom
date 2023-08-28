package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation(value = "课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseInfo(pageParams,queryCourseParamsDto);
        //System.out.println(pageResult);
        log.error("分页查询结果",pageResult.getItems(),"aa");
        return pageResult;
    }

    @ApiOperation(value = "新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDto saveCourseBaseInfo(@RequestBody AddCourseDto courseDto){
        long companyId = 1232141425L;
        return courseBaseInfoService.saveCourseBaseInfo(companyId,courseDto);
    }

}
