package cj.studio.util.reactor;

import cj.studio.ecm.net.CircuitException;

public interface IPipeline {
String key();
	void append(IValve valve);

	void input(Event e) throws CircuitException;

	void nextFlow(Event e, IValve formthis)throws CircuitException;

	void remove(IValve valve);
	IServiceProvider site();

}
