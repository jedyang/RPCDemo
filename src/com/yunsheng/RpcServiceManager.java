package com.yunsheng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shengyun on 17/4/27.
 * 使用动态代理和反射调用，简单模拟RPC服务框架
 *
 * DUBBO和HSF要复杂的多。
 * 服务注册，路由寻址
 * 服务治理
 * 序列化协议
 *
 */
public class RpcServiceManager {

    public static void produce(final Object service, int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            final Socket socket = serverSocket.accept();

            ExecutorService pool = Executors.newCachedThreadPool();

            pool.execute(new Runnable() {
                public void run() {
                    try {
                        try {
                            ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());
                            try {
                                // 获取入参，反射调用
                                String methodName = objInput.readUTF();
                                Class<?>[] parameterTypes = (Class<?>[])objInput.readObject();
                                Object[] parameters = (Object[])objInput.readObject();

                                Method method = service.getClass().getMethod(methodName, parameterTypes);
                                Object result = method.invoke(service, parameters);

                                ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
                                try {
                                    objOutput.writeObject(result);
                                } finally {
                                    objOutput.close();
                                }
                            } finally {
                                objInput.close();
                            }
                        } finally {
                            socket.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
        }
    }

    public static <T> T consume(final Class<T> interfaceClass, final String host, final int port) throws IOException {

        // 动态代理
        Object proxyInstance = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] {interfaceClass},
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Socket socket = new Socket(host, port);
                    try {
                        // 消费者的出对接着服务提供者的入
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        try {
                            objectOutputStream.writeUTF(method.getName());
                            objectOutputStream.writeObject(method.getParameterTypes());
                            objectOutputStream.writeObject(method.getParameters());

                            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                            try {
                                Object result = objectInputStream.readObject();
                                if (result instanceof Throwable) {
                                    throw (Throwable)result;
                                }
                                return result;

                            } finally {
                                objectInputStream.close();
                            }
                        } finally {
                            objectOutputStream.close();
                        }
                    } finally {
                        socket.close();
                    }
                }
            });

        return (T)proxyInstance;

    }
}
