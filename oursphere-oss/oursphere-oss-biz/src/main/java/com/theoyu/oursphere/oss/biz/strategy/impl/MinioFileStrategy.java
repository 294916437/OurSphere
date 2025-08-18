package com.theoyu.oursphere.oss.biz.strategy.impl;

import com.theoyu.oursphere.oss.biz.config.MinioProperties;
import com.theoyu.oursphere.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
public class MinioFileStrategy implements FileStrategy {

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private MinioClient minioClient;

    @Override
    @SneakyThrows
    public String uploadFile(MultipartFile file,String bucketName) {
        log.info("上传文件到Minio的bucketName: {}", bucketName);
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传的文件为空或者大小为0");
            throw new RuntimeException("文件大小不能为0");
        }
        String originalFilename = file.getOriginalFilename();

        String contentType = file.getContentType();

        String key = UUID.randomUUID().toString().replace("-", "");
        String suffix;
        if (originalFilename != null) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }else {
            log.error("==> 上传的文件名为空");
            throw new RuntimeException("文件名不能为空");
        }
        String newFileName = key + suffix;

        log.info("==> 开始上传文件至 Minio, 文件名为: {}", newFileName);

        // 上传文件至 Minio
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(newFileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(contentType)
                .build());

        String fileUrl = minioProperties.getEndpoint() + "/" + bucketName + "/" + newFileName;
        log.info("==> 文件上传到 Minio 成功, 文件访问地址: {}", fileUrl);
        return fileUrl;
    }

}
