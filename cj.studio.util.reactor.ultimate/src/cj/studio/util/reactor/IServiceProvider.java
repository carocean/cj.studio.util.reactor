package cj.studio.util.reactor;

import cj.studio.ecm.ServiceCollection;

public interface IServiceProvider {
	<T> T getService(String name);
	<T> ServiceCollection<T> getServices(T clazz);
}
