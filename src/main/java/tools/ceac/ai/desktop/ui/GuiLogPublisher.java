package tools.ceac.ai.desktop.ui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class GuiLogPublisher {

    private static final List<Consumer<String>> LISTENERS = new CopyOnWriteArrayList<>();

    private GuiLogPublisher() {
    }

    public static void register(Consumer<String> consumer) {
        LISTENERS.add(consumer);
    }

    public static void unregister(Consumer<String> consumer) {
        LISTENERS.remove(consumer);
    }

    public static void publish(String message) {
        for (Consumer<String> listener : LISTENERS) {
            listener.accept(message);
        }
    }
}
