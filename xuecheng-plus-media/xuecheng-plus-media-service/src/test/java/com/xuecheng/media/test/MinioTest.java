package com.xuecheng.media.test;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * minio文件系统测试
 */
public class MinioTest {

    private MinioClient minioClient =
                MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    void testUpload(){
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpg");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
//                    .object("test001.mp4")
                    .object("001/pic01.jpg")//添加子目录
                    .filename("D:\\temp\\upload\\pic01.jpg")
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    @Test
    void testDelete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("001/pic01.jpg").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    void testSearch(){
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("001/pic01.jpg").build();
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\temp\\upload\\pic01_a.jpg"));
        ) {
            IOUtils.copy(inputStream,outputStream);
            //校验文件的完整性对文件的内容进行md5
            FileInputStream fileInputStream1 = new FileInputStream(new File("D:\\temp\\upload\\pic01.jpg"));
            String source_md5 = DigestUtils.md5Hex(fileInputStream1);

            FileInputStream fileInputStream = new FileInputStream(new File("D:\\temp\\upload\\pic01_a.jpg"));
            String local_md5 = DigestUtils.md5Hex(fileInputStream);
            if(source_md5.equals(local_md5)){
                System.out.println("下载成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void test() throws Exception{
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("video").object("6/a/6afa0e83748cebd6750145d38bd4fb5f.mp4").build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        File minioFile = File.createTempFile("minio", ".temp");
        FileOutputStream outputStream = new FileOutputStream(minioFile);

        IOUtils.copy(inputStream,outputStream);

        final String fileMd5 = DigestUtils.md5Hex(new FileInputStream(minioFile));
        System.out.println(fileMd5);
        System.out.println(minioFile.length());
    }

    @Test
    void uploadChunk() throws Exception{
        int len = 6;
        for (int i = 0; i < len; i++) {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("chunk/"+i)//添加子目录
                    .filename("D:\\temp\\bigfile_test\\chunk\\"+i)
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传分块成功"+i);
        }
    }

    @Test
    void testMerge() throws Exception{
        int len = 3;
        String bucket = "video";
        String mergeFileFolder = "d/f/";

//        List<ComposeSource> sources = Stream.iterate(0,i->i++)
//                .limit(len)
//                .map(i -> ComposeSource.builder().bucket(bucket).object(mergeFileFolder + "chunk/" + i).build())
//                .collect(Collectors.toList());
        final ArrayList<ComposeSource> sourceList = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            final ComposeSource source = ComposeSource.builder().bucket(bucket).object(mergeFileFolder + "chunk/" + i).build();
            sourceList.add(source);
        }
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket).object(mergeFileFolder+"video.mov").sources(sourceList).build();
        //合并文件
        minioClient.composeObject(composeObjectArgs);

        //校验md5值
        FilterInputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(mergeFileFolder+"video.mov")
                .build());
        //创建临时文件
        File minioFile = File.createTempFile("minio", ".merge");
        FileOutputStream outputStream = new FileOutputStream(minioFile);
        IOUtils.copy(stream,outputStream);

        final String mergefile_md5 = DigestUtils.md5Hex(new FileInputStream(minioFile));
        File sourcefile = new File("D:\\temp\\upload\\2.mov");
        final String sourcefile_md5 = DigestUtils.md5Hex(new FileInputStream(sourcefile));
        if(sourcefile_md5.equals(mergefile_md5)){
            System.out.println("文件校验成功");
        }else{
            System.out.println("文件校验不成功：{}");
        }
    }



}
