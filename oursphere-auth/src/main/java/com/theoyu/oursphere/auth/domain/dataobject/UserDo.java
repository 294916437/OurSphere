package com.theoyu.oursphere.auth.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDo {
    private Long id;

    private String username;

    private Date createTime;

    private Date updateTime;

}