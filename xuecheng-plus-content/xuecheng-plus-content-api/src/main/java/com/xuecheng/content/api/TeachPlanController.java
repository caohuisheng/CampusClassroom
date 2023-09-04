package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachPlanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation(value = "查询课程计划树形结构的接口")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> queryTeachPlanTreeNodes(@PathVariable Long courseId){
        return teachplanService.selectTeachplanTreeNodes(courseId);
    }

    @ApiOperation("新增或修改课程计划的接口")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplanDto){
        teachplanService.saveTeachPlan(teachplanDto);
    }

    @ApiOperation("删除课程计划和关联媒资的接口")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId){
        teachplanService.deleteTeachplan(teachplanId);
    }

    @ApiOperation("上移课程计划")
    @PostMapping("teachplan/moveup/{teachplanId}")
    public void moveupTeachplan(@PathVariable Long teachplanId){
        teachplanService.moveupTeachplan(teachplanId);
    }

    @ApiOperation("下移课程计划")
    @PostMapping("teachplan/movedown/{teachplanId}")
    public void movedownTeachplan(@PathVariable Long teachplanId){
        teachplanService.movedownTeachplan(teachplanId);
    }
}
