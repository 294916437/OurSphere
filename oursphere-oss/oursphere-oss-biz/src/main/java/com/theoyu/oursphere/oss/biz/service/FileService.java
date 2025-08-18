package com.theoyu.oursphere.oss.biz.service;

import com.theoyu.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;


public interface  FileService {

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    Response<?> uploadFile(MultipartFile file);
}
