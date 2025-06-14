package com.hrc.friendMatch.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrc.friendMatch.common.ErrorCode;
import com.hrc.friendMatch.exception.BusinessException;
import com.hrc.friendMatch.mapper.TeamMapper;
import com.hrc.friendMatch.model.domain.Team;
import com.hrc.friendMatch.model.domain.User;
import com.hrc.friendMatch.model.domain.UserTeam;
import com.hrc.friendMatch.model.enums.TeamStatusEnum;
import com.hrc.friendMatch.model.request.TeamJoinRequest;
import com.hrc.friendMatch.model.request.TeamQuitRequest;
import com.hrc.friendMatch.model.request.TeamRequest;
import com.hrc.friendMatch.model.request.TeamUpdateRequest;
import com.hrc.friendMatch.model.vo.TeamUserVO;
import com.hrc.friendMatch.model.vo.UserVO;
import com.hrc.friendMatch.service.TeamService;
import com.hrc.friendMatch.service.UserService;
import com.hrc.friendMatch.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.spring.web.scanners.CachingOperationReader;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author hrc
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;
    /**
     * 用户可创建的队伍数量，以及用户最多能进入队伍的数量
     */
    public final static int USER_TEAMS = 5;
    /**
     * 用户队伍数量
     */
    public final static int TEAM_NUMBERS = 20;
    /**
     * 队伍描述最大数量
     */
    public final static int TEAM_DESCRIPTION_MAX = 512;
    /**
     * 密码长度
     */
    public final static int PASSWORD_LENGTH_MAX = 32;
    @Autowired
    private CachingOperationReader cachingOperationReader;

    /**
     * 添加队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {

//        1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        //用户id
        final long userId = loginUser.getId();
        //        3. 校验信息
//            1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > TEAM_NUMBERS) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不能太长");
        }
//            2. 队伍标题 <= 20
        String name = team.getName();
        if (StrUtil.isEmptyIfStr(name) || name.length() > TEAM_NUMBERS) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标签过长");
        }
//            3. 描述 <= 512
        String description = team.getDescription();
        if (StrUtil.isBlankIfStr(description) || description.length() > TEAM_DESCRIPTION_MAX) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
//            4. status 是否公开（int）不传默认为0 （公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getContextByStatus(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
//            5. 如果status是加密状态，一定要有密码 ，且密码 <= 32
        String password = team.getPassword();
        if (status == 2) {
            if (StrUtil.isBlank(password) || password.length() > PASSWORD_LENGTH_MAX) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
//            6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间设置有误");
        }
//            7. 校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long userTeams = this.count(queryWrapper);
        if (userTeams > USER_TEAMS) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
//        4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean isSuccess = save(team);
//        5. 插入用户 => 队伍关系到关系表
        if (!isSuccess) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加队伍失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        isSuccess = userTeamService.save(userTeam);
        if (!isSuccess) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加队伍失败");
        }
        return team.getId();
    }

    @Override
    public List<TeamUserVO> listTeam(TeamRequest teamRequest, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //为空代表查询所有
        if (teamRequest != null) {
            //id值
            Long id = teamRequest.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            //根据idlist，查询
            List<Long> idList = teamRequest.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList);
            }
            //名称
            String name = teamRequest.getName();
            if (StrUtil.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            //描述内容
            String description = teamRequest.getDescription();
            if (StrUtil.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            //最大数量
            Integer maxNum = teamRequest.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            //过期时间 不能小于 当前时间
            //expireTime is  null or expireTime < now()

            queryWrapper.and(wq -> wq.gt("expireTime", new Date()).or().isNull("expireTime"));

            Long userId = teamRequest.getUserId();
            if(userId !=null && userId >= 0){
                queryWrapper.eq("userId", userId);
            }

            //状态，只有是 管理员 或者 创建人 ，才能查看私密队伍
            Integer status = teamRequest.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getContextByStatus(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }


            if ((!isAdmin && !TeamStatusEnum.PUBLIC.equals(statusEnum))) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if (status != null && status >= 0) {
                queryWrapper.eq("status", status);
            }

            //根据关键字，查询队伍名和描述
            String searchText = teamRequest.getSearchText();
            if (StrUtil.isNotBlank(searchText)) {
                queryWrapper.and(wq -> wq.like("name", searchText).or().like("description", searchText));
            }
        }
        //查询出满足条件的所有team信息
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //需要将 team 转换成 teamUserVo
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        //查询关联创建人的信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            //为空说明用户有问题
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            //需要将user转换成 userVO
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtil.copyProperties(team, teamUserVO);
            //用户信息脱敏
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);
            teamUserVO.setCreateUser(userVO);
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public Boolean teamUpdate(TeamUpdateRequest teamRequest, User loginUser) {
        //判断请求参数是否为空
        if (teamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断id是否正确
        Integer id = teamRequest.getId();
        Team oldTeam = this.getTeamById(id);
        //判断是否为管理员 ， 或者为队伍创建者
        boolean isAdmin = userService.isAdmin(loginUser);
        if (!isAdmin && oldTeam.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Integer status = teamRequest.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getContextByStatus(status);
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StrUtil.isBlankIfStr(teamRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密状态密码有密码");
            }
        }
        //更新
        Team team = new Team();
        BeanUtil.copyProperties(teamRequest, team);
        return this.updateById(team);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        //判断加入参数是否为空
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断队伍是否存在
        Integer teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        //判断队伍是否已满、未过期
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())) {
            //说明已过期
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 用户最多只能加入5个队伍
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long userTeamCount = userTeamService.count(userTeamQueryWrapper);
        if (userTeamCount >= USER_TEAMS) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多只能加入5个队伍");
        }
        //判断加入的队伍，是否已经加入过
        userTeamQueryWrapper.eq("teamId", teamId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经加入队伍");
        }

        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getContextByStatus(status);
        //私有队伍，不能加入
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "私有队伍，不能加入");
        }
        //加密队伍，输入的密码必须一样
        String teamPassword = team.getPassword();
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StrUtil.isEmptyIfStr(password) || !password.equals(teamPassword)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "密码错误，加入队伍失败");
            }
        }

        //添加队伍，用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setJoinTime(new Date());
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        return userTeamService.save(userTeam);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //校验请求参数
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验队伍是否存在
        Integer teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        //校验我是否加入队伍
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long count = userTeamService.count(userTeamQueryWrapper);
        //没加入队伍
        if (count <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户没加入队伍");
        }
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long joinTeamNumber = userTeamService.count(userTeamQueryWrapper);
        //只有一个人，直接解散队伍
        if (joinTeamNumber == 1) {
            //先删除用户队伍关联表
            userTeamService.remove(userTeamQueryWrapper);
            //删除队伍表信息
            QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
            teamQueryWrapper.eq("id", teamId);
            this.remove(teamQueryWrapper);
        } else {
            //说明还有其他人
            //说明是队长
            if (userId == team.getUserId()) {

                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR);
                }
                //退出队伍
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.eq("userId", userId);
                boolean result = userTeamService.remove(userTeamQueryWrapper);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
                }
                UserTeam userTeam = userTeamList.get(1);
                //将队伍的队长id转移
                team.setUserId(userTeam.getUserId());
                result = this.updateById(team);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
                }
                return true;
            } else {
                //不是队长
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.eq("userId", userId);
                boolean result = userTeamService.remove(userTeamQueryWrapper);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Integer id, User loginUser) {
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = getTeamById(id);
        Long teamUserId = team.getUserId();
        long userId = loginUser.getId();
        if(!teamUserId.equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",id);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解散队伍失败");
        }
        //根据id删除
        boolean isSuccess = this.removeById(id);
        if(!isSuccess){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return true;
    }



    public Team getTeamById(Integer teamId){
        //可以防止到缓存穿透的问题
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        //判断队伍是否存在
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }
}






