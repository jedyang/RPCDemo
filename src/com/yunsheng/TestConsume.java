package com.yunsheng;

import java.io.IOException;

/**
 * Created by shengyun on 17/4/27.
 */
public class TestConsume {
    public static void main(String[] args){
        // 消费RPC服务
        try {
            TestService testService = RpcServiceManager.consume(TestService.class, "localhost", 1234);
            System.out.println(testService.hello());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
