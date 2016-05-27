package cnn;

import dataset.Dataset;
import dataset.Record;
import util.Log;
import util.TaskManager;
import util.Util;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import util.Util.Operator;

/**
 * Created by chzhenzh on 5/24/2016.
 * https://cs231n.github.io/convolutional-networks/
 * input layer-->convolutional layer1-->sampling layer1-->convolutional layer2-->sampling layer2-->output layer
 *this is batch learning(Mini-batches Learning) of CNN, the batch size m=50;
 *
 * train:
 * forward to compute value of node,
 * backward to compute error of node, and update the weight of the previous layer to the layer
 */
public class CNN  implements Serializable {

    //should serialize the class, otherwise, the model could not be saved.
    //error message: java.io.NotSerializableException: cnn.CNN
    private static final long serialVersionUID = 337920299147929932L;
    //learning factor
    private double LAMBDA=0;
    //the step size, alpha for bias and weight/kernel
    private double ALPHA=0.85;
    //the layer of neural network
    private List<Layer> layers;
    //the numbers of layers
    private int layerNum;

    //min-batch size
    private int batchSize;

    //use delta kernel to divide the batchsize in updateKernel function

    public Operator divide_batchSize;
    // alpha*error matrix in updateKernel function
    public Operator multiply_alpha;
    //(1-lambda*alpha)*wij
    public Operator multiply_lambda;

    //initial all layers

    /**
     *
     * @param layerBuilder
     * @param batchSize
     */
    public CNN(LayerBuilder layerBuilder, int batchSize){
        layers=layerBuilder.mLayers;
        layerNum=layers.size();
        this.batchSize=batchSize;
        //initial all vars of every layer
        setup(batchSize);
        initoperator();
    }
    public void initoperator(){
        divide_batchSize= new Operator() {
            @Override
            public double process(double a) {
                return a/batchSize;
            }
        };
        multiply_alpha=new Operator() {
            @Override
            public double process(double a) {
                return a*ALPHA;
            }
        };
        multiply_lambda=new Operator() {
            @Override
            public double process(double a) {
                return a*(1-LAMBDA*ALPHA);
            }
        };


    }

    /**
     * Train the CNN model
     * @param trainset : the input dataset(binarized image data with label in last column)
     * @param repeat ; the running iterations
     */

    public void train(Dataset trainset,int repeat){

        //datazise/batchsize to ge the batch number
        //start the listener to try to see need or not to stop the train
        new Listener().start();
        Log.i(stopTrain.get()+"");
        //repeat 3 times
        for(int t=1; t<=repeat && !stopTrain.get();t++){
            //compute every batch's rows
            int dataSize=trainset.size();
            int ephochsNum=dataSize/ batchSize;
            // need to get the ceiling
            if(dataSize%batchSize !=0){
                ephochsNum++;
            }
            Log.i(t+"th repeat for min-batch:"+ephochsNum);
            //1.1every batch, randomly select batchSize rows from the source into the batch
            //1.2batch start:
            //1.3every row of batch, need to train
            //1.4batch end: update all errors
            int Positive=0;
            int count=0;
            for(int i=0;i<ephochsNum;i++){
                //1.1
                int[] randPerm= Util.randomPerm(dataSize,batchSize);
                //initial the record in batch as 0
                Layer.prepareForNewBatch();
                //1.2start a batch
                //the batch, to train every rows in batch
                for (int index=0;index<randPerm.length;index++)
                {
                    //1.3
                    boolean isPositive=train(trainset.getRecord(index));
                    if(isPositive){
                        Positive++;
                    }
                    count++;
                    //train the next record in min-batch
                    Layer.prepareForNewRecord();

                }
                //1.4 finish a batch, need to update the weights/kernel
                updateParas();
                //Log.i(t+"th repeat,"+i+"th batch done.");
                //output for what?
            }
            double accuracy=(1.0*Positive)/count;
            Log.i(t+"th repeat, accuracy:"+accuracy);
            //when it's the first several round running, and accuracy is very high
            //need to adjust the ALPHA
            if(t%10<3 && accuracy>0.96){
                ALPHA=0.001+0.9*ALPHA;
                Log.i("Adjust ALPHA = "+ALPHA+" at round runing: "+t+",since accuracy is "+accuracy+" which is more than 0.96.");
            }
        }
        //Log.i("all done.");

    }

