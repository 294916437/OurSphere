package com.theoyu.oursphere.search.service;

import org.springframework.http.ResponseEntity;

public interface  ExtDictService {
    /**
     * 获取热更新词典
     * @return
     */
    ResponseEntity<String> getHotUpdateExtDict();
}
