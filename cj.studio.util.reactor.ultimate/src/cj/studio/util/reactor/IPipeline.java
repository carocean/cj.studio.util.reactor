package cj.studio.util.reactor;

import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IDisposable;

public interface IPipeline  {
String key();
	void append(IValve valve);

	void input(Event e) throws CircuitException;

	void nextFlow(Event e, IValve formthis)throws CircuitException;
	void nextError(Event e,Throwable error,  IValve formthis)throws CircuitException;
	void remove(IValve valve);
	IServiceProvider site();

	void error(Event event,Throwable e) throws CircuitException;
}
