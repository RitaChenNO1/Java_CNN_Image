package cnn;

import util.Log;

import java.io.Serializable;

/**
 * Created by chzhenzh on 5/24/2016.
 * set the size (matrix, has x, y) of Layer
 */
public  class Size implements Serializable {

    //should serialize the class, otherwise, the model could not be saved.
    //error message: java.io.NotSerializableException: cnn.CNN
    private static final long serialVersionUID = -209157832162004118L;
    public int x;
    public int y;

    public Size(int x, int y)
    {
        this.x=x;
        this.y=y;
    }

    /**
     * compute the Map Size of convolutional layer,   //W2=(W1?F+2P)/S+1, F is the kernel size, the pad is 0, Step/stride is 1
     * @param kernelSize
     * @param append
     * @return
     */
    public Size substract(Size kernelSize,int append)
    {
        return new Size(this.x-kernelSize.x+append,this.y-kernelSize.y+append);
    }

    /**
     * compute the map size of sampling layer,  28*28, 2*2, then 14*14
     * @param scaleSize
     * @return
     */
    public Size divide(Size scaleSize)
    {
        int x=this.x/scaleSize.x;
        int y=this.y/scaleSize.y;
        if(this.x%scaleSize.x==0 && this.y%scaleSize.y==0){
            return new Size(x,y);
        }else{
            Log.err("x="+this.x+":" +"y="+this.y+":"+ " can not be divisible "+"x="+scaleSize.x+":" +"y="+scaleSize.y+":");
            return null;
        }
    }
}
