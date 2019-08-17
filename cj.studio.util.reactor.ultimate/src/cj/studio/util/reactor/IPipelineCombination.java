package cj.studio.util.reactor;

public interface IPipelineCombination {
	/**
	 * 指定的管道新建时响应该方法
	 * @param pipeline 新建的管道
	 */
	void combine(IPipeline pipeline);

	/**
	 * 指定的管道拆除时响应该方法
	 * @param pipeline 要被拆的管道
	 */
	void demolish(IPipeline pipeline);
}
