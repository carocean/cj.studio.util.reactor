package cj.studio.util.reactor;

import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IDisposable;

public interface IPipeline {
    String key();

    /**
     * 管道的附件，也可能空
     * @return
     */
    Object attachment();
    void append(IValve valve);

    void input(Event e) throws CircuitException;

    void nextFlow(Event e, IValve formthis) throws CircuitException;

    void nextError(Event e, Throwable error, IValve formthis) throws CircuitException;

    void remove(IValve valve);

    IServiceProvider site();

    void error(Event event, Throwable e) throws CircuitException;

    /**
     * 在管道运行完毕之后是否要求拆除当前管道
     * @return
     */
    boolean isDemandDemolish();

    /**
     * 设置在管道运行完毕之后拆除当前管道
     * @param isDemandDemolish
     */
    void setDemandDemolish(boolean isDemandDemolish);
}
