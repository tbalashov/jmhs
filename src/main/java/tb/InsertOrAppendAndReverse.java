package tb;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Thread;

@BenchmarkMode(AverageTime)
@OutputTimeUnit(NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Thread)
public class InsertOrAppendAndReverse {

    final static int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999,
            99999999, 999999999, Integer.MAX_VALUE};
    final static char[] digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9',};
    final static char[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };
    final static char[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    // Requires positive x
    static int stringSize(int x) {
        for (int i = 0; ; i++)
            if (x <= sizeTable[i])
                return i + 1 + i / 3;
    }

    public static String formatByArray(int number) {
        int length = stringSize(number);
        char[] chars = new char[length];

        int q, r;
        int charPos = length;

        // Generate two digits per iteration
        while (number >= 65536) {
            q = number / 100;
            // really: r = i - (q * 100);
            r = number - ((q << 6) + (q << 5) + (q << 2));
            number = q;
            chars[--charPos] = DigitOnes[r];
            if ((length - charPos) % 4 == 3) chars[--charPos] = ',';
            chars[--charPos] = DigitTens[r];
            if ((length - charPos) % 4 == 3) chars[--charPos] = ',';
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (; ; ) {
            q = (number * 52429) >>> (16 + 3);
            r = number - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            chars[--charPos] = digits[r];
            if ((length - charPos) % 4 == 3) chars[--charPos] = ',';
            number = q;
            if (number == 0) break;
        }
        return new String(chars);
    }

    public static String formatByReverseSet(int number) {
        StringBuilder sb = new StringBuilder("1234567890123");
        int l = 13;
        do {
            l--;
            int digit = number % 10;
            number = number / 10;
            sb.setCharAt(l, (char) ('0' + digit));
            if ((14 - l) % 4 == 0 && number != 0) sb.setCharAt(--l, ',');
        } while (number != 0);
        return sb.substring(l, 13);
    }

    public static String formatByInsert(int number) {
        StringBuilder sb = new StringBuilder(13);
        do {
            int digit = number % 10;
            number = number / 10;
            sb.insert(0, digit);
            if ((sb.length() + 1) % 4 == 0 && number != 0) sb.insert(0, ',');
        } while (number != 0);
        return sb.toString();
    }

    private static String formatByInsertCommas(int number) {
        StringBuilder sb = new StringBuilder(13);
        sb.append(number);
        int l = sb.length();
        for (int i = 0; i < (l - 1) / 3; i++) sb.insert(l - (i + 1) * 3, ',');
        return sb.toString();
    }

    private static String formatByAppendAndReverse(int number) {
        StringBuilder sb = new StringBuilder(13);
        do {
            int digit = number % 10;
            number = number / 10;
            sb.append(digit);
            if ((sb.length() + 1) % 4 == 0 && number != 0) sb.append(',');
        } while (number != 0);
        return sb.reverse().toString();
    }

    private static String plainFormat(int number) {
        return NumberFormat.getInstance(Locale.FRANCE).format(number);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(InsertOrAppendAndReverse.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public String array() {
        return formatByArray(1000000000);
    }

    @Benchmark
    public String inserts() {
        return formatByInsert(1000000000);
    }

    @Benchmark
    public String appends() {
        return formatByAppendAndReverse(1000000000);
    }

    @Benchmark
    public String commas() {
        return formatByInsertCommas(1000000000);
    }

    @Benchmark
    public String formatter() {
        return plainFormat(1000000000);
    }

    @Benchmark
    public String reverseSet() {
        return formatByReverseSet(1000000000);
    }

}
