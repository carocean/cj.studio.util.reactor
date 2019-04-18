package cj.studio.util.reactor;

public interface IValve {
	void flow(Event e,IPipeline pipeline);
}
