package cj.studio.util.reactor;

import cj.studio.ecm.net.CircuitException;

public interface IValve {
	void flow(Event e,IPipeline pipeline)throws CircuitException;

    void nextError(Event e,Throwable error,  IPipeline pipeline)throws CircuitException;
}
