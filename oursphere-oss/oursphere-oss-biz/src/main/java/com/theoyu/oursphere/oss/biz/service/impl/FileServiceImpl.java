package com.theoyu.oursphere.oss.biz.service.impl;

import com.theoyu.framework.common.constants.GlobalConstants;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.oss.biz.service.FileService;
import com.theoyu.oursphere.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStrategy fileStrategy;

    @Override
    public Response<?> uploadFile(MultipartFile file){
        // 获取最终返回的文件访问地址
        String fileUrl = fileStrategy.uploadFile(file, GlobalConstants.BUCKET_NAME);

        return Response.success(fileUrl);
    }
}
