package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

/**
 * 课程计划服务
 */
public interface TeachplanService {
    /**
     * 查询课程计划树形结构
     * @param courseId
     * @return
     */
    List<TeachPlanDto> selectTeachplanTreeNodes(Long courseId);

    /**
     * 新增或修改课程计划
     */
    void saveTeachPlan(SaveTeachplanDto teachplanDto);

    /**
     * 删除课程计划和关联的媒资
     * @param teachplanId 课程id
     */
    void deleteTeachplan(Long teachplanId);

    /**
     * 上移课程计划
     * @param teachplanId 课程计划id
     */
    void moveupTeachplan(Long teachplanId);

    /**
     * 下移课程计划
     * @param teachplanId 课程计划id
     */
    void movedownTeachplan(Long teachplanId);

    /**
     * 删除课程的所有课程计划和对应的媒资信息
     * @param courseId 课程id
     */
    void deleteAllTeachplan(Long courseId);
}
