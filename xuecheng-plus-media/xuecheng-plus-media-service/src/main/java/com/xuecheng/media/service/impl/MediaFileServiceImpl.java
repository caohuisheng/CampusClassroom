package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    //注入自身的代理对象
    @Autowired
    private MediaFileService currentProxy;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;


    @Value("${minio.bucket.files}")
    private String bucket_files;
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Override
    public MediaFiles getMediaFilesById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> lqw = new LambdaQueryWrapper<>();
        lqw.like(!StringUtils.isEmpty(queryMediaParamsDto.getFilename()),MediaFiles::getFilename,queryMediaParamsDto.getFilename());
        lqw.eq(!StringUtils.isEmpty(queryMediaParamsDto.getAuditStatus()),MediaFiles::getAuditStatus,queryMediaParamsDto.getAuditStatus());
        lqw.eq(!StringUtils.isEmpty(queryMediaParamsDto.getFileType()),MediaFiles::getFileType,queryMediaParamsDto.getFileType());

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, lqw);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }

    private String getMimeType(String extension){
        if(extension == null){
            extension = "";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    private String getDefaultFolderPath(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String folder = sdf.format(new Date())+"/";
        return folder;
    }

    @Override
    public boolean addMediaFilesToMinio(String bucket,String objectName,String localFilePath,String mimeType){
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//添加子目录
                    .filename(localFilePath)
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            log.info("上传文件到minio成功，buacket:{},objectName:{}",bucket,objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio失败，buacket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
            //设置其它的信息
            mediaFiles.setId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            int flag = mediaFilesMapper.insert(mediaFiles);
            //异常
            //int i = 1/0;
            if(flag<=0){
                XuechengPlusException.cast("文件保存数据库失败");
                log.error("保存文件到数据库失败:{}",mediaFiles.toString());
            }
            //记录
            log.debug("保存文件到数据库成功:{}",mediaFiles.toString());

            //添加到待处理任务表
            addWaitingTask(mediaFiles);
        }

        return mediaFiles;
    }

    private void addWaitingTask(MediaFiles mediaFiles){
        //获取文件的mimetype
        String filename = mediaFiles.getFilename();
        String ext = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(ext);
        //如果文件得mimetype为avi才添加
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");//设置状态为未处理
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);

            mediaProcessMapper.insert(mediaProcess);
            log.debug("添加视频转码任务成功");
        }
    }

    private String getFileMd5(File file){
        try{
            String md5 = DigestUtils.md5Hex(new FileInputStream(file));
            return md5;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UploadFileResultDto uploadMediaFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {
        File file = new File(localFilePath);
        if(!file.exists()){
            XuechengPlusException.cast("文件不存在");
        }
        //获取文件名
        String filename = uploadFileParamsDto.getFilename();
        //获取mimetype
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        //获取文件MD5
        String fileMd5 = getFileMd5(file);

        //获取minio文件对象名
        if(StringUtils.isEmpty(objectName)){
            objectName = getDefaultFolderPath()+fileMd5+extension; //如果没有传对象名，默认使用年月日路径方式
        }
        //上传文件到minio
        boolean flag = addMediaFilesToMinio(bucket_files, objectName, localFilePath, mimeType);

        //uploadFileParamsDto.setFileSize(file.length());
        //上传文件信息到数据库(使用代理对象)
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_files, objectName);

        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 检查文件是否存在
     * @param fileMd5
     * @return
     */
    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) {
        //查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles != null){
            try{
                //获取文件路径
                String filename = mediaFiles.getFilename();
                String ext = filename.substring(filename.lastIndexOf("."));
                String objectName = getMergeFileFolder(fileMd5)+fileMd5+ext;
                //查询minio
                GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket_videofiles).object(objectName).build();
                InputStream stream = minioClient.getObject(getObjectArgs);
                if(stream != null){
                    //文件已存在
                    return RestResponse.success(true);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    private String getChunkPath(String fileMd5, int chunkIndex){
        String folder = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+"chunk"+"/"+chunkIndex;
        return folder;
    }

    /**
     * 检查分块是否存在
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //获取分块路径
        String chunkPath = getChunkPath(fileMd5, chunkIndex);
        try{
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket_videofiles).object(chunkPath).build();
            InputStream stream = minioClient.getObject(getObjectArgs);
            if(stream != null){
                //分块已存在
                return RestResponse.success(true);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        //分块不存在
        return RestResponse.success(false);
    }

    /**
     * 上传分块
     * @param fileMd5
     * @param chunkIndex
     * @param localfilePath
     * @return
     */
    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunkIndex,String localfilePath) {
        String mimeType = getMimeType(null);
        String objectName = getChunkPath(fileMd5,chunkIndex);
        boolean flag = addMediaFilesToMinio(bucket_videofiles, objectName, localfilePath, mimeType);
        if(flag){
            return RestResponse.success(true);
        }
        log.error("上传分块文件失败：{}",objectName);
        return RestResponse.validfail("上传分块失败");
    }

    private String getMergeFileFolder(String fileMd5){
        return fileMd5.charAt(0)+"/"+fileMd5.charAt(1)+"/";
    }

    @Override
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto){
        //1.合并分块
        //得到所有分块
        String chunkFolder = getMergeFileFolder(fileMd5)+"chunk/";
//        List<ComposeSource> sources = Stream.iterate(0, i->i++)
//                .limit(chunkTotal)
//                .map(i -> ComposeSource.builder().bucket(bucket_videofiles).object(chunkFolder + i).build())
//                .collect(Collectors.toList());
        final ArrayList<ComposeSource> sourceList = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            final ComposeSource source = ComposeSource.builder().bucket(bucket_videofiles).object(chunkFolder + i).build();
            sourceList.add(source);
        }
        String fileName = uploadFileParamsDto.getFilename();
        String ext = fileName.substring(fileName.lastIndexOf("."));
        String mergeFilePath = getMergeFileFolder(fileMd5)+fileMd5+ext;
        try{
            ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                    .bucket(bucket_videofiles)
                    .object(mergeFilePath)
                    .sources(sourceList)
                    .build();
            //合并文件
            minioClient.composeObject(composeObjectArgs);
            log.debug("合并文件成功：{}",mergeFilePath);
        }catch(Exception e){
            log.error("合并文件失败，fileMd5:{}，异常：{}",fileMd5,e.getMessage());
            return RestResponse.validfail(false,"合并文件失败");
        }

        //2.验证md5
        File minioFile = downloadFileFromMinIO(bucket_videofiles, mergeFilePath);
        //设置文件大小
        uploadFileParamsDto.setFileSize(minioFile.length());
        if(minioFile == null){
            log.error("下载合并后的文件失败：{}",mergeFilePath);
            return RestResponse.validfail(false,"下载合并后的文件失败");
        }
        try{
            FileInputStream fileInputStream = new FileInputStream(minioFile);
            String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
            if(!mergeFileMd5.equals(fileMd5)){
                log.error("文件校验失败,源文件：{}，下载的文件：{}",fileMd5,mergeFileMd5);
                return RestResponse.validfail(false,"文件校验失败");
            }
        }catch(Exception e) {
            log.error("下载的文件不存在");
        }finally {
            //删除临时文件
            if(minioFile!=null){
                minioFile.delete();
            }
        }

        //3.文件信息入库
        currentProxy.addMediaFilesToDb(companyId,fileMd5,uploadFileParamsDto,bucket_videofiles,mergeFilePath);

        //4.删除分块文件
        clearChunk(chunkFolder,chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 清除分块文件
     * @param chunkFileFolder
     * @param chunkTotal
     */
    private void clearChunk(String chunkFileFolder,int chunkTotal){
        try{
//            List<DeleteObject> objects = Stream.iterate(0, i->i++)
//                    .limit(chunkTotal)
//                    .map(i -> new DeleteObject(chunkFileFolder+i))
//                    .collect(Collectors.toList());
            List<DeleteObject> deleteObjects = new ArrayList<>();
            for (int i = 0; i < chunkTotal; i++) {
                deleteObjects.add(new DeleteObject(chunkFileFolder+i));
            }
            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                    .bucket(bucket_videofiles)
                    .objects(deleteObjects)
                    .build();
            final Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            //遍历每一个分块并删除
            results.forEach(item -> {
                DeleteError deleteError = null;
                try{
                    deleteError = item.get();
                }catch(Exception e){
                    log.error("删除分块失败：{}，",e.getMessage());
                }
            });
            log.debug("删除所有分块文件成功");
        }catch(Exception e){
            log.error("删除所有分块文件失败：{}，异常：{}",chunkFileFolder,e.getMessage());
        }
    }

    /**
     * 从minio下载文件
     * @param bucket
     * @param objectName
     * @return
     */
    @Override
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