    /**
     * train every record in min-batch
     * @param record
     * @return
     */
    public boolean train(Record record){
        forward(record);
        boolean result=backPropagation(record);
        return result;
    }

    //***********************************start 1. initial vars of layers for CNN***********************************
    /**
     * initial all variables of layers
     * @param batchSize
     */
    public void setup(int batchSize)
    {
        Layer inputLayer=layers.get(0);
        inputLayer.initoutMap(batchSize);
        for(int i=1; i<layerNum; i++)
        {
            Layer layer=layers.get(i);
            Layer previouslayer=layers.get(i-1);
            int previousoutMapNum=previouslayer.getOutMapNum();
            switch (layer.getType()){
                case input:
                    break;
                case conv:
                    //mapSize, kernel, bias, error, out maps values
                    //input map: 28*28, kernel size 5*5, then it's (28-5)+1
                    layer.setMapSize(previouslayer.getMapSize().substract(layer.getKernelSize(), 1));
                    //the kernel has the frontlayer out map number * current out map number
                    layer.initkernel(previousoutMapNum);
                    //how many out maps, then bias is same,every map has bias
                    layer.initBias();
                    //error, every map of every row has an error; only in convolutional layer
                    layer.initerrors(batchSize);
                    //value of out maps of this layer
                    layer.initoutMap(batchSize);
                    break;
                case samp:
                    //out map number,mapSize, outMap
                    //out map number is same with previous layer
                    layer.setOutMapNum(previousoutMapNum);
                    //in map: 24*24, scale size 2*2, then it's 24/2 *24/2=12*12
                    layer.setMapSize(previouslayer.getMapSize().divide(layer.getScaleSize()));
                    //error,  only in convolutional layer, but here, need to keep the error
                    layer.initerrors(batchSize);
                    //value of out maps of this layer
                    layer.initoutMap(batchSize);
                    break;
                case output:
                    //mapSize, kernel, bias, error, out maps values
                    //output layer's map size= preivous layer's map size
                    //layer.setMapSize(previouslayer.getMapSize());
                    //the output layer kernel is same with previous layer's Map Size
                    layer.initOutputKernel(previousoutMapNum,previouslayer.getMapSize());
                    //every map has bias
                    layer.initBias();
                    //error, every map of every row needs to keep an error;
                    layer.initerrors(batchSize);
                    //value of out maps of this layer
                    layer.initoutMap(batchSize);
                    break;
            }
        }
    }
    //***********************************end 1. initial vars of layers for CNN***********************************

    //***********************************start 2. training(forward and backward) for CNN***********************************
    //train forward to compute the value of every map
    public void forward(Record record)
    {
        //set the record as input layer's, the record array to 28*28 matrix
        setInputLayerOutput(record);
        //for other layers
        for(int i=1;i<layerNum;i++)
        {
            Layer layer=layers.get(i);
            Layer previousLayer=layers.get(i-1);
            switch (layer.getType())
            {
                case input:
                    break;
                case conv:
                    //compute the out value  5*5 map * kernel 5*5  sum(m_ij*k_ij)+bias[i]
                    setConvLayerOutput(previousLayer,layer);
                    break;
                case samp:
                    //compute the out value, the conv 24*24, divide 2*2, choose the max value
                    setSampLayerOutput(previousLayer,layer);
                    break;
                case output:
                    //
                    setConvLayerOutput(previousLayer,layer);
                    break;
                default:
                    break;
            }

        }

    }

    //propagation the errors
    public boolean backPropagation(Record record)
    {
        boolean result=setOutputLayerErrors(record);
        setHiddenLayerErrors();
        return result;
    }

    //*************start: forward*************
    public void setInputLayerOutput(Record record)
    {
        Layer layer=layers.get(0);
        Size mapSize=layer.getMapSize();
        double []attr=record.getAttr();
        if(attr.length!=mapSize.x*mapSize.y)
        {
            Log.err("the image size is not match with the Map Size, eg: 28*28=748 input data, but Map size is not 28*28.");
        }else{
            //set the attr 748 to 28*28 matrix
            for(int x=0;x<mapSize.x;x++){
                for(int y=0;y<mapSize.y;y++)
                {
                    //set the 1 dimensional attr to 28*28(mapSize) matrix
                    //input the mapNo is 0
                    layer.setMapValue(0,x,y,attr[x*mapSize.x+y]);
                }
            }
        }
    }

