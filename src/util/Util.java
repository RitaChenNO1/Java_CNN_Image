package util;

import cnn.Size;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * Created by chzhenzh on 5/25/2016.
 */
public class Util {

    //random with seed 2
    private static Random r = new Random(2);

    /**
     * randomly to initial the matrix // for kernel
     * @param x
     * @param y
     * @return
     */
    public static double[][] randomMatrix(int x, int y)
    {
        double matrix[][]=new double[x][y];
        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++)
            {
                //set the weight between -0.05 to 0.05?? is bellow real?
                //???????????????? I think it should be Random(1), and here (r.nextDouble()-0.5)/10
                matrix[i][j]=(r.nextDouble()-0.05)/10;
            }
        }
        return matrix;
    }

    /**
     * initial the array// for bias, all initial to 0
     * @param len
     * @return
     */
    public static double[] randomArray(int len)
    {
        double data[]=new double[len];
        for(int i=0;i<len;i++){
                //set the weight between -0.05 to 0.05?? is bellow real?
                //???????????????? I think it should be Random(1), and here (r.nextDouble()-0.5)/10
            data[i]=0;
        }
        return data;
    }

    /**
     *batch learning CNN, randomly sampling the data from dataset, batchsize rows from [0,size)
     * can not be duplicated, so need to use hashset
     * @param size  the dataset size
     * @param batchSize the min-batch size
     * @return
     */
    public static int[] randomPerm(int size, int batchSize)
    {
        Set<Integer> set=new HashSet<Integer>();
        //since can not be duplicated, so check the size is the best way
        //HashSet will delete duplicated value automatically
        while(set.size()<batchSize) {
            set.add(r.nextInt(size));
        }
        //store the rows number to randPerm for the CNN
        int [] randPerm=new int[batchSize];
        int i=0;
        for(Integer value: set)
        {
            randPerm[i]=value;
            i++;
        }
        return randPerm;
    }


    /**
     * https://cs231n.github.io/convolutional-networks/#conv
     * Convolution Demo.
     *  need to choose 5*5 from matrix to * kernel 5*5, and then step 1, etc...
     * @param matrix  28:28
     * @param kernel  5*5
     * @return  (28-5+1)*(28-5+1)
     */
    public static double[][] convnValid(double[][] matrix,double[][]kernel){
        int m=matrix.length;
        int n=matrix[0].length;
        int km=kernel.length;
        int kn=kernel[0].length;
        //output map size: (28-5+1)*(28-5+1)
        int kms=m-km+1;
        int kns=n-kn+1;

        //the output matrix
        //Log.i(m+":"+n+":"+km+":"+kn);
        double outMatrix[][]=new double[kms][kns];
        for(int i=0;i<kms;i++)
        {
            for(int j=0;j<kns;j++)
            {
                double sum=0;
                //matrix 5*5, kernel 5*5
                for(int ki=0;ki<km;ki++ )
                {
                    for(int kj=0;kj<kn;kj++)
                    {
                        sum+=matrix[i+ki][j+kj]*kernel[ki][kj];
                    }
                }
                outMatrix[i][j]=sum;
            }
        }
        /*
        if(km==1 && kn==1)
        {
            Log.i("is in"+kms+":"+kns+":outMatrix:"+outMatrix.length);
        }*/
        return outMatrix;
    }

    public static double[][] matrixOp(double[][] matrixa,Operator op)
    {
        int axlen=matrixa.length;
        int aylen=matrixa[0].length;
        for(int i=0;i<axlen;i++) {
            for (int j = 0; j < aylen; j++) {
                matrixa[i][j]=op.process(matrixa[i][j]);
            }
        }
        return matrixa;
    }
    /**
     *
     * @param matrixa
     * @param matrixb
     * @param opa  , it could be matrix a , to add 1 value
     * @param opb , it could be matrix a , to add 1 value
     * @param op2 , it could be matrix a + matrix b
     * @return
     */
    public static double[][] matrixOp(double[][] matrixa, double[][] matrixb,Operator opa,Operator opb, OperatorOnTwo op2)
    {
        int axlen=matrixa.length;
        int aylen=matrixa[0].length;
        int bxlen=matrixb.length;
        int bylen=matrixb[0].length;
        if(axlen!=bxlen || aylen!=bylen)
        {
            //?? here has a problem, if matrix multiply matrix, don't need to be same length
            Log.i(axlen+":"+aylen+":"+bxlen+":"+bxlen);
            Log.err("this 2 matrix size are different.");
        }else{
            for(int i=0;i<axlen;i++){
                for(int j=0;j<aylen;j++)
                {
                    double a=matrixa[i][j];
                    double b=matrixa[i][j];
                    if(opa!=null){
                        a=opa.process(a);
                    }
                    if(opb!=null){
                        b=opa.process(b);
                    }
                    if(op2!=null)
                    {
                        matrixb[i][j]=op2.process(a,b);
                    }
                }
            }
        }
        return matrixb;
    }


    /**
     *
     * @param matrix
     * @param scaleSize
     * @return
     */
    public static double[][] scaleMatrix(double[][] matrix,final Size scaleSize){
        int mxlen=matrix.length;
        int mylen=matrix[0].length;
        int outmxlen=mxlen/scaleSize.x;
        int outmylen=mylen/scaleSize.y;
        double outMatrix[][]=new double[outmxlen][outmylen];
       // Log.i("in:"+mxlen+":"+mylen);
        if(mxlen%scaleSize.x!=0 ||mylen%scaleSize.y!=0 ){
            Log.err("this map size is not divisable by sampling layer's scale Size.");
        }
        //the out map (0,0),  0*x to 0*x+x, 0*y to 0*y+y
        else{
            int size=scaleSize.x*scaleSize.y;
            for(int i=0;i<outmxlen;i++) {
                for (int j = 0; j < outmylen; j++) {
                    //sum all value then get mean
                    double sum=0.0;
                    for(int si=i*scaleSize.x;si<(i+1)*scaleSize.x;si++) {
                        for(int sj=j*scaleSize.y;sj<(j+1)*scaleSize.y;sj++) {
                           // Log.i(si+":"+sj+":"+i+":"+j);
                            sum+=matrix[si][sj];
                        }
                    }
                    outMatrix[i][j]=sum/size;
                }
            }
        }
        return outMatrix;
    }

    /**
     * expand the samp error with scaleSize  12*12 error matrix, 2*2 matrix to 24*24
     * @param sampMap
     * @param scaleSize
     * @return
     */
    public static double[][] kronecker(double[][] sampMap,Size scaleSize){
        int mxlen=sampMap.length;
        int mylen=sampMap[0].length;
        double[][] outMatrix=new double[mxlen*scaleSize.x][mylen*scaleSize.y];
        for(int i=0;i<mxlen;i++) {
            for (int j = 0; j < mylen; j++) {
                //sum all value then get mean
                double sum=0.0;
                for(int si=i*scaleSize.x;si<(i+1)*scaleSize.x;si++) {
                    for(int sj=i*scaleSize.y;sj<(j+1)*scaleSize.y;sj++) {
                        outMatrix[si][sj]=sampMap[i][j];
                    }
                }
            }
        }
        return outMatrix;
    }


    /**
     * return the max value's index from an array
     * @param out
     * @return
     */
   public static int getMaxIndex(double []out){
       double max=out[0];
       int maxIndex=0;
       for(int i=1;i<out.length;i++)
       {
           if(out[i]>max){
               max=out[i];
               maxIndex=i;
           }
       }
       return maxIndex;
   }

    /**extend the error matrix to  (n+2(k-1))*(n+2(k-1)) with kernel matrix size
     * @param matrix
     * @param kernel
     * @return
     */
    public static double[][]convnFull(double[][] matrix,double[][] kernel)
    {
        int nxlen=matrix.length;
        int nylen=matrix[0].length;
        int kxlen=kernel.length;
        int kylen=kernel[0].length;
        double outMatrix[][]=new double[nxlen+2*(kxlen-1)][nylen+2*(kylen-1)];
        for(int i=0;i<nxlen;i++) {
            for (int j = 0; j < nylen; j++) {
                outMatrix[i+(kxlen-1)][j+(kylen-1)]=matrix[i][j];
            }
        }
        return convnValid(outMatrix,kernel);
    }

    public static double[][] rot180(double[][] matrix ){
        double[][] outMatrix=cloneMatrix(matrix);
        int nxlen=matrix.length;
        int nylen=matrix[0].length;
        //exchange with columns
        for(int i=0;i<nxlen;i++) {
            for (int j = 0; j < nylen/2; j++) {
                double tmp=matrix[i][j];
                outMatrix[i][j]=matrix[i][nylen-1-j];
                outMatrix[i][nylen-1-j]=tmp;
            }
        }
        //exchange with rows
        for(int i=0;i<nxlen/2;i++) {
            for (int j = 0; j < nylen; j++) {
                double tmp=matrix[i][j];
                outMatrix[i][j]=matrix[nxlen-1-i][j];
                outMatrix[nxlen-1-i][j]=tmp;
            }
        }
        return outMatrix;
    }

    public static double[][] cloneMatrix(double[][] matrix) {
        int nxlen = matrix.length;
        int nylen = matrix[0].length;
        double outMatrix[][] = new double[nxlen][nylen];
        for (int i = 0; i < nxlen; i++) {
            for (int j = 0; j < nylen; j++) {
                outMatrix[i][j]=matrix[i][j];
            }
        }
        return outMatrix;
    }

    public static double sum(double[][][][] errors,int mapNo){
        int batchSize = errors.length;
        int mapNo_errorX = errors[0][mapNo].length;
        int mapNo_errorY =errors[0][mapNo][0].length;
        double sum=0;
        for (int i = 0; i < batchSize; i++) {
            for (int j = 0; j < mapNo_errorX; j++) {
                for (int k = 0; k < mapNo_errorY; k++) {
                    //Log.i("in:"+i+":"+mapNo+":"+j+":"+k);
                    sum+=errors[i][mapNo][j][k];
                }
            }
        }
        return sum;
    }
    //***********************************start 1. operators***********************************
    public interface Operator extends Serializable{
        public double process(double a);
    }


    public static final Operator one_value=new Operator() {
        @Override
        public double process(double a) {
            return 1-a;
        }
    };

    public interface OperatorOnTwo extends Serializable{
        public double process(double a,double b);
    }

    public static final OperatorOnTwo plus=new OperatorOnTwo() {
        @Override
        public double process(double a, double b) {
            return a+b;
        }
    };

    public static final OperatorOnTwo multiply=new OperatorOnTwo() {
        @Override
        public double process(double a, double b) {
            return a*b;
        }
    };



    //***********************************end 1. operators***********************************


    //***********************************2. functions***********************************

    public static double sigmoid(double x)
    {
        return 1/(1+Math.exp(-x));
    }

}

