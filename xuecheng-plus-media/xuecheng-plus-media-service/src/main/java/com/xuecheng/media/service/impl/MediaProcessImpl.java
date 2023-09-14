package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MediaProcessImpl implements MediaProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 根据分片索引查询任务列表
     * @param sharedTotal 分片总数
     * @param sharedIndex 分片索引
     * @param count 查询记录数
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessBySharedIndex(int sharedTotal, int sharedIndex, int count) {
        return mediaProcessMapper.selectMediaProcessBySharedIndex(sharedTotal,sharedIndex,count);
    }

    /**
     * 开启任务
     * @param taskId
     * @return
     */
    @Override
    public boolean startTask(Long taskId) {
        int result = mediaProcessMapper.startTask(taskId);
        return result>0?true:false;
    }

    /**
     * 更新任务处理完成的状态
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查询任务，如果不存在则返回
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return ;
        }

        //1.任务处理失败
        if(status.equals("3")){
            //更新任务处理结果
            LambdaQueryWrapper<MediaProcess> lqw = new LambdaQueryWrapper<>();
            lqw.eq(MediaProcess::getId,taskId);
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1); //失败次数+1
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.update(mediaProcess,lqw);
            return;
        }

        //2.任务处理成功
        if(status.equals("2")){
            //更新mediafiles表
            final MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);

            //更新任务处理结果,并删除
            mediaProcess.setStatus("3"); //设置状态为处理成功
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.deleteById(mediaProcess);

            //添加到历史记录
            final MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
            mediaProcessHistoryMapper.insert(mediaProcessHistory);
        }
    }
}
