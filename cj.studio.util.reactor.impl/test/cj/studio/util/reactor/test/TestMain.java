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
