package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 课程查询分页参数
 */
@Data
@ToString
public class PageParams {

    //当前页码
    @ApiModelProperty(value = "当前页码")
    private Long pageNo = 1L;

    //每页记录数默认值
    @ApiModelProperty(value = "每页记录数默认值")
    private Long pageSize =10L;

    public PageParams(){
    }

    public PageParams(long pageNo,long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
