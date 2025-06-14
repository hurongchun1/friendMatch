package com.hrc.friendMatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hrc.friendMatch.model.domain.Team;
import com.hrc.friendMatch.model.domain.User;
import com.hrc.friendMatch.model.request.TeamJoinRequest;
import com.hrc.friendMatch.model.request.TeamQuitRequest;
import com.hrc.friendMatch.model.request.TeamRequest;
import com.hrc.friendMatch.model.request.TeamUpdateRequest;
import com.hrc.friendMatch.model.vo.TeamUserVO;

import java.util.List;

/**
*
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    public long addTeam(Team team, User loginUser);

    /**
     * 根据标签内容查询队伍
     * @param teamRequest
     * @return
     */
    List<TeamUserVO> listTeam(TeamRequest teamRequest,boolean isAdmin);

    /**
     * 修改队伍信息
     * @param teamRequest
     * @param loginUser
     * @return
     */
    Boolean teamUpdate(TeamUpdateRequest teamRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(Integer id, User loginUser);


}
