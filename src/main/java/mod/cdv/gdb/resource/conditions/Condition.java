package mod.cdv.gdb.resource.conditions;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.util.function.Predicate;

public abstract class Condition<C extends Condition<C>> implements JsonSerializer<C>, JsonDeserializer<C> {
    public abstract Predicate<ConditionContext> check();
}
