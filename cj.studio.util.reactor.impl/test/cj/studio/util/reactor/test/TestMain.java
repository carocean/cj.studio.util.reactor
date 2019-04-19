package cj.studio.util.reactor.test;

import cj.studio.ecm.ServiceCollection;
import cj.studio.util.reactor.DefaultReactor;
import cj.studio.util.reactor.Event;
import cj.studio.util.reactor.IPipeline;
import cj.studio.util.reactor.IPipelineCombination;
import cj.studio.util.reactor.IReactor;
import cj.studio.util.reactor.IServiceProvider;
import cj.studio.util.reactor.IValve;
import cj.studio.util.reactor.Reactor;

public class TestMain {

	public static void main(String[] args) {
		IPipelineCombination combin = new IPipelineCombination() {

			@Override
			public void combine(IPipeline pipeline) {
				IValve valve = new IValve() {

					@Override
					public void flow(Event e, IPipeline pipeline) {
						System.out.println("----进入线程:" + pipeline.key() + " " + Thread.currentThread().getId() + " "
								+ e.getCmd() + " " + this.hashCode());
						switch (e.getCmd()) {
						case "deposit":
							break;
						case "cashout":
							break;
						}
						ServiceCollection<Object> col=pipeline.site().getServices(Object.class);
						pipeline.nextFlow(e, this);
						System.out.println("----退出线程:" + pipeline.key() + " " + Thread.currentThread().getId() + " "
								+ e.getCmd() + " " + this.hashCode()+" "+col.hashCode());
					}

				};
				pipeline.append(valve);
			}

		};
		IReactor reactor = Reactor.open(DefaultReactor.class, 10, 1000, combin, new IServiceProvider() {
			
			@Override
			public <T> ServiceCollection<T> getServices(Class<T> clazz) {
				return new ServiceCollection<>();
			}
			
			@Override
			public <T> T getService(String name) {
				return (T)name;
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
		for (int i = 0; i < 100; i++) {
			Event e = new Event(keys[i % keyCount], "doMain_" + i);
			reactor.input(e);
		}
//		reactor.removeKey(keys[0]);
		System.out.println("-----完");
//		reactor.close();
	}

}
