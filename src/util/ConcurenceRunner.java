package util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chzhenzh on 5/25/2016.
 * refer to http://www.itzhai.com/the-executorservice-common-method-newfixedthreadpool-of-create-fixed-size-thread-pool.html#2%E3%80%81Executor%EF%BC%9A
 * talk about ExecutorService, and Executors
 */
public class ConcurenceRunner {

    public ExecutorService a;
    private static final ExecutorService exec;
    public static final int cpuNum;
    static{
        //get the avaliable cpu numbers
        //and start cpuNum threads.
        cpuNum=Runtime.getRuntime().availableProcessors();
        exec= Executors.newFixedThreadPool(cpuNum);
    }

    public static void run(Runnable task){
        exec.execute(task);
    }

    public static void stop(){
        exec.shutdown();
    }


}
