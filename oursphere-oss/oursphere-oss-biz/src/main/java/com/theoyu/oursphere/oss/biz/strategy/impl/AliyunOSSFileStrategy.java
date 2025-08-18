package com.theoyu.oursphere.oss.biz.strategy.impl;
import com.theoyu.oursphere.oss.biz.strategy.FileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
@Slf4j


public class AliyunOSSFileStrategy implements FileStrategy {
    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("上传文件到阿里云OSS");
        return null;
    }
}
