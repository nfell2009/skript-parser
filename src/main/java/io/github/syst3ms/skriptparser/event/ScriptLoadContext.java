package io.github.syst3ms.skriptparser.event;

/**
 * The context representing the main entry point in scripts,
 * equivalent to {@code public static void main(String[] args)} in Java
 */
public class ScriptLoadContext implements TriggerContext {
    private final String[] args;

    public ScriptLoadContext(String[] args) {
        this.args = args;
    }

    @Override
    public String getName() {
        return "main";
    }

    public String[] getArguments() {
        return args;
    }
}
