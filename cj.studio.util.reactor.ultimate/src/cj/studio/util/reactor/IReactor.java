package cj.studio.util.reactor;

public interface IReactor extends IServiceProvider{

	void input(Event e);

	/**
	 * 注意：执行该方法可能会丢事件
	 */
	void close();

	boolean isOpened();

	void removeKey(String key);
	
	int pipelineCount();
	int queueCount();
}
