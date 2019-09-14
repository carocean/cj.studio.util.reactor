
# reactor

## 多路复用器
原理：对于一个管道，永远处在同一个线程之下，而多个管道在不同时段可共用同一线程。因此为多路复用

- 提升高并发处理性能
- 适合并发余额等的更新，保证一致性.每个pipeline下按序执行,不同pipeline之间并发执行
- 可用于并发余额的列新，将一批并发余额指标按key归并，而后一个key对应一个pipeline

## 原理

* 目的：Set集群化，同一个关键字最终总是落在同一台R上
* 常见的两种网络：
- 消费网络（mq），后继节点知道它想要什么消息
- 路由网络（lb)，当前节点知道它想分给谁什么消息
 本方案采用了路由网络。
## 功能
- 支持内存队列
- 支持磁盘队列
- 支持一致性哈希负载，运行时动态改变节点数
- 支持在一致性哈希负载的基础上使后续同一key的请求定向，从而达到同一个关键字最终总是落在同一台R上

## 用法

```java
package cj.studio.util.reactor.test;

import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.net.CircuitException;
import cj.studio.util.reactor.*;

public class TestMain {

    public static void main(String[] args) throws InterruptedException {
        testDefault();
//  testCluster();
    }

    private static void testDefault() throws InterruptedException {
        IPipelineCombination combin = new IPipelineCombination() {

            @Override
            public void combine(IPipeline pipeline) {
                IValve valve = new IValve() {
                    @Override
                    public void nextError(Event e, Throwable error, IPipeline pipeline) throws CircuitException {
                        System.out.println("----出错:" + pipeline.key() + " " + error);
                    }

                    @Override
                    public void flow(Event e, IPipeline pipeline) throws CircuitException {
                        ISelectionKey find=pipeline.site().getService("$.key.bank_0");
                        System.out.println("----进入线程:" + pipeline.key() + " " + Thread.currentThread().getId() + " "
                                + e.getCmd() + " " + this.hashCode());
                        switch (e.getCmd()) {
                            case "deposit":
                                break;
                            case "cashout":
                                break;
                        }
                        ServiceCollection<Object> col = pipeline.site().getServices(Object.class);
                        pipeline.nextFlow(e, this);
                        System.out.println("----退出线程:" + pipeline.key() + " " + Thread.currentThread().getId() + " "
                                + e.getCmd() + " " + this.hashCode() + " " + col.hashCode());
//						throw new CircuitException("404","xxxxx");
//						throw new RuntimeException("xxxxx");
                    }

                };
                pipeline.append(valve);
            }

            @Override
            public void demolish(IPipeline pipeline) {
                System.out.println(String.format("管道拆除：%s ", pipeline.key()));
            }
        };
        IReactor reactor = Reactor.open(DefaultReactor.class, 10, 2*1024*1024, combin, new IServiceProvider() {

            @Override
            public <T> ServiceCollection<T> getServices(Class<T> clazz) {
                return new ServiceCollection<>();
            }

            @Override
            public <T> T getService(String name) {
                if("$.reactor.queue".equals(name)){
                    return (T) new DiskStreamEventQueue("/Users/cj/studio/cj.studio.util.reactor/cj.studio.util.reactor.impl/data/");
                }
                return (T) name;
            }
        });
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        int keyCount = 10;// pipeline数,每个pipeline下按序执行,不同pipeline之间并发执行
        String[] keys = new String[keyCount];
        for (int i = 0; i < keyCount; i++) {
            keys[i] = "bank_" + i;
        }
        for (int i = 0; i < 20000000; i++) {
            Event e = new Event(keys[i % keyCount], "doMain_" + i);
            reactor.input(e);
            Thread.sleep(10);
        }
//		reactor.removeKey(keys[0]);
        System.out.println("-----完");
//		reactor.close();

    }

    private static void testCluster() {
        //以下是集群演示，可在IValve中实现远程连接、数据发送
        IPipelineCombination combin = new IPipelineCombination() {
            @Override
            public void combine(IPipeline pipeline) {
                //新管道建立，进行连接
                RemoteServiceNode node = (RemoteServiceNode) pipeline.attachment();
                System.out.println(String.format("新管道建立，请进行连接：%s ", node.getKey() + " " + node.getHost()));
                pipeline.append(new IValve() {
                    @Override
                    public void flow(Event e, IPipeline pipeline) throws CircuitException {
                        RemoteServiceNode node = (RemoteServiceNode) pipeline.attachment();
                        System.out.println(String.format("将请求：%s 分发到目标：%s ", e.getKey(), node.getKey()));
//                        pipeline.setDemandDemolish(true);
                    }

                    @Override
                    public void nextError(Event e, Throwable error, IPipeline pipeline) throws CircuitException {
                        RemoteServiceNode node = (RemoteServiceNode) pipeline.attachment();
                        System.out.println(String.format("将请求：%s 分发到目标：%s 但出错了，原因：%s", e.getKey(), node.getKey(), error));
                    }
                });
            }

            @Override
            public void demolish(IPipeline pipeline) {
                RemoteServiceNode node = (RemoteServiceNode) pipeline.attachment();
                System.out.println(String.format("管道拆除，请断开连接：%s %s", node.getKey(), node.getHost()));
            }
        };
        IReactor reactor = Reactor.open(DefaultClusterRactor.class, 10, 2*1024*1024, combin,new IServiceProvider() {

            @Override
            public <T> ServiceCollection<T> getServices(Class<T> clazz) {
                return new ServiceCollection<>();
            }

            @Override
            public <T> T getService(String name) {
                if("$.reactor.queue".equals(name)){//使用磁盘流队列
                    return (T) new DiskStreamEventQueue("/Users/cj/studio/cj.studio.util.reactor/cj.studio.util.reactor.impl/data/");
                }
                if("$.reactor.orientor".equals(name)){//使用定向器，以记忆路由
                    return (T) new DefaultOrientor("/Users/cj/studio/cj.studio.util.reactor/cj.studio.util.reactor.impl/data/orientor.db");
                }
                return (T) name;
            }
        });

        IRemoteServiceNodeRouter manager = reactor.getService("$.remote.nodes.router");
        manager.init(12);
        manager.addNode(new RemoteServiceNode("microService1", "http://localhost:8080/website/"));
        manager.addNode(new RemoteServiceNode("microService2", "http://localhost:9090/website/"));
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        int keyCount = 10;// pipeline数,每个pipeline下按序执行,不同pipeline之间并发执行
        String[] keys = new String[keyCount];
        for (int i = 0; i < keyCount; i++) {
            keys[i] = "bank_" + i;
        }

        for (int i = 0; i < 1000; i++) {
            Event e = new Event(keys[i % keyCount], "doMain_" + i);
            reactor.input(e);
        }
//        reactor.close();
    }

}


```
