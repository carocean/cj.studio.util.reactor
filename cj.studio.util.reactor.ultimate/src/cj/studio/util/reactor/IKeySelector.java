package cj.studio.util.reactor;

public interface IKeySelector {

	ISelectionKey select();

	int keyCount();

	void removeKey(String key);

}
