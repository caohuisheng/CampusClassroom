package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

public class CourseCategoryTreeDto extends CourseCategory {
    private List<CourseCategoryTreeDto> childrenTreeNodes;

    public List<CourseCategoryTreeDto> getChildrenTreeNodes(){
        return childrenTreeNodes;
    }
    public void setChildrenTreeNodes(List<CourseCategoryTreeDto> childs){
        this.childrenTreeNodes = childs;
    }
}
