package com.taobao.lambda;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;

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
}
