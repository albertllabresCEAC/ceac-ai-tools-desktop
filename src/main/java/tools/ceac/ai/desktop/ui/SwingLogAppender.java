package tools.ceac.ai.desktop.ui;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class SwingLogAppender extends AppenderBase<ILoggingEvent> {

    private String pattern = "%d{HH:mm:ss} %-5level %logger{36} - %msg%n";
    private PatternLayout layout;

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void start() {
        layout = new PatternLayout();
        layout.setContext(getContext());
        layout.setPattern(pattern);
        layout.start();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (layout != null) {
            GuiLogPublisher.publish(layout.doLayout(eventObject));
        }
    }
}
