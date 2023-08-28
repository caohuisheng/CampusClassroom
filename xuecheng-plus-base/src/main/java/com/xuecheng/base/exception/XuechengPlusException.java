package com.xuecheng.base.exception;


/**
 * 学成在线异常类
 */
public class XuechengPlusException extends RuntimeException {

    private String errMessage;

    public XuechengPlusException() {
        super();
    }

    public XuechengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    /**
     * 抛出自定义异常信息
     * @param commonError
     */
    public static void cast(CommonError commonError){
        throw new XuechengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XuechengPlusException(errMessage);
    }

}
