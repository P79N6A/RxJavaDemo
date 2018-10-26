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
import java.sql.Time;
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
    public void take() {
//        The take() operator has two overloads. One will take a specified number of emissions and then call
//        onComplete() after it captures all of them. It will also dispose of the entire subscription so that no more
//        emissions will occur
        Flowable source = Flowable.interval(1, TimeUnit.SECONDS);
        Flowable flowable = source.take(3);
        flowable.blockingSubscribe(i -> System.out.println("Observer 1:" + i));
        source.blockingSubscribe(i -> System.out.println("Observer 2:" + i));
    }

    @Test
    public void fileReadByBufferedReader() {
        String filePath = "/test.txt";

        Flowable.generate(() -> new BufferedReader(new InputStreamReader(FlowableTest.class.getResourceAsStream(filePath))), (reader, emitter) -> {
            String line = reader.readLine();
            if (line != null) {
                emitter.onNext(line);
            } else {
                emitter.onComplete();
            }
        }, reader -> reader.close()).observeOn(Schedulers.io()).subscribe(System.out::println, throwable -> throwable.printStackTrace(), () -> System.out.println("read complete"));
    }

    private static final int BUFFER_SIZE = 50;

    @Test
    public void fileRead() throws URISyntaxException, InterruptedException {
        String filePath = "/test.txt";
        Flowable.create(emitter -> {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(FlowableTest.class.getResource(filePath).toURI()), StandardOpenOption.READ);
            ByteBuffer dst = ByteBuffer.allocate(BUFFER_SIZE);
            fileChannel.read(dst, 0, dst, new FileReadHandler(fileChannel, emitter));
        }, BackpressureStrategy.BUFFER).subscribe(System.out::println, throwable -> throwable.printStackTrace(), () -> System.out.println("read complete"));

        TimeUnit.SECONDS.sleep(10);
    }

    private static class FileReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        private AsynchronousFileChannel fileChannel;
        private Emitter emitter;

        //UTF-8解码
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = CharBuffer.allocate(BUFFER_SIZE);

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

            decoder.decode(buffer, charBuffer, true);
            charBuffer.flip();

            char[] data = new char[charBuffer.limit()];
            charBuffer.get(data);
            String readInfo = new String(data);

            //发送获取的数据
            emitter.onNext(readInfo);

            charBuffer.clear();
            buffer.clear();

            //位置增加
            position += result;

            fileChannel.read(buffer, position, buffer, this);

        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
        }
    }

    @Test
    public void firstElement() {
        //firstElement() operator, which is similar to first(), but it returns an empty result if no elements are emitted
        Flowable.just(1, 2, 3).firstElement().subscribe(System.out::println);
        Flowable.empty().firstElement().subscribe(i -> System.out.println(i), throwable -> throwable.printStackTrace(), () -> System.out.println("complete"));
    }
}
