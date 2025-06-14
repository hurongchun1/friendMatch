package com.hrc.friendMatch.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @FileName: DeleteRequest
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/18 15:27
 * @Version: 1.0.0
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 3463465204116561844L;

    private Integer id;
}