    /**
     *   5*5 input matrix * 5*5 kernel matrix to 1 value of 24*24
     * @param previousLayer
     * @param layer
     */
    public void setConvLayerOutput(final Layer previousLayer, final Layer layer)
    {
        final int previousOutMapNum=previousLayer.getOutMapNum();
        int OutMapNum = layer.getOutMapNum();
        //multiple thread to run part of map
        new TaskManager(OutMapNum){
            //run start map to end map
            @Override
            public void process(int start, int end){
                //current layer's map
                for(int j=start;j<end;j++){
                    double [][]sum=null;
                    //previous layer's map
                    for(int i=0;i<previousOutMapNum;i++)
                    {
                        double[][] previousMap=previousLayer.getMap(i);
                        double[][] kernel=layer.getKernel(i,j);
                        //sum(value_ij*w_ij)+bias
                        if(sum==null){
                            sum=Util.convnValid(previousMap,kernel);
                        }else{
                            sum=Util.matrixOp(Util.convnValid(previousMap,kernel),sum,null,null,Util.plus);
                        }
                        //+bias
                        final double bias=layer.getBias(j);
                        //sum+bias, then sigmoid
                        sum=Util.matrixOp(sum, new Util.Operator() {
                            @Override
                            public double process(double a) {
                                return Util.sigmoid(a+bias);
                            }
                        });
                    }
                    layer.setMapValue(j,sum);
                }
            }
        }.start();
    }

    public void setSampLayerOutput(final Layer previousLayer, final Layer layer)
    {
        //out map number is same with previous layer
        int OutMapNum = layer.getOutMapNum();
        //multiple thread to run part of map
        new TaskManager(OutMapNum) {
            //run start map to end map
            @Override
            public void process(int start, int end) {
                //all previous layer's map ,sampling layer map size is 24*24 divide 2*2 =12*12,
                for(int i=start;i<end;i++){
                    double[][] previousMap=previousLayer.getMap(i);
                    Size scaleSize=layer.getScaleSize();
                    //get the mean of 2*2 scale area
                    double outSampMatrix[][]=Util.scaleMatrix(previousMap,scaleSize);
                    layer.setMapValue(i,outSampMatrix);
                    //set to current layer
                }
            }
        }.start();

    }
    //*************end: forward*************

    //*************start: Propagation*************

    /**
     * set the error for output layer
     * @param record
     * @return
     */
    public boolean setOutputLayerErrors(Record record)
    {
        Layer outputLayer=layers.get(layerNum - 1);
        int outMapnum=outputLayer.getOutMapNum();
        //index of target is the class, if belong to the class, set 1
        //Log.i("outMapnum"+outMapnum);
        double target[]=new double[outMapnum];
        double outMaps[]=new double[outMapnum];
        int label= ((int) record.getLabel());
        target[label]=1;
        for(int i=0;i<outMapnum;i++)
        {
            double[][] outMap=outputLayer.getOutmaps(i);
            outMaps[i]=outMap[0][0];
            //error= p*(1-p)*(target-p)????????? why not; target-p
            outputLayer.setErrors(i,0,0,outMaps[i]*(1-outMaps[i])*(target[i]-outMaps[i]));
        }
        //check if the biggest probability is same with the class label
        return label==Util.getMaxIndex(outMaps);
    }

