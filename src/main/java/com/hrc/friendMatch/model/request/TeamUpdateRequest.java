package com.hrc.friendMatch.model.request;

import lombok.Data;

import java.util.Date;

/**
 * @FileName: TeamUpdateRequest
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/17 20:49
 * @Version: 1.0.0
 */
@Data
public class TeamUpdateRequest {
    /**
     * id
     */
    private Integer id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * 0 - 公共，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}
