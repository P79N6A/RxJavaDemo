package com.taobao;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/15 下午7:12
 */
public class StreamTest {

    @Test
    public void flatMap() {
        Stream.of(Arrays.asList(1, 2), Arrays.asList(3, 4)).flatMap(numbers -> numbers.stream()).forEach(System.out::println);
    }

    @Test
    public void min() {
        int min = Arrays.asList(1, 2, 3, 4).stream().min(Comparator.comparing(integer -> integer)).get();
        System.out.println(min);

        //reduce实现
        min = Arrays.asList(-1, -2, 1, 2, 3, 4).stream().reduce(Integer.MAX_VALUE, (last, element) -> element > last ? last : element).intValue();
        System.out.println(min);
    }

    @Test
    public void max() {
        int max = Arrays.asList(1, 2, 3, 4).stream().max(Comparator.comparing(integer -> integer)).get();
        System.out.println(max);
    }

    @Test
    public void reduce() {
        int sum = Arrays.asList(1, 2, 3).stream().reduce((acc, element) -> acc + element).get();
        System.out.println(sum);

        sum = Arrays.asList(1, 2, 3).stream().reduce(1, (acc, element) -> acc + element).intValue();
        System.out.println(sum);
    }
}
