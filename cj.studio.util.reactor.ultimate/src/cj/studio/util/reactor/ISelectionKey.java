package cj.studio.util.reactor;

import cj.ultimate.IDisposable;

public interface ISelectionKey {
    IPipeline pipeline();

    String key();


    Object attachment();
}
