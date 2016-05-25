package cnn;

import util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chzhenzh on 5/25/2016.
 */
public class Listener  extends Thread{
    public AtomicBoolean stopTrain;

    /**
     * set listener to listen the stop sign
     * @param stopTrain
     */
    public Listener(AtomicBoolean stopTrain)
    {
        setDaemon(true);
        this.stopTrain = new AtomicBoolean(false);
    }

    /**
     * set the & as stop sign
     */
    @Override
    public void run(){
        Log.i("Input & to stop train the CNN model....");
        while(true){
            int a= 0;
            try {
                a = System.in.read();
                if(a == '&'){
                    stopTrain.compareAndSet(true,false);
                    Log.i("Listener is stopped...");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
