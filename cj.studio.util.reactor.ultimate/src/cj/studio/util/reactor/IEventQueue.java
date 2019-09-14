package cj.studio.util.reactor;

import cj.ultimate.IDisposable;

import java.util.List;

public interface IEventQueue extends IDisposable {
	Event selectOne();
	void addEvent(Event e);

    int count();

	void init(int capacity);

}
