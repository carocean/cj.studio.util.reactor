package cj.studio.util.reactor;

import java.util.HashMap;
import java.util.Map;

public class Event {
	String key;// 选择器要用的selectionKey
	String cmd;
	Map<String, Object> parameters;

	public Event() {
		parameters = new HashMap<String, Object>();
	}
	
	public Event(String key, String cmd) {
		this();
		this.key = key;
		this.cmd = cmd;
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
