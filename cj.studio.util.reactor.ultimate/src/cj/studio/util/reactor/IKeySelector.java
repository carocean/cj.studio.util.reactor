package cj.studio.util.reactor;

public interface IKeySelector {

	ISelectionKey select(String key);

	int keyCount();

	void removeKey(String key);

}
