package com.theoyu.oursphere.kv.api;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.constants.ApiConstants;
import com.theoyu.oursphere.kv.dto.request.AddNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.FindNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.response.FindNoteContentRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface  KeyValueFeignApi {
    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);
    @PostMapping(value = PREFIX + "/note/content/find")
    Response<FindNoteContentRspDTO> findNoteContent(@RequestBody FindNoteContentReqDTO findNoteContentReqDTO);

}
