package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseTeacherService courseTeacherService;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //查询课程信息,营销信息
        final CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfoDto(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);

        //查询课程计划
        final List<TeachPlanDto> teachPlanDtos = teachplanService.selectTeachplanTreeNodes(courseId);
        coursePreviewDto.setTeachplans(teachPlanDtos);

        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //1.查看课程审核状态
        final CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase.getAuditStatus().equals("202003")){
            XuechengPlusException.cast("当前课程审核中");
        }

        //2.检验基本信息
        if(!courseBase.getCompanyId().equals(companyId)){
            XuechengPlusException.cast("只能修改本机构的课程");
        }
        if(StringUtils.isEmpty(courseBase.getPic())){
            XuechengPlusException.cast("提交失败，请上传课程图片");
        }

        //3.添加到预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //设置基本信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfoDto(courseId);
        BeanUtils.copyProperties(courseBaseInfoDto,coursePublishPre);

        //设置营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String marketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(marketJson);

        //设置课程计划信息
        List<TeachPlanDto> teachPlanDtos = teachplanService.selectTeachplanTreeNodes(courseId);
        if(CollectionUtils.isEmpty(teachPlanDtos)){
            XuechengPlusException.cast("提交失败，请上传课程计划");
        }
        String teachplanJson = JSON.toJSONString(teachPlanDtos);
        coursePublishPre.setTeachplan(teachplanJson);

        //设置师资信息
        List<CourseTeacher> courseTeachers = courseTeacherService.selectCourseTeacher(courseId);
        if(CollectionUtils.isEmpty(courseTeachers)){
            XuechengPlusException.cast("提交失败，请上传师资信息");
        }
        String teachersJson = JSON.toJSONString(courseTeachers);
        coursePublishPre.setTeachers(teachersJson);

        coursePublishPre.setCreateDate(LocalDateTime.now());
        coursePublishPre.setAuditDate(LocalDateTime.now());
        coursePublishPre.setStatus("202003");
        CoursePublishPre coursePublishPre_origin = coursePublishPreMapper.selectById(courseId);

        //判断该课程预发布信息是否存在
        if(coursePublishPre_origin == null){
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    public void publish(Long companyId, Long courseId) {
        //约束校验
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XuechengPlusException.cast("请先提交审核，审核通过才可以发布");
        }
        if(!companyId.equals(coursePublishPre.getCompanyId())){
            XuechengPlusException.cast("只可以发布本机构的课程");
        }
        if(!coursePublishPre.getStatus().equals("202004")){
            XuechengPlusException.cast("操作失败，审核通过方可提交");
        }

        //保存课程发布信息
        saveCoursePublish(coursePublishPre);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除该课程预发布记录
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * 保存消息表记录
     * @param courseId
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage == null){
            XuechengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    /**
     * 课程发布信息
     * @param coursePublishPre
     */
    public void saveCoursePublish(CoursePublishPre coursePublishPre){
        Long courseId = coursePublishPre.getId();
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);

        //判断是否存在该课程的发布信息
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }

        //设置课程发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);

            //创建临时文件
            File htmlFile = File.createTempFile("temp",".html");
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);

            return htmlFile;
        } catch (Exception e) {
           log.error("生成静态页面失败：{}，课程id：{}",e.getMessage(),courseId);
           XuechengPlusException.cast("生成静态页面失败");
        }
        return null;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String upload = mediaServiceClient.uploadFile(multipartFile, "course/"+courseId+".html");
        if(upload == null){
            XuechengPlusException.cast("上传静态文件异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        Object obj = redisTemplate.opsForValue().get("course_" + courseId);
        //判断是否存在缓存
        if(obj != null){
            String jsonStr = obj.toString();
            CoursePublish coursePublish = JSON.parseObject(jsonStr, CoursePublish.class);
            return coursePublish;
        }

        System.out.println("从数据库查询");
        CoursePublish coursePublish = getCoursePublish(courseId);
        if(coursePublish != null){
            redisTemplate.opsForValue().set("course_" + courseId,coursePublish);
        }

        return coursePublish;
    }

    public CoursePublish getCoursePublishCache1(Long courseId) {
        Object obj = redisTemplate.opsForValue().get("course_" + courseId);
        //判断是否存在缓存
        if(obj != null){
            String jsonStr = obj.toString();
            CoursePublish coursePublish = JSON.parseObject(jsonStr, CoursePublish.class);
            return coursePublish;
        }

        System.out.println("从数据库查询");
        synchronized(this){
            CoursePublish coursePublish = getCoursePublish(courseId);
            //不论是否为null都保存到redis
            redisTemplate.opsForValue().set("course_" + courseId,coursePublish);
            return coursePublish;
        }
    }


}
