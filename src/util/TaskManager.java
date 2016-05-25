package util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chzhenzh on 5/25/2016.
 * CountDownLatch refer to http://blog.csdn.net/shihuacai/article/details/8856370
 *
 */
public abstract class TaskManager {
    private int workLength;
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

    public TaskManager(int workLength){
        this.workLength=workLength;
    }

    //this is a abstract function, it'll be implemented in the real function
    public abstract void process(int start, int end);

    //start is the real to start threads
    public void start(){
        //why 1, it should be workLength?
        int runCpu=cpuNum<workLength?cpuNum:1;
        //Start a count
        final CountDownLatch gate=new CountDownLatch(runCpu);

        //here why???????????????????? why +runCpu, why not workLength/runCpu
        int freeLength=(workLength+runCpu-1)/runCpu;

        //need to start mutliple thread
        for(int cpu=0;cpu<runCpu;cpu++)
        {
            final int start=cpu*freeLength;
            int tmp=(cpu+1)*freeLength;
            final int end=tmp<=workLength?tmp:workLength;
            //set the runnable task, and insert into Executors to run
            Runnable task=new Runnable() {
                @Override
                public void run() {
                    process(start, end);
                    //a thread is done, then count down
                    gate.countDown();
                }
            };
            TaskManager.run(task);
        }
        //wait until all threads are done.
        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
