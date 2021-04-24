package com.coder.lee.postgresmarkdown.datasource;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Description: DynamicRefreshProxy
 * Copyright: Copyright (c)
 * Company: Ruijie Co., Ltd.
 * Create Time: 2021/4/20 1:33
 *
 * @author coderLee23
 */
public class DynamicRefreshProxy<T> implements InvocationHandler {

    private final AtomicReference<T> atomicReference;


    public DynamicRefreshProxy(T instance) {
        atomicReference = new AtomicReference<>(instance);
    }

    public static <T> T newInstance(T obj) {
        return (T) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new DynamicRefreshProxy<>(obj));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return ReflectionUtils.invokeMethod(method, atomicReference.get(), args);
    }

    public static void main(String[] args) {
        //1. 创建 dataSource 代理对象
        //2. 配置刷新之后修改 DynamicRefreshProxy 中的 atomicReference 的引用值
        //3. 修改完之后,关闭关闭旧对象相关的资源
    }
}