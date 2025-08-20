package com.theoyu.oursphere.oss.biz.controller;

import com.theoyu.framework.common.constants.GlobalConstants;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import com.theoyu.oursphere.oss.biz.enums.ResponseCodeEnum;
import com.theoyu.oursphere.oss.biz.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    private FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> uploadFile(@RequestPart(value = "file") MultipartFile file) {
        log.info("当前用户 ID: {}", LoginUserContextHolder.getUserId());
        //控制器层面校验文件大小
        if (file.getSize() > GlobalConstants.MAX_FILE_SIZE) {
            throw new BusinessException(ResponseCodeEnum.MAX_FILE_SIZE_EXCEEDED);
        }
        return fileService.uploadFile(file);
    }
}
