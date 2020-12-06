package com.questionme.service.impl;

import com.questionme.dao.FollowDOMapper;
import com.questionme.dao.PraiseDOMapper;
import com.questionme.dao.UserDOMapper;
import com.questionme.dataobject.FollowDO;
import com.questionme.dataobject.UserDO;
import com.questionme.error.BusinessException;
import com.questionme.error.EmBusinessError;
import com.questionme.service.UserService;
import com.questionme.service.model.UserModel;
import com.questionme.validator.ValidationResult;
import com.questionme.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private FollowDOMapper followDOMapper;

    @Autowired
    private PraiseDOMapper praiseDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserByOpenid(String openid) {
        UserDO userDO = userDOMapper.selectByOpenid(openid);
        if(userDO==null){
            return null;
        }
        return convertFromDataObject(userDO);
    }

    @Override
    public Boolean isRegistered(String openid) {
        UserDO userDO = userDOMapper.selectByOpenid(openid);
        if(userDO==null){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        ValidationResult validationResult = validator.validate(userModel);
        if(validationResult.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrMsg());
        }

        UserDO userDO = convertFromModel(userModel);

        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"已注册");
        }
        userModel.setId(userDO.getId());
    }

    @Override
    @Transactional
    public UserModel updateUser(UserModel userModel) {
        UserDO userDO = this.convertFromModel(userModel);
        int num = userDOMapper.updateByPrimaryKeySelective(userDO);
        if(num != 1){
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
        return userModel;
    }

    @Override
    public List<UserModel> getFollowList(Integer userId) {
        List<FollowDO> followDOList = followDOMapper.selectByUserId(userId);
        if(followDOList == null || followDOList.size() == 0 ) {
            throw new BusinessException(EmBusinessError.FOLLOW_IS_EMPTY);
        }
        List<UserModel> userModelList = convertFromFollowDO(followDOList, 1);
        return userModelList;
    }

    @Override
    public List<UserModel> getFansList(Integer followId) {
        List<FollowDO> followDOList = followDOMapper.selectByFollowId(followId);
        if(followDOList == null || followDOList.size() == 0 ) {
            throw new BusinessException(EmBusinessError.FOLLOW_IS_EMPTY);
        }
        List<UserModel> userModelList = convertFromFollowDO(followDOList, 2);
        return userModelList;
    }

    @Override
    @Transactional
    public void followUser(Integer userId, Integer followId) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);
        UserDO followUserDO = userDOMapper.selectByPrimaryKey(followId);
        if(userDO == null || followUserDO == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        userDOMapper.increaseFollowNumber(userId);
        userDOMapper.increaseFansNumber(followId);
        FollowDO followDO = new FollowDO();
        followDO.setUserId(userId);
        followDO.setFollowId(followId);
        followDO.setStatus(1);
        followDO.setCreateTime(new Date());
        followDO.setUpdateTime(new Date());
        followDOMapper.insertSelective(followDO);
    }

    @Override
    @Transactional
    public void cancelFollow(Integer userId, Integer followId) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);
        UserDO followUserDO = userDOMapper.selectByPrimaryKey(followId);
        if(userDO == null || followUserDO == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        FollowDO followDO = followDOMapper.selectByUserIdAndFollowId(userId, followId);
        int res1 = userDOMapper.decreaseFollowNumber(userId);
        int res2 = userDOMapper.decreaseFansNumber(followId);
        if(followDO == null || res1 == 0 || res2 == 0) {
            throw new BusinessException(EmBusinessError.FOLLOW_ERROR);
        }
        followDOMapper.deleteByPrimaryKey(followDO.getId());
    }

    //实现model->dataobject方法
    private UserDO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO){
        if(userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        return userModel;
    }

    private List<UserModel> convertFromFollowDO(List<FollowDO> followDOList, Integer mode) {
        if(mode == 1){              //模式1，获取关注列表
            List<UserModel> userModelList = followDOList.stream().map(followDO -> {
                UserDO userDO =userDOMapper.selectByPrimaryKey(followDO.getFollowId());
                if(userDO == null){
                    return null;
                }
                UserModel userModel = convertFromDataObject(userDO);
                return userModel;
            }).collect(Collectors.toList());
            return userModelList;
        }else if(mode == 2) {       //模式2，获取粉丝列表
            List<UserModel> userModelList = followDOList.stream().map(followDO -> {
                UserDO userDO =userDOMapper.selectByPrimaryKey(followDO.getUserId());
                if(userDO == null){
                    return null;
                }
                UserModel userModel = convertFromDataObject(userDO);
                return userModel;
            }).collect(Collectors.toList());
            return userModelList;
        }
        return null;
    }
}
