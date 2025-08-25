package com.theoyu.oursphere.user.api;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.user.constants.ApiConstants;
import com.theoyu.oursphere.user.dto.request.FindUserByPhoneReqDTO;
import com.theoyu.oursphere.user.dto.request.RegisterUserReqDTO;
import com.theoyu.oursphere.user.dto.request.UpdateUserPasswordReqDTO;
import com.theoyu.oursphere.user.dto.response.FindUserByPhoneRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface  UserFeignApi {
    String PREFIX = "/user";

    /**
     * 用户注册
     *
     * @param registerUserReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserReqDTO registerUserReqDTO);

    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/findByPhone")
    Response<FindUserByPhoneRspDTO> findByPhone(@RequestBody FindUserByPhoneReqDTO findUserByPhoneReqDTO);
    /**
     * 更新密码
     *
     * @param updateUserPasswordReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

}
