package com.hrc.friendMatch.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 * @FileName: TeamJoinRequest
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/17 22:00
 * @Version: 1.0.0
 */
@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = -5487432948340188125L;
    private Integer teamId;
    private String password;
}
