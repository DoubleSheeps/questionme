package com.questionme.service;

import com.questionme.error.BusinessException;
import com.questionme.service.model.UserModel;

import java.util.List;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserByOpenid(String openid);

    //注册校验
    Boolean isRegistered(String openid);

    //注册
    void register(UserModel userModel) throws BusinessException;

    //修改用户信息
    UserModel updateUser(UserModel userModel);

    //获取用户关注列表
    List<UserModel> getFollowList(Integer userId);

    //获取用户粉丝列表
    List<UserModel> getFansList(Integer followId);

    //关注用户
    void followUser(Integer userId, Integer followId);

    //取消关注
    void cancelFollow(Integer userId, Integer followId);

}
