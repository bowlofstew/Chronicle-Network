package net.openhft.chronicle.network2.event;

import net.openhft.lang.thread.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Event "Loop" for blocking tasks. Created by peter on 26/01/15.
 */
public class BlockingEventLoop implements EventLoop {
    private final EventLoop parent;
    private final ExecutorService service;

    public BlockingEventLoop(EventLoop parent, String name) {
        this.parent = parent;
        service = Executors.newCachedThreadPool(new NamedThreadFactory(name, true));
    }

    @Override
    public void addHandler(EventHandler handler) {
        service.submit(() -> {
            handler.eventLoop(parent);
            while (!handler.isDead())
                handler.runOnce();
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        service.shutdown();
    }
}
