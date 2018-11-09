package com.taobao.lambda;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/5 下午8:12
 */
public class Parallel {

    @Test
    public void test() {
        int n = 100000;
        double fraction = 1.0 / n;
        System.out.println(IntStream.range(0, n).parallel().mapToObj(twoDiceThrows()).collect(Collectors.groupingBy(side -> side, Collectors.summingDouble(i -> fraction))));
    }

    private IntFunction<Integer> twoDiceThrows() {
        return (i) -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int first = random.nextInt(1, 7);
            int second = random.nextInt(1, 7);
            return first + second;
        };

    }

    @Test
    public void sumOfSquares() {
        System.out.println(IntStream.range(1, 10).map(x -> x * x).sum());
        System.out.println(IntStream.range(1, 10).parallel().map(x -> x * x).sum());
    }

    @Test
    public void multiply() {
       System.out.println(IntStream.range(1, 10).reduce(5, (acc, x) -> x * acc));

        System.out.println(IntStream.range(1, 10).parallel().reduce((acc, x) -> x * acc).getAsInt()*5);
    }
}
