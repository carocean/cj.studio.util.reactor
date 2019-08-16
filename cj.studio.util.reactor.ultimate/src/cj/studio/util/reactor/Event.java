package cj.studio.util.reactor;

import java.util.HashMap;
import java.util.Map;

public class Event {
	String key;// 选择器要用的selectionKey
	String cmd;
	boolean mustCancelKey;//是否必须撤消key，如果必须撤消则每次执行完就释放key对应的管道
	Map<String, Object> parameters;

	public Event() {
		parameters = new HashMap<String, Object>();
	}
	/**
	 * 默认在处理完事件后不关闭管道
	 * @param key 并发关键字
	 * @param cmd 命令
	 */
	public Event(String key, String cmd) {
		this();
		this.key = key;
		this.cmd = cmd;
	}

	/**
	 * 指定在处理完事件后是否必须关闭管道
	 * @param key 并发关键字
	 * @param cmd 命令
	 * @param mustCancelKey 是否必须撤消key，如果必须撤消则每次执行完就释放key对应的管道
	 */
	public Event(String key, String cmd,boolean mustCancelKey) {
		this(key,cmd);
		this.mustCancelKey=mustCancelKey;
	}
	public boolean isMustCancelKey() {
		return mustCancelKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

}
