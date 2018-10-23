package com.taobao;

import org.junit.Test;

import java.io.File;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/19 下午3:44
 */
public class FiePathTest {

    @Test
    public void test() {
        getPath();
    }

    public static void getPath() {
        //方式一
        System.out.println(System.getProperty("user.dir"));
        //方式二
        File directory = new File("");//设定为当前文件夹
        try {
            System.out.println(directory.getCanonicalPath());//获取标准的路径
            System.out.println(directory.getAbsolutePath());//获取绝对路径
        } catch (Exception e) {
            e.printStackTrace();
        }
        //方式三
        //这个要注意
        System.out.println(FiePathTest.class.getResource("/"));
        System.out.println(FiePathTest.class.getResource(""));
        //方式4
        //这个要注意
        System.out.println(FiePathTest.class.getClassLoader().getResource(""));

        System.out.println(FiePathTest.class.getClassLoader().getResource("source.xml"));
    }
}
