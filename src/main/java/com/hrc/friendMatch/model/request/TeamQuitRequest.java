package com.hrc.friendMatch.model.request;

/**
 * @FileName: TeamQuitRequest
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/18 10:34
 * @Version: 1.0.0
 */

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 9046292191337254033L;

    private  Integer teamId;
}
