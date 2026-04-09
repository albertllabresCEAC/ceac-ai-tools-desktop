package tools.ceac.ai.desktop.launcher;

public record CommandResult(
        int exitCode,
        String stdout,
        String stderr
) {
    public boolean isSuccess() {
        return exitCode == 0;
    }

    public String combinedOutput() {
        return (stdout == null ? "" : stdout) + (stderr == null ? "" : System.lineSeparator() + stderr);
    }
}
