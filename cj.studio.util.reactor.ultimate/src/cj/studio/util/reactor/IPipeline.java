package cj.studio.util.reactor;

public interface IPipeline {
String key();
	void append(IValve valve);

	void input(Event e);

	void nextFlow(Event e, IValve formthis);

	void remove(IValve valve);

}
