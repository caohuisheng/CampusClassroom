package com.xuecheng.content.model.dto;

/**
 * 更新课程信息表单dto
 */
public class UpdateCourseDto extends CourseBaseInfoDto{
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
