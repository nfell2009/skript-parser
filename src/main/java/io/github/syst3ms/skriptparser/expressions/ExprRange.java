package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.ranges.RangeInfo;
import io.github.syst3ms.skriptparser.types.ranges.Ranges;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Returns a range of values between two endpoints. Types supported by default are integers and characters (length 1 strings).
 *
 * @name Range
 * @pattern range from %object% to %object%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprRange implements Expression<Object> {
    private Expression<?> from, to;
    private RangeInfo<?, ?> range;
    @Nullable
    private Comparator<?, ?> comparator;

    static {
        Main.getMainRegistration().addExpression(
                ExprRange.class,
                Object.class,
                false,
                "range from %object% to %object%"
        );
        Ranges.registerRange(
                Long.class,
                Long.class,
                (l, r) -> {
                    if (l.compareTo(r) >= 0) {
                        return new Long[0];
                    } else {
                        return LongStream.range(l, r + 1)
                                         .boxed()
                                         .toArray(Long[]::new);
                    }
                }
        );
        Ranges.registerRange(
                BigInteger.class,
                BigInteger.class,
                (l, r) -> {
                    if (l.compareTo(r) >= 0) {
                        return new BigInteger[0];
                    } else {
                        List<BigInteger> elements = new ArrayList<>();
                        BigInteger current = l;
                        do {
                            elements.add(current);
                            current = current.add(BigInteger.ONE);
                        } while (current.compareTo(r) <= 0);
                        return elements.toArray(new BigInteger[0]);
                    }
                }
        );
        // It's actually a character range
        Ranges.registerRange(
                String.class,
                String.class,
                (l, r) -> {
                    if (l.length() != 1 || r.length() != 1)
                        return new String[0];
                    char leftChar = l.charAt(0), rightChar = r.charAt(0);
                    return IntStream.range(leftChar, rightChar + 1)
                                    .mapToObj(i -> Character.toString((char) i))
                                    .toArray(String[]::new);
                }
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        from = expressions[0];
        to = expressions[1];
        range = Ranges.getRange(ClassUtils.getCommonSuperclass(from.getReturnType(), to.getReturnType()));
        comparator = Comparators.getComparator(from.getReturnType(), to.getReturnType());
        if (range == null) {
            SkriptLogger logger = parseContext.getLogger();
            parseContext.getLogger().error("Cannot get a range between " + from.toString(null, logger.isDebug()) + " and " + from.toString(null, logger.isDebug()));
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object f = from.getSingle(ctx);
        Object t = to.getSingle(ctx);
        if (f == null || t == null) {
            return new Object[0];
        }
        // This is safe... right ?
        if (comparator != null && ((Comparator) comparator).apply(f, t).is(Relation.GREATER)) {
            return CollectionUtils.reverseArray((Object[]) ((BiFunction) this.range.getFunction()).apply(t, f));
        } else {
            return (Object[]) ((BiFunction) this.range.getFunction()).apply(f, t);
        }
    }

    @Override
    public Class<?> getReturnType() {
        return range.getTo();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "range from " + from.toString(ctx, debug) + " to " + to.toString(ctx, debug);
    }
}
