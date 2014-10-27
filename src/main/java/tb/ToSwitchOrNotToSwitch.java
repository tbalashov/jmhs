package tb;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;
import static org.openjdk.jmh.annotations.Mode.*;
import static org.openjdk.jmh.annotations.Scope.*;

@BenchmarkMode(AverageTime)
@OutputTimeUnit(NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Thread)
public class ToSwitchOrNotToSwitch {

    @Param({"1"})
    private int x;

    private HashMap<Integer, Integer> hashMap;
    private Map<Integer, Integer> map;
    private Hashtable<Integer, Integer> hashtable;
    private IntIntMap hppcInts;
    private int[] intArr;

    @Setup
    public void setup() {
        hashMap = new HashMap<>();
        hashMap.put(0, 0);
        hashMap.put(1, 1);
        hashMap.put(2, 0);
        hashMap.put(3, -1);

        map = hashMap;

        hashtable = new Hashtable<>();
        hashtable.put(0, 0);
        hashtable.put(1, 1);
        hashtable.put(2, 0);
        hashtable.put(3, -1);

        hppcInts = new IntIntOpenHashMap();
        hppcInts.put(0, 0);
        hppcInts.put(1, 1);
        hppcInts.put(2, 0);
        hppcInts.put(3, -1);

        intArr = new int[]{0, 1, 0, -1};
    }

    @Benchmark
    public int _switch() {
        switch (x) {
            case 1:
                return 1;
            case 3:
                return -1;
            default:
                return 0;
        }
    }

    @Benchmark
    public int _if() {
        return (x == 1) ? 1
                : (x == 3) ? -1
                : 0;
    }

    @Benchmark
    public int map() {
        return map.get(x);
    }

    @Benchmark
    public int hashMap() {
        return hashMap.get(x);
    }

    @Benchmark
    public int hashtable() {
        return hashtable.get(x);
    }

    @Benchmark
    public int hppc() {
        return hppcInts.get(x);
    }

    @Benchmark
    public int arr() {
        return intArr[x];
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ToSwitchOrNotToSwitch.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
