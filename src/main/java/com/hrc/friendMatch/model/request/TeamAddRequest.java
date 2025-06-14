package com.hrc.friendMatch.model.request;

import lombok.Data;

import java.util.Date;

/**
 * @FileName: TeamAddRequest
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/14 22:21
 * @Version: 1.0.0
 */
@Data
public class TeamAddRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 0 - 公共，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}
