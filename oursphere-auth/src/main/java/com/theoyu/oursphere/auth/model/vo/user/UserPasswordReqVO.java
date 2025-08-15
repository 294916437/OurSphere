package com.theoyu.oursphere.auth.model.vo.user;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPasswordReqVO {

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
