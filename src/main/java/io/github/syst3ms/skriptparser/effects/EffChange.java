package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 *  A very general effect that can change many expressions. Many expressions can only be set and/or deleted, while some can have things added to or removed from them.
 *
 * @name Change: Set/Add/Remove/Delete/Reset
 * @pattern set %~objects% to %objects%
 * @pattern %~objects% = %objects%
 * @pattern add %objects% to %~objects%
 * @pattern %~objects% += %objects%
 * @pattern remove %objects% from %~objects%
 * @pattern %~objects% -= %~objects%
 * @pattern remove (all|every) %objects% from %~objects%
 * @pattern (delete|clear) %~objects%
 * @pattern reset %~objects%
 * @since ALPHA
 * @author Syst3ms
 */
public class EffChange extends Effect {
    public static final PatternInfos<ChangeMode> PATTERNS = new PatternInfos<>(new Object[][]{
            {"set %~objects% to %objects%", ChangeMode.SET},
            {"%~objects% = %objects%", ChangeMode.SET},
            {"add %objects% to %~objects%", ChangeMode.ADD},
            {"%~objects% += %objects%", ChangeMode.ADD},
            {"remove %objects% from %~objects%", ChangeMode.REMOVE},
            {"%~objects% -= %objects%", ChangeMode.REMOVE},
            {"remove (all|every) %objects% from %~objects%", ChangeMode.REMOVE_ALL},
            {"(delete|clear) %~objects%", ChangeMode.DELETE},
            {"reset %~objects%", ChangeMode.RESET}
    });

    private Expression<?> changed;
    @Nullable
    private Expression<?> changeWith;
    private ChangeMode mode;

    static {
        Main.getMainRegistration().addEffect(EffChange.class, PATTERNS.getPatterns());
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        ChangeMode mode = PATTERNS.getInfo(matchedPattern);
        if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            changed = expressions[0];
        } else if ((matchedPattern & 1) == 1 || mode == ChangeMode.SET) {
            changed = expressions[0];
            changeWith = expressions[1];
            assignment = (matchedPattern & 1) == 1;
        } else {
            changed = expressions[1];
            changeWith = expressions[0];
        }
        this.mode = mode;
        SkriptLogger logger = parseContext.getLogger();
        String changedString = changed.toString(null, logger.isDebug());
        if (changeWith == null) {
            assert mode == ChangeMode.DELETE || mode == ChangeMode.RESET;
            return changed.acceptsChange(mode) != null;
        } else {
            Class<?> changeType = changeWith.getReturnType();
            Class<?>[] acceptance = changed.acceptsChange(mode);
            if (acceptance == null) {
                switch (mode) {
                    case SET:
                        logger.error(changedString + " cannot be set to anything");
                        break;
                    case ADD:
                        logger.error("Nothing can be added to " + changedString);
                        break;
                    case REMOVE_ALL:
                    case REMOVE:
                        logger.error("Nothing can be removed from " + changedString);
                        break;
                }
                return false;
            } else if (!ClassUtils.containsSuperclass(acceptance, changeType)) {
                boolean array = changeType.isArray();
                Type<?> type = TypeManager.getByClassExact(changeType);
                assert type != null;
                String changeTypeName = StringUtils.withIndefiniteArticle(
                    type.getPluralForms()[array ? 1 : 0],
                    array
                );
                switch (mode) {
                    case SET:
                        logger.error(changedString + " cannot be set to " + changeTypeName);
                        break;
                    case ADD:
                        logger.error(changeTypeName + " cannot be added to " + changedString);
                        break;
                    case REMOVE_ALL:
                    case REMOVE:
                        logger.error(changeTypeName + " cannot be removed from " + changedString);
                        break;
                }
                return false;
            }
        }
        return true;
    }

    private boolean assignment;

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        String changedString = changed.toString(ctx, debug);
        String changedWithString = changeWith != null ? changeWith.toString(ctx, debug) : "";
        switch (mode) {
            case SET:
                if (assignment) {
                    return String.format("%s = %s", changedString, changedWithString);
                } else {
                    return String.format("set %s to %s", changedString, changedWithString);
                }
            case ADD:
                if (assignment) {
                    return String.format("%s += %s", changedString, changedWithString);
                } else {
                    return String.format("add %s to %s", changedWithString, changedString);
                }
            case REMOVE:
                if (assignment) {
                    return String.format("%s -= %s", changedString, changedWithString);
                } else {
                    return String.format("remove %s from %s", changedWithString, changedString);
                }
            case DELETE:
            case RESET:
                return String.format("%s %s", mode.name().toLowerCase(), changedString);
            case REMOVE_ALL:
                return String.format("remove all %s from %s", changedWithString, changedString);
            default:
                assert false;
                return "!!!unknown change mode!!!";
        }
    }

    @Override
    public void execute(TriggerContext ctx) {
        if (changeWith == null) {
            changed.change(ctx, new Object[0], mode);
        } else {
            changed.change(ctx, changeWith.getValues(ctx), mode);
        }
    }
}
