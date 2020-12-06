package com.questionme.controller;

import com.alibaba.fastjson.JSONObject;
import com.questionme.controller.viewobject.UserVO;
import com.questionme.error.BusinessException;
import com.questionme.error.EmBusinessError;
import com.questionme.response.CommonReturnType;
import com.questionme.service.HttpClientService;
import com.questionme.service.UserService;
import com.questionme.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpClientService httpClientService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType login(@RequestParam(name = "code")String code) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(StringUtils.isEmpty(code)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //String openid = getOpenid(code);
        String openid = code;
        //用户登录服务，用来校验用户登录是否合法
        UserModel userModel = userService.getUserByOpenid(openid);
        if(userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //将登录凭证加入到用户登录成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_USER_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        //登录有效时长（单位：秒）
        this.httpServletRequest.getSession().setMaxInactiveInterval(600);

        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/get",method = {RequestMethod.GET})
    public CommonReturnType getUser() throws BusinessException {
        //获取用户的登录信息
        UserModel userModel = this.getUserModel();
        //将核心领域模型用户对象转化成可供UI使用的viewobject
        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType register(@RequestParam(name = "code")String code,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "imageUrl")String imageUrl,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "school")String school,
                                     @RequestParam(name = "introduction")String introduction,
                                     @RequestParam(name = "price")Double price) throws BusinessException {
        //String openid = getOpenid(code);
        String openid = code;
        if(userService.isRegistered(openid)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户已注册");
        }else {
            UserModel userModel = new UserModel();
            userModel.setName(name);
            userModel.setOpenid(openid);
            userModel.setGender(gender);
            userModel.setImageUrl(imageUrl);
            userModel.setSchool(school);
            userModel.setIntroduction(introduction);
            userModel.setPrice(new BigDecimal(price));
            userModel.setAnswerNumber(0);
            userModel.setAverageTime("0分钟");
            userModel.setFollowNumber(0);
            userModel.setFansNumber(0);
            userModel.setPraiseNumber(0);
            userModel.setAnswerNumber(0);
            userModel.setGrade(new BigDecimal(0.0));
            userModel.setCreateTime(new Date());
            userModel.setUpdateTime(new Date());
            userService.register(userModel);
            //注册成功后自动登录
            //将登录凭证加入到用户登录成功的session内
            this.httpServletRequest.getSession().setAttribute("IS_USER_LOGIN",true);
            this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
            //登录有效时长（单位：秒）
            this.httpServletRequest.getSession().setMaxInactiveInterval(600);
            return CommonReturnType.create(null);
        }
    }

    @RequestMapping(value = "/update",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType updateUser(@RequestParam(name = "name")String name,
                                       @RequestParam(name = "gender")Integer gender,
                                       @RequestParam(name = "school")String school,
                                       @RequestParam(name = "price")Double price,
                                       @RequestParam(name = "introduction")String introduction){
        //获取用户的登录信息
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_USER_LOGIN");
        if(isLogin == null || !isLogin.booleanValue()){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        userModel.setName(name);
        userModel.setSchool(school);
        userModel.setGender(gender);
        userModel.setIntroduction(introduction);
        userModel.setPrice(new BigDecimal(price));
        userModel.setUpdateTime(new Date());
        UserModel userModel1 = userService.updateUser(userModel);
        UserVO userVO = this.convertFromModel(userModel1);
        return CommonReturnType.create(userVO);
    }

    @RequestMapping(value = "/getFollowList",method = {RequestMethod.GET})
    public CommonReturnType getFollowList() {
        //获取用户的登录信息
        UserModel userModel = this.getUserModel();
        List<UserModel> userModelList = userService.getFollowList(userModel.getId());
        if(userModelList == null){
            throw new BusinessException(EmBusinessError.FOLLOW_IS_EMPTY);
        }
        List<UserVO> userVOList = userModelList.stream().map(userModel1 -> {
            UserVO userVO = this.convertFromModel(userModel1);
            return userVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(userVOList);
    }

    @RequestMapping(value = "/getFansList",method = {RequestMethod.GET})
    public CommonReturnType getFansList() {
        //获取用户的登录信息
        UserModel userModel = this.getUserModel();
        List<UserModel> userModelList = userService.getFansList(userModel.getId());
        if(userModelList == null){
            throw new BusinessException(EmBusinessError.FANS_IS_EMPTY);
        }
        List<UserVO> userVOList = userModelList.stream().map(userModel1 -> {
            UserVO userVO = this.convertFromModel(userModel1);
            return userVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(userVOList);
    }

    @RequestMapping(value = "/follow",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType follow(@RequestParam(name = "followId")Integer followId) {
        //获取用户的登录信息
        UserModel userModel = this.getUserModel();
        userService.followUser(userModel.getId(), followId);
        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/cancelFollow",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType cancelFollow(@RequestParam(name = "followId")Integer followId) {
        //获取用户的登录信息
        UserModel userModel = this.getUserModel();
        userService.cancelFollow(userModel.getId(), followId);
        return CommonReturnType.create(null);
    }

    /*
    @RequestMapping(value = "/praise",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType praise(@RequestParam(name = "followId")Integer followId) {
        //获取用户的登录信息
        UserModel userModel = this.getUserModel();
        userService.cancelFollow(userModel.getId(), followId);
        return CommonReturnType.create(null);
    }
    */


    //测试
    @RequestMapping(value = "/test",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType test(@RequestParam(name = "code")String code) {
        //入参校验
        if(StringUtils.isEmpty(code)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //api url地址
        String url = "http://localhost:8090/user/login";
        //post请求
        HttpMethod method =HttpMethod.POST;
        // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        params.add("code", "xxxxx");
        //发送http请求并返回结果
        return CommonReturnType.create(httpClientService.client(url,method,params));
    }

    //获取用户的登录信息
    private UserModel getUserModel() {
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_USER_LOGIN");
        if(isLogin == null || !isLogin.booleanValue()){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");

        //若获取的对应用户信息不存在
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        return userModel;
    }

    //调用小程序api获取用户openid
    private String getOpenid(String code){
        //api url地址
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=wx5d1fc2a848fa0ab0&secret=e061197dabdba85d226b4da55ef5623f&js_code=JSCODE&grant_type=authorization_code";
        url=url.replaceAll("JSCODE", code);
        //post请求
        HttpMethod method =HttpMethod.GET;
        // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        //发送http请求并返回结果
        JSONObject object = JSONObject.parseObject(httpClientService.client(url,method,params));
        Integer errcode = object.getInteger("errcode");
        String errmsg = object.getString("errmsg");
        if(errcode != 0 ) {
            throw new BusinessException(EmBusinessError.API_ERROR.setErrMsg(errmsg));
        }
        return object.getString("openid");
    }

    private UserVO convertFromModel(UserModel userModel) {
        if(userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }
}
