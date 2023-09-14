package com.xuecheng.media.api;

import com.alibaba.nacos.common.http.param.MediaType;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);
    }

    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile multipartFile,
//                                      @RequestParam(value = "folder",required = false) String folder,
                                      @RequestParam(value = "objectName",required = false) String objectName)throws Exception{
        Long companyId = 1232141425L;
        //构建上传文件的参数
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileSize(multipartFile.getSize());
        //文件类型：图片
        uploadFileParamsDto.setFileType("001001");
        uploadFileParamsDto.setFilename(multipartFile.getOriginalFilename());

        //获取文件路径
        File tempFile = File.createTempFile("minio","temp");
        multipartFile.transferTo(tempFile);
        String absolutePath = tempFile.getAbsolutePath();

        //上传文件
        UploadFileResultDto uploadFileResultDto = mediaFileService.uploadMediaFile(companyId, uploadFileParamsDto, absolutePath,objectName);
        return uploadFileResultDto;
    }

}
