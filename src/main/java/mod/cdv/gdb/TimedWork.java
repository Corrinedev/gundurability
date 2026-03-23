package mod.cdv.gdb;

import java.util.function.Consumer;

public class TimedWork<T> {
    public long startTimestamp;
    final long timeMS;
    final Consumer<T> action;

    public TimedWork(long timeMS, Consumer<T> action) {
        this.timeMS = timeMS;
        this.action = action;
        startTimestamp = System.currentTimeMillis();
    }

    public void start() {
        startTimestamp = System.currentTimeMillis();
    }

    public boolean poll(T actor) {
        if(System.currentTimeMillis() - startTimestamp > timeMS) {
            action.accept(actor);
            return true;
        }
        return false;
    }
}
