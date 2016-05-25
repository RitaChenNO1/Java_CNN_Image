package cnn;

import util.Log;
import util.Util;

/**
 * Created by chzhenzh on 5/24/2016.
 *
 * input layer (type:input, map 1,)
 * convolutional layer(type: covn, out put how many maps: n,every map size,  kernel matrix(weight matrix),step/stride,bias -->how many kernel, then how many bias, error matrix)
 * sampling layer(type:samp, map n=conv n, map size, scale matrix)
 * output layer(type:output, image class number (0 to c-1), error)
 *batch
 * //the level
 * batch-->record
 * Layer-->Map-->Kernel
 */
public class Layer {

    private  LayerType type;
    private int outMapNum;//every layer have
    private Size mapSize;
    private Size kernelSize;//the kernel size, it means the weight size,only covn layer, eg: weight matrix is 5*5, then Size(5, 5)
    private Size scaleSize;//only samp layer
    private double[][][][] kernel;//the kernel/weight, only covn and output layer, eg: layer 1 to layer 2, weight matrix is 5*5
    private double[] bias;//every map have a bias, only covn and output layer, bias actually is the intercept
    private double[][][][] outmaps;//the out map of every batch, outmaps[0][0] means the 1st record, the 0 out map
    private double[][][][] errors;//the error

    //the record in a batch, this is a global variable in a batch
    private static int recordInBatch=0;//record the number of record in  batch
    private int classNum=-1;// class number

    //***********************************1. enum the 4 Layer Type value***********************************
    enum LayerType {
        // the Layer type: input layer, output layer, convolutional layer, sampling layer;
        input, output, conv, samp
    }

    /**
     * initial the record as 0 for a new batch
     */
    public static void prepareForNewBatch()
    {
        setRecordInBatch(0);
    }

    /**
     * the same batch, go to next record
     */
    public static void prepareForNewRecord()
    {
        recordInBatch++;
    }

    //***********************************start 2. initial to build the 4 layers***********************************
    /**
     * build the input layer, with the Size(width, height) of an image
     * @param mapSize
     * @return
     */
    public static Layer buildInputLayer(Size mapSize)
    {
        Layer layer=new Layer();
        layer.type=LayerType.input;
        layer.outMapNum=1;// input an image, so  the map number is 1
        layer.mapSize=mapSize;
        return layer;
    }

    /**
     * set the convolutional layer with out put map number, and kernel size (weight matrix)
     * @param outMapNum
     * @param kernelSize
     * @return
     */
    public static Layer buildConvLayer(int outMapNum,Size kernelSize)
    {
        Layer layer=new Layer();
        layer.type=LayerType.conv;
        layer.outMapNum=outMapNum;
        layer.kernelSize=kernelSize;
        //mapSize, kernel value,bias, errors need to be initialed later
        return layer;
    }

    /**
     * set the sampling layer with the scale size, select the max pixel value of 2*2 area
     * @param scaleSize
     * @return
     */
    public static Layer buildSampLayer(Size scaleSize)
    {
        Layer layer=new Layer();
        layer.type=LayerType.samp;
        layer.scaleSize=scaleSize;
        //mapSize need to be calculate later, mapNum is equal to the previous conv layer
        return layer;
    }

    /**
     *
     * @param classNum
     * @return
     */
    public static Layer buildOutputLayer(int classNum)
    {
        Layer layer=new Layer();
        layer.type=LayerType.output;
        layer.classNum=classNum;
        layer.mapSize=new Size(1,1);
        layer.outMapNum=classNum;
        //errors need to be calculate later, mapNum is equal to the previous conv layer
        return layer;
    }
    //***********************************end 2. initial to build the 4 layers***********************************

    //***********************************start 3. initial vars of layers***********************************

    /**
     * kernel, previous layers' out map * current layer's out map (this is the link between 2 layers), then 5*5 as kernel/weight matrix
     * @param frontoutMapNum
     */
    public void initkernel(int frontoutMapNum)
    {
        //Log.i(kernelSize.x+":"+kernelSize.y+":"+frontoutMapNum+":"+outMapNum);
        kernel=new double[frontoutMapNum][outMapNum][kernelSize.x][kernelSize.y];
        for(int i=0;i<frontoutMapNum;i++){
            for(int j=0;j<outMapNum;j++)
            {
                kernel[i][j]=  Util.randomMatrix(kernelSize.x,kernelSize.y);
            }
        }
    }

    /**
     * how many out maps in this layer, then how many bias are there
     */
    public void initBias(){
        this.bias=new double[outMapNum];
        bias=Util.randomArray(outMapNum);

    }

