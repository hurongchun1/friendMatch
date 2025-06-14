package com.hrc.friendMatch.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrc.friendMatch.common.BaseResponse;
import com.hrc.friendMatch.common.DeleteRequest;
import com.hrc.friendMatch.common.ErrorCode;
import com.hrc.friendMatch.common.ResultUtils;
import com.hrc.friendMatch.exception.BusinessException;
import com.hrc.friendMatch.model.domain.Team;
import com.hrc.friendMatch.model.domain.User;
import com.hrc.friendMatch.model.domain.UserTeam;
import com.hrc.friendMatch.model.request.*;
import com.hrc.friendMatch.model.vo.TeamUserVO;
import com.hrc.friendMatch.service.TeamService;
import com.hrc.friendMatch.service.UserService;
import com.hrc.friendMatch.service.UserTeamService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author Ding Jiaxiong
 */

//@CrossOrigin

/**
 * 后端直接支持跨域不安全
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000/"},allowCredentials = "true")
@Slf4j
@Api(tags = "队伍接口")
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;
    /**
     * 添加队伍
     * @param teamRequest
     * @return
     */
   @PostMapping("addTeam")
   public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamRequest, HttpServletRequest request){

       //判断是否为空
       if(teamRequest == null ){
           throw  new BusinessException(ErrorCode.PARAMS_ERROR);
       }
       //将 teamRequest对象 转成 team 对象
       Team team = BeanUtil.toBean(teamRequest, Team.class);
       User loginUser = userService.getLoginUser(request);

       return ResultUtils.success(teamService.addTeam(team, loginUser));
   }

    /**
     * 解散队伍
     * @param deleteRequest
     * @return
     */
   @PostMapping("delete")
   public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
       Integer id = deleteRequest.getId();
       //判断传递的id是否正确
       if(id == null || id <= 0){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
       User loginUser = userService.getLoginUser(request);
       if(loginUser == null){
           throw new BusinessException(ErrorCode.NO_AUTH);
       }
       return ResultUtils.success(teamService.deleteTeam(id,loginUser));
   }

    /**
     * 修改队伍
     * @param teamRequest
     * @return
     */
   @PostMapping("update")
   public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamRequest, HttpServletRequest  request){
       //判断传入的是否正确
       if(teamRequest == null || teamRequest.getId() <= 0L){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }

       User loginUser = userService.getLoginUser(request);

       return ResultUtils.success(teamService.teamUpdate(teamRequest,loginUser));
   }

    /**
     * 根据id查询
     * @param id
     * @return
     */
   @GetMapping("get")
   public BaseResponse<Team> queryById(long id){
       //判断传入的id是否正确
       if(id <= 0L){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

       Team team = teamService.getById(id);
       return ResultUtils.success(team);
   }

    @GetMapping("list")
    public BaseResponse<List<TeamUserVO>> listTeam(TeamRequest teamRequest, HttpServletRequest request){
        //判断传入的参数是否有误
        if(teamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean isAdmin = userService.isAdmin(request);
        //根据条件查询队伍
        List<TeamUserVO> teamList = teamService.listTeam(teamRequest,isAdmin);
        //获取出查询数据的所有队伍
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        //判断当前用户是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try{
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        }catch (Exception e){}
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    /**
     * 查询所有，获取我创建的队伍
     * @param teamRequest
     * @return
     */
   @GetMapping("list/my/create")
   public BaseResponse<List<TeamUserVO>> listMyTeam(TeamRequest teamRequest,HttpServletRequest request){
       //判断传入的参数是否有误
       if(teamRequest==null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
       User loginUser = userService.getLoginUser(request);
       if(loginUser == null){
           throw  new BusinessException(ErrorCode.NO_AUTH);
       }
       teamRequest.setUserId(loginUser.getId());
       //管理员，由于下面方法判断只有管理员才能查看私密方法
       //由于这里已经限制了 userId，所以查询出来的 team 一定是我创建的队伍
       //因为我创建的队伍，可以公开的，加密的，私有的
       List<TeamUserVO> list = teamService.listTeam(teamRequest,true);
        return ResultUtils.success(list);
   }


    /**
     * 查询所有，获取我加入的队伍
     * @param teamRequest
     * @return
     */
    @GetMapping("list/my/join")
    public BaseResponse<List<TeamUserVO>> queryJoinTeam(TeamRequest teamRequest,HttpServletRequest request){
        //判断传入的参数是否有误
        if(teamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        // 取出不重复的队伍 id
        //teamId userId
        //1, 2
        //1, 3
        //2, 1
        //result
        // 1 => 2,3
        // 2 => 1
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamRequest.setIdList(idList);
        //查询
        List<TeamUserVO> list = teamService.listTeam(teamRequest,true);

        return ResultUtils.success(list);
    }

    /**
     * 分页查询
     * @param teamRequest
     * @return
     */
    @GetMapping("list/page")
    public BaseResponse<Page<Team>> queryByPage(TeamRequest teamRequest){
        //判断传入的参数是否有误
        if(teamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = BeanUtil.toBean(teamRequest, Team.class);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = teamService
                .page(new Page<>(teamRequest.getPageNum(), teamRequest.getPageSize()), queryWrapper);
        return ResultUtils.success(page);
    }

    @PostMapping("join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return ResultUtils.success(teamService.quitTeam(teamQuitRequest,loginUser));
    }
}
