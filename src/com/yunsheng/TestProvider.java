package com.yunsheng;

import java.io.IOException;

/**
 * Created by shengyun on 17/4/27.
 */
public class TestProvider {

    public static void main(String[] args){
        // 提供RPC服务
        TestService testService = new TestServiceImpl();
        try {
            RpcServiceManager.produce(testService, 1234);
            System.out.println("服务已启动");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
