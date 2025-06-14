package com.hrc.friendMatch.model.enums;

/**
 * @FileName: TeamStatusEnum
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/14 21:51
 * @Version: 1.0.0
 */
public enum TeamStatusEnum {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私密"),
    SECRET(2,"加密");

    private Integer status;
    private String context;

    TeamStatusEnum(Integer status, String context) {
        this.status = status;
        this.context = context;
    }

    /**
     * 根据状态获取内容
     * @return
     */
    public static TeamStatusEnum getContextByStatus(Integer status) {
        if(status == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for(TeamStatusEnum value:values){
            if(status.equals(value.status)) {
                return value;
            }
        }
        return null;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
