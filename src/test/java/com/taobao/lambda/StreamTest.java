package com.taobao.lambda;

import org.junit.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/24 下午8:00
 */
public class StreamTest {

    @Test
    public void test() {

        List<String> ls = Arrays.asList("Hello", "World");

        IntSummaryStatistics statistics = ls.stream().sorted(Comparator.comparing(s -> s)).mapToInt(String::length).summaryStatistics();

        System.out.println(statistics);
    }

    @Test
    public void collect() {
        //Collectors 里有很多实用方法
        List<String> ls = Arrays.asList("Hello", "World");
        TreeSet<String> set = ls.stream().collect(Collectors.toCollection(TreeSet::new));

        //数据分块
        Map<Boolean, List<Integer>> map = Stream.of(1, 2, 3, 4, 5).collect(Collectors.partitioningBy(i -> i % 2 == 0));
        System.out.println(map);

        //数据分组
        System.out.println(Stream.of(1, 2, 3, 4, 5).collect(Collectors.groupingBy(i -> i < 3,Collectors.counting())));


    }
}
