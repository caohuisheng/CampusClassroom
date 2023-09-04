package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String rootId) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(rootId);
        courseCategoryTreeDtos = courseCategoryTreeDtos.stream().filter(item->!rootId.equals(item.getId())).collect(Collectors.toList());
        //将分类列表转换为map，便于根据id查询
        Map<String,CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .collect(Collectors.toMap(item->item.getId(),item->item));

        //最终结果
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        //遍历每一个分类节点
        courseCategoryTreeDtos.stream().forEach(item->{
            //判断是不是一级节点
            if(item.getParentid().equals(rootId)){
                categoryTreeDtos.add(item);
            }else{
                //得到该节点的父节点
                CourseCategoryTreeDto parentTreeDto = mapTemp.get(item.getParentid());
                if(parentTreeDto.getChildrenTreeNodes() == null){
                    parentTreeDto.setChildrenTreeNodes(new ArrayList<>());
                }
                parentTreeDto.getChildrenTreeNodes().add(item);
            }
        });

        return categoryTreeDtos;
    }


}
