package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程信息管理业务接口实现类
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseTeacherService courseTeacherService;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseInfo(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //测试查询接口
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //拼接查询条件
        //根据课程名称模糊查询  name like '%名称%'
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        //教学机构细粒度授权
        queryWrapper.eq(CourseBase::getCompanyId,companyId);

        //分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //分页查询E page 分页参数, @Param("ew") Wrapper<T> queryWrapper 查询条件
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        //数据
        List<CourseBase> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();

        //准备返回数据 List<T> items, long counts, long page, long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Override
    public CourseBaseInfoDto saveCourseBaseInfo(long companyId,AddCourseDto courseDto) {
//        int i = 1/0;
        //合法性校验
//        if (StringUtils.isBlank(courseDto.getName())) {
//            XuechengPlusException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(courseDto.getMt())) {
//            XuechengPlusException.cast("课程大分类为空");
//        }
//
//        if (StringUtils.isBlank(courseDto.getSt())) {
//            XuechengPlusException.cast("课程小分类为空");
//        }
//
//        if (StringUtils.isBlank(courseDto.getGrade())) {
//            XuechengPlusException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(courseDto.getTeachmode())) {
//            XuechengPlusException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(courseDto.getUsers())) {
//            XuechengPlusException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(courseDto.getCharge())) {
//            XuechengPlusException.cast("收费规则为空");
//        }

        //创建CourseBase类
        CourseBase newCourseBase = new CourseBase();
        BeanUtils.copyProperties(courseDto,newCourseBase);
        newCourseBase.setCompanyId(companyId);
        newCourseBase.setAuditStatus("202002");
        newCourseBase.setStatus("203001");
        newCourseBase.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        int flag = courseBaseMapper.insert(newCourseBase);
        if(flag<=0){
            throw new RuntimeException("课程添加失败");
        }

        //获取课程市场信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(courseDto,courseMarket);
        Long courseId = newCourseBase.getId();
        courseMarket.setId(courseId);
        saveCourseMarket(courseMarket);

        return getCourseBaseInfoDto(courseId);
    }

    /**
     * 保存课程营销信息
     * @param courseMarket
     */
    private void saveCourseMarket(CourseMarket courseMarket){
        //收费规则
        String charge = courseMarket.getCharge();
//        if(StringUtils.isBlank(charge)){
//            XuechengPlusException.cast("收费规则没有选择");
//        }
        if(charge.equals("201001")){
            Float price = courseMarket.getPrice();
            if(price == null || price <= 0){
                XuechengPlusException.cast("价格不能为负值");
            }
        }

        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());
        int flag;
        //判断表中是否存在课程的市场信息
        if(courseMarket1 == null){
            flag = courseMarketMapper.insert(courseMarket);
        }else{
            flag = courseMarketMapper.updateById(courseMarket);
        }

        if(flag<=0){
            throw new RuntimeException("课程营销信息添加失败");
        }
    }

    /**
     * 获取课程详细信息
     * @param courseId
     * @return
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfoDto(long courseId){
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        //组合基本信息和营销信息
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //根据分类id查询课程的分类名称并设置
        CourseCategory stCourseCategory = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(stCourseCategory.getName());
        CourseCategory mtCourseCategory = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(mtCourseCategory.getName());

        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, UpdateCourseDto updateCourseDto) {
        Long courseId = updateCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //判断是否为本机构
        if(!companyId.equals(courseBase.getCompanyId())){
            XuechengPlusException.cast("只能修改本机构的课程");
        }

        //更新课程基本信息
        BeanUtils.copyProperties(updateCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);

        //更新课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(updateCourseDto,courseMarket);
        saveCourseMarket(courseMarket);

        return this.getCourseBaseInfoDto(courseId);
    }

    @Override
    public void deleteCourseBaseInfo(Long courseId) {
        //删除课程基本信息
        courseBaseMapper.deleteById(courseId);
        //删除课程营销信息
        courseMarketMapper.deleteById(courseId);
        //删除课程计划信息和媒资信息
        teachplanService.deleteAllTeachplan(courseId);
        //删除课程的所有教师
        courseTeacherService.deleteAllCourseTeacher(courseId);
    }

}
