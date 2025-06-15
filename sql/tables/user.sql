create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(255)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(256)                       not null comment '密码',
    phone        varchar(512)                       null comment '手机号',
    email        varchar(512)                       null comment '邮箱',
    userStatus   tinyint  default 0                 null comment '是否有效',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '替换时间',
    isDelete     tinyint  default 0                 null comment '是否删除',
    userRole     int      default 0                 not null comment '用户角色 0-普通会员  1-管理员',
    planetCode   varchar(512)                       null comment '星球编号',
    tags         varchar(1024)                      null comment '标签 json 列表',
    profile      varchar(1024)                      null comment '个人简介'
);

-- 队伍表
create table team
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    expireTime  datetime                           null comment '过期时间',
    userId      bigint comment '用户Id',
    status      int      default 0                 not null comment '0 - 公共，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    createTime  datetime default current_timestamp null comment '创建时间',
    updateTime  datetime default current_timestamp null on update current_timestamp comment '修改时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '队伍';

-- 用户队伍表
create table user_team
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint comment '用户Id',
    teamId      bigint comment '队伍Id',
    joinTime    datetime      null     comment '加入时间',
    createTime  datetime default current_timestamp null comment '创建时间',
    updateTime  datetime default current_timestamp null on update current_timestamp comment '修改时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)comment  '用户队伍表';