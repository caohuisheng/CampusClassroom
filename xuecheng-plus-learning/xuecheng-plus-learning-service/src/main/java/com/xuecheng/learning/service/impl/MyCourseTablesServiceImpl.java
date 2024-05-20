package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper chooseCourseMapper;

    @Autowired
    XcCourseTablesMapper courseTablesMapper;


    @Autowired
    private ContentServiceClient contentServiceClient;

    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //1.查询发布的课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(courseId);

        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        //2.根据收费规则添加记录
        String charge = coursePublish.getCharge();
        if(charge.equals("201000")){//免费
            //添加免费课程
            xcChooseCourse = addFreeCourse(userId,coursePublish);
            //添加到课程表
            addCourseTables(xcChooseCourse);
        }else{
            //添加收费课程
            xcChooseCourse = addChargeCourse(userId, coursePublish);
        }
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);

        //3.获取学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }

    public XcChooseCourse addFreeCourse(String userId,CoursePublish coursePublish){
        //查询选课表中是否存在该用户的对应课程选课记录
        LambdaQueryWrapper<XcChooseCourse> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcChooseCourse::getCourseId,coursePublish.getId())
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getOrderType,"700001") //免费课程
                .eq(XcChooseCourse::getStatus,"701001"); //选课成功
        XcChooseCourse xcChooseCourse = chooseCourseMapper.selectOne(lqw);

        if(xcChooseCourse != null){
            return xcChooseCourse;
        }

        xcChooseCourse = new XcChooseCourse();
        //添加选课记录信息
        xcChooseCourse.setCourseId(coursePublish.getId());
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidDays(coursePublish.getValidDays());//课程有限时间
        xcChooseCourse.setStatus("701001");//选课成功
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        chooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;
    }

    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse){
        //1.判断选课是否成功
        if(!xcChooseCourse.getStatus().equals("701001")){
            XuechengPlusException.cast("选课未成功，无法添加到课程表");
        }

        //2.查询我的课程表
        String userId = xcChooseCourse.getUserId();
        Long courseId = xcChooseCourse.getCourseId();
        XcCourseTables xcCourseTables = getXcCourseTables(userId,courseId);
        if(xcCourseTables != null){
            return xcCourseTables;
        }

        //3.添加记录到课程表
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,xcCourseTablesNew);
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTablesNew.setUpdateDate(LocalDateTime.now());
        courseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;
    }

    /**
     * 根据用户id和课程id查询课程表的课程
     * @param userId
     * @param courseId
     * @return
     */
    private XcCourseTables getXcCourseTables(String userId,Long courseId) {
        LambdaQueryWrapper<XcCourseTables> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcCourseTables::getUserId,userId).eq(XcCourseTables::getCourseId,courseId);
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(lqw);
        return xcCourseTables;
    }


    public XcChooseCourse addChargeCourse(String userId,CoursePublish coursePublish){
        //1.查询选课表中是否存在该用户的对应课程选课记录
        LambdaQueryWrapper<XcChooseCourse> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcChooseCourse::getCourseId,coursePublish.getId())
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getOrderType,"700002") //收费订单
                .eq(XcChooseCourse::getStatus,"701002"); //待支付
        XcChooseCourse xcChooseCourse = chooseCourseMapper.selectOne(lqw);
        //如果存在选课记录，直接返回
        if(xcChooseCourse != null){
            return xcChooseCourse;
        }

        xcChooseCourse = new XcChooseCourse();
        //2.添加选课记录信息
        xcChooseCourse.setCourseId(coursePublish.getId());
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");////收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setValidDays(coursePublish.getValidDays());//有效时间
        xcChooseCourse.setStatus("701002");//待支付
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        chooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;
    }


    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        XcCourseTablesDto xcCourseTablesDto = null;
        if(xcCourseTables == null){
            //1.没有选课或选课后未支付
            xcCourseTablesDto = new XcCourseTablesDto();
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }

        //2.课程是否过期
        boolean isExpired = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        xcCourseTablesDto = new XcCourseTablesDto();
        if(!isExpired){
            BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702001");
        }else{
            xcCourseTablesDto.setLearnStatus("702003");
        }

        return xcCourseTablesDto;
    }

    @Override
    public PageResult<XcCourseTables> getMyCoursetables(MyCourseTableParams params) {
        //构造分页参数
        int pageNo = params.getPageNo();
        int pageSize = params.getPageSize();
        Page<XcCourseTables> page = new Page<>(pageNo,pageSize);

        LambdaQueryWrapper<XcCourseTables> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcCourseTables::getUserId,params.getUserId());

        //分页查询
        Page<XcCourseTables> pageResult = courseTablesMapper.selectPage(page, lqw);
        List<XcCourseTables> records = pageResult.getRecords();

        //创建分页对象
        PageResult<XcCourseTables> coursePageResult = new PageResult<>(records,records.size(),pageNo,pageSize);

        return coursePageResult;
    }

    @Override
    public boolean saveChooseCourseStatus(String choosecourseId) {
        //查询选课记录
        XcChooseCourse xcChooseCourse = chooseCourseMapper.selectById(choosecourseId);
        //创建课表实例并添加到数据库
        XcCourseTables xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,xcCourseTables);
        xcCourseTables.setId(null);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        courseTablesMapper.insert(xcCourseTables);
        return true;
    }
}
