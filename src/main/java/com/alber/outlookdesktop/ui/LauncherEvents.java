package com.alber.outlookdesktop.ui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class LauncherEvents {

    private static final List<Consumer<AppRuntimeState>> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile AppRuntimeState state = new AppRuntimeState("Stopped", null);

    private LauncherEvents() {
    }

    public static void register(Consumer<AppRuntimeState> consumer) {
        LISTENERS.add(consumer);
        consumer.accept(state);
    }

    public static void unregister(Consumer<AppRuntimeState> consumer) {
        LISTENERS.remove(consumer);
    }

    public static void publish(AppRuntimeState newState) {
        state = newState;
        for (Consumer<AppRuntimeState> listener : LISTENERS) {
            listener.accept(newState);
        }
    }
}
