package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

/**
 * 课程计划树形结构dto
 */
public class TeachPlanDto extends Teachplan {
    //媒资信息
    private TeachplanMedia teachplanMedia;

    //课程计划子节点
    private List<TeachPlanDto> teachPlanTreeNodes;

    public TeachplanMedia getTeachplanMedia() {
        return teachplanMedia;
    }

    public void setTeachplanMedia(TeachplanMedia teachplanMedia) {
        this.teachplanMedia = teachplanMedia;
    }

    public List<TeachPlanDto> getTeachPlanTreeNodes() {
        return teachPlanTreeNodes;
    }

    public void setTeachPlanTreeNodes(List<TeachPlanDto> teachPlanTreeNodes) {
        this.teachPlanTreeNodes = teachPlanTreeNodes;
    }
}
