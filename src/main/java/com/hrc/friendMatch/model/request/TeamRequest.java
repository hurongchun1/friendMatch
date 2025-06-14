package com.hrc.friendMatch.model.request;

import com.hrc.friendMatch.common.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @FileName: TeamRequest
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/14 20:10
 * @Version: 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamRequest extends PageInfo {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;
    /**
     * id 列表
     */
    private List<Long> idList;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

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
     * 关键字，查询描述和标签
     */
    private String searchText;
}
