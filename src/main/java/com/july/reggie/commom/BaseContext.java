package com.july.reggie.commom;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的id
 *
 * @author july
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
