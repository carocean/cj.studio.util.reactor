package cj.studio.util.reactor;

public interface IServiceProvider {
	<T> T getService(String name);
}
