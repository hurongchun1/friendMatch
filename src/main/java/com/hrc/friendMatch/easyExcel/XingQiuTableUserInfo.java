package com.hrc.friendMatch.easyExcel;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author: hrc
 * @date: 2022/11/20
 * @ClassName: yupao-backend01
 * @Description:    星球表格用户信息
 */
@Data
public class XingQiuTableUserInfo {
    /**
     * id
     */
    @ExcelProperty("成员编号")
    private String planetCode;

    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String username;

}