    public void setHiddenLayerErrors()
    {
        //for hidden layers(conv, samp)
        for(int i=layerNum-1;i<0;i--) {
            Layer layer = layers.get(i);
            Layer nextLayer = layers.get(i + 1);
            switch (layer.getType()) {
                case input:
                    //don't have errors
                    break;
                case conv:
                    //set the errors
                    setConvLayerErrors(layer,nextLayer);
                    break;
                case samp:
                    //set the errors
                    setSampLayerErrors(layer,nextLayer);
                    break;
                case output:
                    //already done in function setOutputLayerErrors
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * set the error for convolutional layer
     * @param layer
     * @param nextlayer
     */
    public void setConvLayerErrors(final Layer layer, final Layer nextlayer)
    {
        //the map number is same for conv layer and next samp layer
        int OutMapNum=layer.getOutMapNum();
        new TaskManager(OutMapNum) {
            //run start map to end map
            @Override
            public void process(int start, int end) {
                //current layer's map
                for (int j = start; j < end; j++) {
                    //errors=outmap*(1-outmap)*kronecker(nextlayer.outmap)
                    //
                    double[][] outMap=layer.getMap(j);
                    double[][] nextError=nextlayer.getErrors(j);
                    //here may need to cloneMatrix,since it will use the same value, 1 changed, another changed,too.
                    double[][] outErrors=Util.matrixOp(outMap,Util.cloneMatrix(outMap),null,Util.one_value,Util.multiply);
                    //next layer is samp layer, need to
                    //refer to http://blog.sina.com.cn/s/blog_4aa887440102wbxz.html
                    outErrors=Util.matrixOp(outErrors,Util.kronecker(nextError,nextlayer.getScaleSize()),null,null,Util.multiply);
                    layer.setErrors(j,outErrors);
                }
            }
        }.start();
    }

    /**
     * set the error for sampling layer,  =sum (all the error of all next conv maps * response kernel rotate 180)
     * @param layer
     * @param nextlayer
     */
    public void setSampLayerErrors(final Layer layer, final Layer nextlayer)
    {
        int OutMapNum=layer.getOutMapNum();
        final int nextOutMapNum=nextlayer.getOutMapNum();
        new TaskManager(OutMapNum) {
            //run start map to end map
            @Override
            public void process(int start, int end) {
                //current layer's map

                for (int j = start; j < end; j++) {
                    double sum[][]=null;
                    for(int i=0;i<nextOutMapNum;i++)
                    {
                        //http://blog.sina.com.cn/s/blog_4aa887440102wbxz.html
                        //get next layer error
                        double nextErrors[][]=nextlayer.getErrors(i);
                        //get next layer kernel,
                        double nextKernel[][]=nextlayer.getKernel(j,i);
                        //extend  next layer error
                        // 180 rotation kernel
                        if(sum==null){
                            sum=Util.convnFull(nextErrors, Util.rot180(nextKernel));
                        }else{
                            sum=Util.matrixOp(Util.convnFull(nextErrors, Util.rot180(nextKernel)),sum,null,null,Util.plus);
                        }
                    }
                    layer.setErrors(j,sum);
                }
            }
        }.start();

    }


    //*************end: Propagation*************
    //***********************************end 2. training(forward and backward) for CNN***********************************

    //***********************************start 3. update the weight,bias/intercept for CNN***********************************
    public void updateParas()
    {
        for(int i=1;i<layerNum;i++)
        {
            Layer layer = layers.get(i);
            Layer previousLayer = layers.get(i - 1);
            switch (layer.getType()) {
                case input:
                    //don't have kernel/weight and bias
                    break;
                case conv:
                    //update kernel/weight and bias
                    //Log.i("layer conv:"+i);
                    updateKernel(layer, previousLayer);
                    updateBias(layer);
                    break;
                case samp:
                    //don't have kernel/weight and bias
                    break;
                case output:
                    //update kernel/weight and bias
                    //Log.i("layer output:"+i+"layer kernel size"+layer.getKernelSize().x);
                    updateKernel(layer,previousLayer);
                    updateBias(layer);
                    break;
                default:
                    break;
            }

        }
    }

    /**
     * wij=learning_factor*wij+alpha*delta(wij)  ,learning_factor is lamda, delta=error*xij , xij is the out value of previous layer
     * @param layer
     * @param previousLayer
     */
    public void updateKernel(final Layer layer, final Layer previousLayer)
    {
        int OutMapNum=layer.getOutMapNum();
        final int previousOutMapNum=previousLayer.getOutMapNum();
       // final double[][][][] errors=layer.getErrors();
        new TaskManager(OutMapNum) {
            //run start map to end map
            @Override
            public void process(int start, int end) {
                //current layer's map
                for (int j = start; j < end; j++) {
                    //previous layer
                    for(int i=0;i<previousOutMapNum;i++)
                    {
                        double[][] delta_kernel=null;
                        for(int b=0;b<batchSize;b++)
                        {
                            //current layer's error
                            double[][] error=layer.getError(b,j);
                            //previous layer's outMap
                            double[][] previousMap=previousLayer.getoutMap(b,i);
                            if(delta_kernel==null){
                                delta_kernel=Util.convnValid(previousMap,error);
                               // Log.i("delta_kernel:" + Arrays.toString(delta_kernel[0])+":"+ Arrays.toString(previousMap[0])+":"+ error.length+":"+error[0].length+":kernel->"+delta_kernel.length);

                            }else{//sum all
                                delta_kernel=Util.matrixOp(Util.convnValid(previousMap,error),delta_kernel,null,null,Util.plus);
                            }
                           // Log.i("delta_kernel:" + Arrays.toString(delta_kernel[0])+":"+ Arrays.toString(delta_kernel[1]));
                            //sum/batchSize
                            delta_kernel=Util.matrixOp(delta_kernel, divide_batchSize);
                            // h*wij+alpha*delta
                            layer.setKernel(i,j,Util.matrixOp(layer.getKernel(i,j),delta_kernel,multiply_lambda,multiply_alpha,Util.plus));
                        }
                    }
                }
            }
        }.start();
    }

    /**
     * bias=bias+alpha*error
     * @param layer
     */
    public void updateBias(final Layer layer)
    {
        int OutMapNum=layer.getOutMapNum();
        final double[][][][] errors=layer.getErrors();
        new TaskManager(OutMapNum) {
            //run start map to end map
            @Override
            public void process(int start, int end) {
                //current layer's map
                for (int j = start; j < end; j++) {
                    double delta_bias=Util.sum(errors, j)/batchSize;
                    double bias=layer.getBias(j)+ALPHA*delta_bias;
                    layer.setBias(j,bias);
                }
            }
        }.start();

    }
    //***********************************end 3. update the weight, bias/intercept for CNN***********************************

    private static AtomicBoolean stopTrain;
//Listener class must be here, since the variable stopTrain, otherwise, it'll be error for the stopTrain is null
    public class Listener  extends Thread{
       // public AtomicBoolean stopTrain;

        /**
         * set listener to listen the stop sign

         */
        public Listener()
        {
            setDaemon(true);
            stopTrain = new AtomicBoolean(false);
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
            Log.i("Listener stopped automatically..");
        }
    }


    /**
     * save the model to a file
     * @param fileName
     */
    public void saveModel(String fileName)
    {
        try {
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CNN loadModel(String fileName){
            try {
                ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName));
                CNN cnn=(CNN)in.readObject();
                in.close();
                return cnn;
            } catch (IOException|ClassNotFoundException e) {
                e.printStackTrace();
            }
        return null;
    }

    public double test(Dataset testset){
        Layer.prepareForNewBatch();
        Iterator<Record> iter=testset.iter();
        int Positive=0;
        while(iter.hasNext())
        {
            Record record= iter.next();
            forward(record);
            //get the predict probability of class
            Layer outputLayer=layers.get(layerNum-1);
            int mapNum=outputLayer.getOutMapNum();
            double[] out=new double[mapNum];
            for(int m=0;m<mapNum;m++){
                double[][] outmap=outputLayer.getMap(m);
                //the 1st batch, the m map, matrix
                out[m]=outmap[0][0];
            }
            int label=Util.getMaxIndex(out);
            if((int)record.getLabel()==label)
            {
                Positive++;
            }
        }
        double p=(1.0*Positive)/testset.size();
        Log.i("Validation accuracy",p+"");
        return p;
    }

    public void predict(Dataset testset,String fileName){
        try {
            PrintWriter pw=new PrintWriter(new File(fileName));
            Layer.prepareForNewBatch();
            Iterator<Record> iter=testset.iter();
            int Positive=0;
            while(iter.hasNext())
            {
                Record record= iter.next();
                forward(record);
                //get the predict probability of class
                Layer outputLayer=layers.get(layerNum-1);
                int mapNum=outputLayer.getOutMapNum();
                double[] out=new double[mapNum];
                for(int m=0;m<mapNum;m++){
                    double[][] outmap=outputLayer.getMap(m);
                    //the 1st batch, the m map, matrix
                    out[m]=outmap[0][0];
                }
                int label=Util.getMaxIndex(out);
                if((int)record.getLabel()==label)
                {
                    Positive++;
                }
                pw.write(label+"\n");
            }
            double p=(1.0*Positive)/testset.size();
            Log.i("predict accuracy",p+"");
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Log.i("Predict is done.");
    }

}


