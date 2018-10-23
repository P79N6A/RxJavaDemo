package com.taobao;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/28 下午6:53
 */
public class FlowableTest {

    @Test
    public void test() throws InterruptedException {
        Flowable<Integer> f1 = Flowable.just(1, 2, 3, 4).delay(40, TimeUnit.MILLISECONDS, Schedulers.io());
        Flowable<Integer> f2 = Flowable.just(9, 5, 6, 7).delay(60, TimeUnit.MILLISECONDS, Schedulers.io());

        List<Flowable<Integer>> flowableList = new ArrayList<>();
        flowableList.add(f1);
        flowableList.add(f2);

        Flowable<Flowable<Integer>> f3 = Flowable.fromIterable(flowableList);
        Flowable<Integer> f4 = f3.flatMap(flowable -> flowable);
        Flowable<List<Integer>> f5 = f4.toList().toFlowable();
        f5.blockingSubscribe(results -> {
            results.forEach(result -> {
                System.out.println(Thread.currentThread().getName() + "\t" + result);
            });
        });

    }

    @Test
    public void generate() {
        //generate 可以根据背压动态计算需要生产多少数据

        Flowable<Long> numbers = Flowable.generate(() -> 0L, (state, emitter) -> {
            System.out.println("generate 调用了");
            emitter.onNext(state);
            return state + 1;
        });

        numbers.observeOn(Schedulers.newThread()).subscribe(System.out::println);
    }

    @Test
    public void fileReadByBufferedReader() {
        String filePath = "/test.txt";

        Flowable.generate(()->new BufferedReader(new InputStreamReader(FlowableTest.class.getResourceAsStream(filePath))),(reader,emitter)->{
            String line=reader.readLine();
            if(line!=null){
                emitter.onNext(line);
            }else{
                emitter.onComplete();
            }
        },reader->reader.close()).observeOn(Schedulers.io()).subscribe(System.out::println,throwable -> throwable.printStackTrace(),()->System.out.println("read complete"));
    }

    @Test
    public void fileRead() throws URISyntaxException, InterruptedException {
        String filePath = "/test.txt";
        Path path = Paths.get(FlowableTest.class.getResource(filePath).toURI());
        Flowable.create(emitter -> {
            System.out.println("create 调用了");
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer dst = ByteBuffer.allocate(16);
            fileChannel.read(dst, 0, dst, new FileReadHandler(fileChannel, emitter));
        }, BackpressureStrategy.BUFFER).observeOn(Schedulers.io()).subscribeOn(Schedulers.newThread()).subscribe(System.out::print, throwable -> throwable.printStackTrace(), () -> System.out.println("complete"));

        //不加complete监听就要等到sleep结束才触发？为什么？

        TimeUnit.SECONDS.sleep(100);
    }

    private static class FileReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        private AsynchronousFileChannel fileChannel;
        private Emitter emitter;
        //Java.nio.charset.Charset处理了字符转换问题。它通过构造CharsetEncoder和CharsetDecoder将字符序列转换成字节和逆转换。
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();

        CharBuffer charBuffer = CharBuffer.allocate(16);

        private long position;

        public FileReadHandler(AsynchronousFileChannel fileChannel, Emitter emitter) {
            this.fileChannel = fileChannel;
            this.emitter = emitter;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {

            if (result < 0) {
                //读取结束了
                emitter.onComplete();
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;
            }

            buffer.flip();

            decoder.decode(buffer, charBuffer, false);
            charBuffer.flip();

            char[] data = new char[charBuffer.limit()];
            charBuffer.get(data);
            String readInfo = new String(data);
            //读取中文的时候为什么这里会为空？
            if (readInfo.isEmpty()) {
                System.out.println("读取为空");
            }

            emitter.onNext(readInfo);

            charBuffer.clear();
            buffer.clear();

            //位置增加
            position += result;

            fileChannel.read(buffer, position, buffer, this);

        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {

        }
    }

    @Test
    public void firstElement() {
        //firstElement() operator, which is similar to first(), but it returns an empty result if no elements are emitted
        Flowable.just(1, 2, 3).firstElement().subscribe(System.out::println);
        Flowable.empty().firstElement().subscribe(i -> System.out.println(i), throwable -> throwable.printStackTrace(), () -> System.out.println("complete"));
    }
}
