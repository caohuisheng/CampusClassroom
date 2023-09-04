package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * 课程分类服务
 */
public interface CourseCategoryService {
    /**
     * 课程分类树形结构查询
     * @return
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String rootId);
}
