package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaProcessService {
    List<MediaProcess> getMediaProcessBySharedIndex(int sharedTotal,int sharedIndex,int count);

    boolean startTask(Long taskId);

    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