    /**
     *every out map has an error matrix,  a map has an internal error matrix
     *
     * @param batchSize   keep rows of  every batch
     */
    public void initerrors(int batchSize)
    {
        errors=new double[batchSize][outMapNum][mapSize.x][mapSize.y];
    }

    /**
     * every out map has an map matrix
     * @param batchSize keep rows of  every batch
     */
    public  void  initoutMap(int batchSize)
    {
        outmaps=new double[batchSize][outMapNum][mapSize.x][mapSize.y];
    }

    public void initOutputKernel(int frontoutMapNum, Size size)
    {
        kernelSize=size;
        //Log.i(kernelSize.x+":"+kernelSize.y+":"+frontoutMapNum+":"+outMapNum);
        kernel=new double[frontoutMapNum][outMapNum][kernelSize.x][kernelSize.y];
        for(int i=0;i<frontoutMapNum;i++){
            for(int j=0;j<outMapNum;j++)
            {
                kernel[i][j]=  Util.randomMatrix(kernelSize.x,kernelSize.y);
            }
        }
    }
    //***********************************end 3. initial vars of layers***********************************

    //***********************************start 4.0. user defined set and get for all variables***********************************

    /**
     *
     * @return
     */
    public void setMapValue(int mapNo,int mapX,int mapY,double value)
    {
        outmaps[recordInBatch][mapNo][mapX][mapY]=value;
    }

    public double[][]getMap(int MapNo)
    {
        return this.outmaps[recordInBatch][MapNo];
    }

    public double[][]getKernel(int previousLayerMapNo,int mapNo)
    {
        return this.kernel[previousLayerMapNo][mapNo];
    }

    public double getBias(int j) {
        return bias[j];
    }

    public void setMapValue(int mapNo, double sum[][]){
        outmaps[recordInBatch][mapNo]=sum;
    }

    public double[][] getOutmaps(int i) {
        return outmaps[recordInBatch][i];
    }

    public void setErrors(int mapNo,int mapX,int mapY,double value){
        errors[recordInBatch][mapNo][mapX][mapY]=value;
    }

    public double[][] getErrors(int mapNo)
    {
        return errors[recordInBatch][mapNo];
    }

    public void setErrors(int mapNo,double [][]value){
        errors[recordInBatch][mapNo]=value;
    }

    public void setBias(int mapNO,double bias)
    {
        this.bias[mapNO]=bias;
    }

    public double[][] getError(int batch,int mapNo)
    {
        return errors[batch][mapNo];
    }

    public double[][] getoutMap(int batch,int mapNo)
    {
        return outmaps[batch][mapNo];
    }

    public void setKernel(int previousMapNO,int mapNO,double[][] kernel_matrix){
        kernel[previousMapNO][mapNO]=kernel_matrix;
    }

    //***********************************start 4.0. user defined set and get for all variables***********************************


    //***********************************start 4. set and get for all variables***********************************

    public LayerType getType() {
        return type;
    }

    public void setType(LayerType type) {
        this.type = type;
    }

    public int getOutMapNum() {
        return outMapNum;
    }

    public void setOutMapNum(int outMapNum) {
        this.outMapNum = outMapNum;
    }

    public Size getMapSize() {
        return mapSize;
    }

    public void setMapSize(Size mapSize) {
        this.mapSize = mapSize;
    }

    public Size getKernelSize() {
        return kernelSize;
    }

    public void setKernelSize(Size kernelSize) {
        this.kernelSize = kernelSize;
    }

    public Size getScaleSize() {
        return scaleSize;
    }

    public void setScaleSize(Size scaleSize) {
        this.scaleSize = scaleSize;
    }

    public double[][][][] getKernel() {
        return kernel;
    }

    public void setKernel(double[][][][] kernel) {
        this.kernel = kernel;
    }

    public double[] getBias() {
        return bias;
    }

    public void setBias(double[] bias) {
        this.bias = bias;
    }

    public double[][][][] getOutmaps() {
        return outmaps;
    }

    public void setOutmaps(double[][][][] outmaps) {
        this.outmaps = outmaps;
    }

    public double[][][][] getErrors() {
        return errors;
    }

    public void setErrors(double[][][][] errors) {
        this.errors = errors;
    }

    public static int getRecordInBatch() {
        return recordInBatch;
    }

    public static void setRecordInBatch(int recordInBatch) {
        Layer.recordInBatch = recordInBatch;
    }

    public int getClassNum() {
        return classNum;
    }

    public void setClassNum(int classNum) {
        this.classNum = classNum;
    }


    //***********************************end 4. set and get for all variables***********************************



}
