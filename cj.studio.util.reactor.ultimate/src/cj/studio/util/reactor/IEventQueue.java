package cj.studio.util.reactor;

import java.util.List;

public interface IEventQueue {
	List<Event> select();
	Event selectOne();
	void addEvent(Event e);

    int count();
}
