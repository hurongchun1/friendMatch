package com.hrc.friendMatch.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrc.friendMatch.mapper.UserTeamMapper;
import com.hrc.friendMatch.model.domain.UserTeam;
import com.hrc.friendMatch.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
*
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
implements UserTeamService {

}
