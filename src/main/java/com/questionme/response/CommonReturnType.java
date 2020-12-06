package com.questionme.response;

public class CommonReturnType {
    /**
     * 表明对应请求的处理结果是"success"或者"fail"
     * 若status=success,则返回前端需要的json数据
     * 若status=fail,则data内使用通用的错误码格式
     */
    private String status;

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";

    private Object data;

    public static CommonReturnType create(String status,Object result){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public static CommonReturnType create(Object result){
        return CommonReturnType.create(CommonReturnType.STATUS_SUCCESS,result);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
