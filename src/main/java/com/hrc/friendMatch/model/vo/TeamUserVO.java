package com.hrc.friendMatch.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @FileName: TeamUserVO
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/17 17:05
 * @Version: 1.0.0
 */
@Data
public class TeamUserVO implements Serializable {
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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 创建的用户信息
     */
    private UserVO createUser;
    /**
     * 加入人信息
     */
    private List<UserVO> userList;
    /**
     * 是否加入队伍
     */
    private boolean hasJoin =false;
    /**
     * 加入队伍人数
     */
    private Integer hasJoinNum;
}
