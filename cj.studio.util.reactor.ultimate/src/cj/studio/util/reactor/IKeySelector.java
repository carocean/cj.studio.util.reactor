package cj.studio.util.reactor;

import cj.ultimate.IDisposable;

public interface IKeySelector extends IDisposable {

	ISelectionKey select(String key,Object attachment);

	int keyCount();

	void removeKey(String key);

}
