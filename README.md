
# reactor

## 多路复用器
原理：对于一个管道，永远处在同一个线程之下，而多个管道在不同时段可共用同一线程。因此为多路复用

- 提升高并发处理性能
- 适合并发余额等的更新，保证一致性.每个pipeline下按序执行,不同pipeline之间并发执行
- 可用于并发余额的列新，将一批并发余额指标按key归并，而后一个key对应一个pipeline

## 原理

* 目的：Set集群化，同一个关键字最终总是落在同一台R上
* 缺点：一致性哈希在SET化中的缺陷是：当动态增加节点时同一个key并不一定再会落向先前的同一节点R上，所以组成的网络仍属于静态网络，而且每个C向后的节点R数必须相同。但可以通过结合数据库事务用上动态网络，分析如下：如果添加节点的此时又过来一个先前的key，而先前的节点还未处理完这个key，仅在此时才会导致事务争用问题，其它时段事务不会有争用现象，所以事务可以一直采用三级锁。
* 常见的两种网络：
- 消费网络（mq），后继节点知道它想要什么消息
- 路由网络（lb)，当前节点知道它想分给谁什么消息
 本方案采用了路由网络。
 
## 用法

```java
package cj.studio.util.reactor.test;

import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.net.CircuitException;
import cj.studio.util.reactor.*;

public class TestMain {

    public static void main(String[] args) {
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
        IReactor reactor = Reactor.open(DefaultReactor.class, 10, 1000, combin, new IServiceProvider() {

            @Override
            public <T> ServiceCollection<T> getServices(Class<T> clazz) {
                return new ServiceCollection<>();
            }

            @Override
            public <T> T getService(String name) {
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
        for (int i = 0; i < 20; i++) {
            Event e = new Event(keys[i % keyCount], "doMain_" + i);
            reactor.input(e);
        }
//		reactor.removeKey(keys[0]);
        System.out.println("-----完");
//		reactor.close();

        //以下是集群演示，可在IValve中实现远程连接、数据发送
        combin = new IPipelineCombination() {
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
				System.out.println(String.format("管道拆除，请断开连接：%s %s", node.getKey() , node.getHost()));
			}
		};
        reactor = Reactor.open(DefaultClusterRactor.class, 10, 1000, combin);

        IRemoteServiceNodeRouter manager = reactor.getService("$.remote.nodes.router");
        manager.init(12);
        manager.addNode(new RemoteServiceNode("microService1", "http://localhost:8080/website/"));
        manager.addNode(new RemoteServiceNode("microService2", "http://localhost:9090/website/"));
        for (int i = 0; i < 1000; i++) {
            Event e = new Event(keys[i % keyCount], "doMain_" + i);
            reactor.input(e);
        }
//        reactor.close();
    }

}

```
