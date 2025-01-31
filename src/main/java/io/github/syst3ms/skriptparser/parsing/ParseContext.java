package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;
import java.util.regex.MatchResult;

/**
 * An object that stores data about how an object was parsed.
 * By opposition to {@link MatchContext}, this object is immutable.
 */
public class ParseContext {
    private final Class<? extends TriggerContext>[] currentContexts;
    private final PatternElement element;
    private final String expressionString;
    private final List<MatchResult> matches;
    private final int parseMark;
    private final SkriptLogger logger;

    public ParseContext(Class<? extends TriggerContext>[] currentContexts, PatternElement element, List<MatchResult> matches, int parseMark, String expressionString, SkriptLogger logger) {
        this.currentContexts = currentContexts;
        this.element = element;
        this.expressionString = expressionString;
        this.matches = matches;
        this.parseMark = parseMark;
        this.logger = logger;
    }

    /**
     * @return all the regex that were matched
     */
    public List<MatchResult> getMatches() {
        return matches;
    }

    /**
     * @return the {@link PatternElement} that was successfully matched
     */
    public PatternElement getElement() {
        return element;
    }

    /**
     * @return the parse mark
     */
    public int getParseMark() {
        return parseMark;
    }

    /**
     * @return {@linkplain #getElement() the pattern element} in string form
     */
    public String getExpressionString() {
        return expressionString;
    }

    public Class<? extends TriggerContext>[] getCurrentContexts() {
        return currentContexts;
    }

    public SkriptLogger getLogger() {
        return logger;
    }
}
