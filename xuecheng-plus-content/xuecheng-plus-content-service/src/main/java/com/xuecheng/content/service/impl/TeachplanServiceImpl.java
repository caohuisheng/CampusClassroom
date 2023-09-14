package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;


    @Override
    public List<TeachPlanDto> selectTeachplanTreeNodes(Long courseId) {
        return teachplanMapper.selectTeachPlanTreeNodes(courseId);
    }

    @Override
    public void saveTeachPlan(SaveTeachplanDto teachplanDto) {
        Long teachplanId = teachplanDto.getId();
        if(teachplanId == null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            int count = getTeachplanCount(teachplanDto);
            teachplan.setOrderby(count+1);
            teachplanMapper.insert(teachplan);
        }else{
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    private int getTeachplanCount(SaveTeachplanDto teachplanDto){
        Long parentid = teachplanDto.getParentid();
        Long courseId = teachplanDto.getCourseId();
        LambdaQueryWrapper<Teachplan> qw = new LambdaQueryWrapper<>();
        qw.eq(Teachplan::getParentid,parentid).eq(Teachplan::getCourseId,courseId);
        return teachplanMapper.selectCount(qw);
    }

    @Override
    public void deleteTeachplan(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        if(teachplan.getGrade().equals(1)){
            //为一级节点，当无子节点时才可以删
            LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Teachplan::getParentid,teachplanId);
            Integer childCount = teachplanMapper.selectCount(lqw);
            if(childCount > 0){
                XuechengPlusException.cast("当前大章节下有小章节，不可删除");
            }
            teachplanMapper.deleteById(teachplanId);
        }else{
            //为二级节点，直接删除
            teachplanMapper.deleteById(teachplanId);
            //删除对应媒资信息
            LambdaQueryWrapper<TeachplanMedia> lqw = new LambdaQueryWrapper<>();
            lqw.eq(TeachplanMedia::getTeachplanId,teachplanId);
            teachplanMediaMapper.delete(lqw);
        }
    }

    void swapOrder(Teachplan plan1,Teachplan plan2){
        int temp = plan1.getOrderby();
        plan1.setOrderby(plan2.getOrderby());
        plan2.setOrderby(temp);
    }

    @Override
    public void moveupTeachplan(Long teachplanId) {
        //获取当前课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer orderby = teachplan.getOrderby();
        //找到该课程计划上面的一条课程计划
        LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Teachplan::getParentid,teachplan.getParentid())
                .lt(Teachplan::getOrderby,orderby)
                .orderByDesc(Teachplan::getOrderby);
        List<Teachplan> teachplans = teachplanMapper.selectList(lqw);
        if(teachplans.size()==0){
            XuechengPlusException.cast("当前课程计划已经到达最上面");
        }
        Teachplan teachplanUp = teachplans.get(0);
        //交换排序字段
        swapOrder(teachplan,teachplanUp);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplanUp);
    }

    @Override
    public void movedownTeachplan(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer orderby = teachplan.getOrderby();
        LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Teachplan::getParentid,teachplan.getParentid())
                .gt(Teachplan::getOrderby,orderby)
                .orderByAsc(Teachplan::getOrderby);
        List<Teachplan> teachplans = teachplanMapper.selectList(lqw);
        if(teachplans.size()==0){
            XuechengPlusException.cast("当前课程计划已经到达最下面");
        }
        Teachplan teachplanDown = teachplans.get(0);
        swapOrder(teachplan,teachplanDown);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplanDown);
    }

    @Override
    public void deleteAllTeachplan(Long courseId) {
        //删除课程对应的所有课程计划
        LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Teachplan::getCourseId,courseId);
        teachplanMapper.delete(lqw);
        //删除课程对应的所有媒资
        LambdaQueryWrapper<TeachplanMedia> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(TeachplanMedia::getCourseId,courseId);
        teachplanMediaMapper.delete(lqw1);
    }

    @Override
    public void bindMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        final Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        final Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan == null){
            XuechengPlusException.cast("该课程计划不存在");
        }

        //1.删除该课程计划绑定的媒资
        LambdaQueryWrapper<TeachplanMedia> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TeachplanMedia::getTeachplanId,teachplanId);
        teachplanMediaMapper.delete(lqw);

        //2.绑定新的媒资
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
    }
}
