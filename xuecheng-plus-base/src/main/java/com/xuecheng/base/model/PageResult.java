package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果模型类
 * @param <T>
 */
@Data
@ToString
public class PageResult<T> implements Serializable {
    //分页查询结果
    private List<T> items;
    //当前页码
    private long pageNo;
    //每页记录数
    private long pageSize;
    //记录总数
    private long counts;

    public PageResult(List<T> result, long counts,long pageNo, long pageSize) {
        this.items = result;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.counts = counts;
    }
}
