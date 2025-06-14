package com.hrc.friendMatch.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @FileName: PageInfo
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/14 20:32
 * @Version: 1.0.0
 */
@Data
public class PageInfo{
    protected long pageSize =10;
    protected long pageNum = 1;

}
