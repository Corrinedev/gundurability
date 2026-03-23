package mod.cdv.gdb;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConditionalTimedWork<T> extends TimedWork<T> {
    private final Predicate<T> condition;
    public ConditionalTimedWork(long timeMS, Consumer<T> action, Predicate<T> condition) {
        super(timeMS, action);
        this.condition = condition;
    }

    public boolean poll(T actor) {
        if(condition.test(actor)) {
            if (System.currentTimeMillis() - startTimestamp > timeMS) {
                action.accept(actor);
                return true;
            }
        }
        return false;
    }
}
