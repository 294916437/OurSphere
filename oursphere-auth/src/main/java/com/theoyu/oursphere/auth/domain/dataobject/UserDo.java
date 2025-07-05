package com.theoyu.oursphere.auth.domain.dataobject;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserDo {
    private Long id;

    private String userId;

    private String password;

    private String nickname;

    private String avatar;

    private Date birthday;

    private String backgroundImg;

    private String phone;

    private Byte sex;

    private Byte status;

    private String introduction;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

}