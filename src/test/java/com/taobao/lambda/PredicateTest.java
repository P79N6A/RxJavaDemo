package com.taobao.lambda;

import org.junit.Test;

import java.util.function.Predicate;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/11 下午8:46
 */
public class PredicateTest {

    @Test
    public void test() {
        Predicate<Integer> atLeast5 = (x) -> x > 5;

        System.out.println(atLeast5.test(2));
    }

    @Test
    public void and() {
        Predicate<Integer> atLeast5 = (x) -> x > 5;
        System.out.println(atLeast5.and((x) -> x > 2).test(4));
    }
}
