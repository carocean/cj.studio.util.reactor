package cj.studio.util.reactor;

public interface ISelectionKey {

	IPipeline pipeline();

	String key();


	void addEvent(Event e);
	Event event();
	boolean isEventEmpty();
	int eventCount();

}
