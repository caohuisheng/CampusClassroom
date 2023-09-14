package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask {
    @Autowired
    private MediaProcessService mediaProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 视频处理任务
     */
    @XxlJob("VideoJobHandler")
    public void VideoJobHandler() throws Exception{

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //获取cpu线程数
        int processorCount = Runtime.getRuntime().availableProcessors();
        //获取任务列表
        final List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessBySharedIndex(shardTotal, shardIndex, processorCount);
        final int taskSize = mediaProcessList.size();
        log.debug("任务总数：{}",taskSize);
        if(taskSize == 0){
            return;
        }

        //创建taskSize个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(taskSize);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(taskSize);

        //将处理任务加入线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                try {
                    transferVideo(mediaProcess);
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
        //等待，并设置一个超时时间
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    void transferVideo(MediaProcess mediaProcess){
        //1.开始执行任务
        final Long taskId = mediaProcess.getId();
        final boolean flag = mediaProcessService.startTask(taskId);
        if(!flag){
            return;
        }
        log.debug("开始执行任务：{}",mediaProcess.getFilename());

        //2.从minio下载文件
        String bucket = mediaProcess.getBucket();
        String fileId = mediaProcess.getFileId();
        String filePath = mediaProcess.getFilePath();
        String fileName = mediaProcess.getFilename();

        final File minioFile = mediaFileService.downloadFileFromMinIO(bucket, filePath);
        if(minioFile == null){
            log.error("文件下载失败：bucket:{},objectName:{}",bucket,filePath);
            mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"文件下载失败");
            return;
        }

        //3.处理文件
        //首先创建临时文件
        final String originalFilePath = minioFile.getAbsolutePath();
        File mp4File = null;
        try{
            mp4File = File.createTempFile("minio",".mp4");
        }catch(Exception e){
            log.error("创建临时文件失败：{}",e.getMessage());
            mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"创建临时文件失败");
            return;
        }

        //开始视频转换，成功将返回success
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath,originalFilePath,mp4File.getName(),mp4File.getAbsolutePath());
        String result = videoUtil.generateMp4();
        if(!"success".equals(result)){
            log.error("视频转码失败:{}，filePath:{}",result,filePath);
            mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"视频转码失败");
            return;
        }

        //4.将处理后的文件上传到minio
        String objectName = filePath.substring(0,filePath.lastIndexOf("."))+".mp4";
        final boolean flag1 = mediaFileService.addMediaFilesToMinio(bucket, objectName, mp4File.getAbsolutePath(), "video/mp4");
        if(!flag1){
            mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"上传处理后的文件到minio失败");
            return;
        }
        String url = "/"+bucket+"/"+objectName;
        mediaProcessService.saveProcessFinishStatus(taskId,"2",fileId,url,null);
    }

}
