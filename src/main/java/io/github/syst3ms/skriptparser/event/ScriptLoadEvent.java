package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

public class ScriptLoadEvent extends SkriptEvent {

    static {
        Main.getMainRegistration()
            .newEvent(ScriptLoadEvent.class, "script load[ing]")
            .setHandledContexts(ScriptLoadContext.class)
            .register();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof ScriptLoadContext;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "script loading";
    }
}
