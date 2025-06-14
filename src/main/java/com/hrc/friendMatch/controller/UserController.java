package com.hrc.friendMatch.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrc.friendMatch.common.BaseResponse;
import com.hrc.friendMatch.common.ErrorCode;
import com.hrc.friendMatch.common.ResultUtils;
import com.hrc.friendMatch.exception.BusinessException;
import com.hrc.friendMatch.model.domain.User;
import com.hrc.friendMatch.model.request.DeleteUserRequest;
import com.hrc.friendMatch.model.request.UserLoginRequest;
import com.hrc.friendMatch.model.request.UserRegisterRequest;
import com.hrc.friendMatch.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hrc.friendMatch.contant.UserConstant.USER_LOGIN_STATE;

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
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000/"},allowCredentials = "true")
@Slf4j
@Api(tags = "用户接口")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法【比如是否已经被封号等】
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {

        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteUserRequest deleteUserRequest, HttpServletRequest request) {

        if (!userService.isAdmin(request)) {
            return null;
        }

        if (deleteUserRequest.getId() <= 0) {
            return null;
        }
        boolean b = userService.removeById(deleteUserRequest.getId());

        return ResultUtils.success(b);
    }



    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if(tagNameList.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        //校验参数是否为空
        if(user == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //校验登录的用户是否为空
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        int result = userService.updateUser(user,loginUser);
        return  null;
    }

    /**
     * 推荐的用户
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageNum,long pageSize,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        //判断缓存中是否存在数据
        String key = StrUtil.format("yupao:user:recommend:{}",loginUser.getId());
        String userData = valueOperations.get(key);
        if(userData != null && !userData.isEmpty()){
            Page userPage = JSONUtil.toBean(userData, Page.class);
            //缓存中存在数据
            return ResultUtils.success(userPage);
        }
        //缓存中不存在数据，需要重新查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userIPage = new Page<>(pageNum,pageSize);
        Page<User> userPage = userService.page(userIPage, queryWrapper);

        //还需要将查询到的数据，添加到redis中
        try{
            //因为缓存及时添加失败了，也不应该导致程序报错，走统一异常处理
            valueOperations.set(key, JSONUtil.toJsonStr(userPage),30, TimeUnit.SECONDS);
        }catch (Exception e){
            log.error("缓存添加失败！！",e);
        }
        return ResultUtils.success(userPage);
    }


    /**
     * 随机匹配
     * @return
     */
    @GetMapping("match")
    public BaseResponse<List<User>> matchUsers(long num , HttpServletRequest request){
        if(num <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return ResultUtils.success(userService.matchUsers(num,loginUser));
    }
}
