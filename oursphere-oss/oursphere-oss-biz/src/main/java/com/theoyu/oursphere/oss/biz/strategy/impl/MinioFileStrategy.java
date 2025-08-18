package com.theoyu.oursphere.oss.biz.strategy.impl;

import com.theoyu.oursphere.oss.biz.strategy.FileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class MinioFileStrategy implements FileStrategy {

    @Override
    public String uploadFile(MultipartFile file,String bucketName) {
        log.info("上传文件到Minio的bucketName: {}", bucketName);
        return null;
    }

}
