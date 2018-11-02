package com.taobao.lambda;

import org.junit.Test;

import java.util.*;
import java.util.function.*;
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
        System.out.println(Stream.of(1, 2, 3, 4, 5).collect(Collectors.groupingBy(i -> i < 3, Collectors.counting())));


    }

    @Test
    public void reduce() {

        StringCombiner reduce = Arrays.asList("A", "B", "C").stream().reduce(new StringCombiner(","), StringCombiner::add, StringCombiner::merge);

        System.out.println(reduce.toString());

        System.out.println(Arrays.asList("A", "B", "C").stream().collect(new StringCollector()));

    }

    @Test
    public void reduce3() {
        ArrayList<Integer> accResult_ = Stream.of(1, 2, 3, 4)
                .reduce(new ArrayList<Integer>(),
                        new BiFunction<ArrayList<Integer>, Integer, ArrayList<Integer>>() {
                            @Override
                            public ArrayList<Integer> apply(ArrayList<Integer> acc, Integer item) {

                                acc.add(item);
                                System.out.println("item: " + item);
                                System.out.println("acc+ : " + acc);
                                System.out.println("BiFunction");
                                return acc;
                            }
                        }, new BinaryOperator<ArrayList<Integer>>() {
                            @Override
                            public ArrayList<Integer> apply(ArrayList<Integer> acc, ArrayList<Integer> item) {
                                //Stream是支持并发操作的，为了避免竞争，对于reduce线程都会有独立的result，combiner的作用在于合并每个线程的result得到最终结果
                                System.out.println("BinaryOperator");
                                acc.addAll(item);
                                System.out.println("item: " + item);
                                System.out.println("acc+ : " + acc);
                                System.out.println("--------");
                                return acc;
                            }
                        });
        System.out.println("accResult_: " + accResult_);
    }
}
