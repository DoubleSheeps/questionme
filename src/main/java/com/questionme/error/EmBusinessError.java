package com.questionme.error;

public enum EmBusinessError implements CommonError {
    //1000开头为通用错误类型
    UNKNOWN_ERROR(10000,"未知错误"),
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    API_ERROR(10002,"小程序API调用出错"),


    //2000开头为用户信息相关错误类型
    USER_NOT_EXIST(20000,"用户不存在"),
    USER_NOT_LOGIN(20001,"用户未登录或登录失效，请登录"),
    FOLLOW_IS_EMPTY(20002,"关注列表为空"),
    FANS_IS_EMPTY(20003,"粉丝列表为空"),
    FOLLOW_ERROR(20004,"用户关注信息有误")

    //3000开头为用户信息相关错误类型

    ;

    private EmBusinessError(int errCode,String errMsg){
        this.errCode=errCode;
        this.errMsg=errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    //修改自身错误信息
    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg=errMsg;
        return this;
    }
}

