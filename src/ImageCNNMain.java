import cnn.CNN;
import cnn.Layer;
import cnn.LayerBuilder;
import cnn.Size;
import dataset.Dataset;
import image.ImagePreprocess;
import util.Log;

import java.util.Arrays;

/**
 * Created by chzhenzh on 5/19/2016.
 * http://research.microsoft.com/en-us/um/people/kahe/eccv14sppnet/index.html
 * Data located on server: 15.107.21.26
 * Train image:
 * /home/grid/RIM2/Datasets/ILSVRC2015_CLS/Data/CLS-LOC/train/
 * Train image, object detection xml file
 * /home/grid/RIM2/Datasets/ILSVRC2015_CLS/Annotations/CLS-LOC/train
 * label,
 * /home/grid/RIM2/Datasets/ILSVRC2015_CLS/ImageSets/CLS-LOC/train_cls.txt
 *n01440764/n01440764_10361 10 ==>n01440764 is the class label, n01440764_10361 is the image ID
 * code refer to:
 * https://github.com/BigPeng/JavaCNN/tree/master/src/edu/hitsz/c102c
 * Since the output
 of the network has a fixed dimension, we predict a mask of a fixed size N = d  d. After being
 resized to the image size, the resulting binary mask represents one or several objects: it should have
 value 1 at particular pixel if this pixel lies within the bounding box of an object of a given class and
 0 otherwise.
 ************
 * 1. resize the image to 32*32 ,
 * 2. binarized the image to 0 1 value, the last column is the class label
 * 3. CNN --Here
 * 4. validate image
 */
public class ImageCNNMain {

    public static void main(String args[]){

        //1. split to train dataset, validation dataset, test dataset
        //reside the image to N*N
        //a pixel belong to an object, set it to 1, otherwise 0;
        /*int max_class=10;
        int rows_train=1000;
        int targetWidth=28;
        int targetHeight=28;*/
        //args 0 is the main class name
        //[ImageCNNMain, 10, 1000, 28, 28, /home/grid/Rita/Data/train_imagenet.format, /home/grid/Rita/Data/val_imagenet.format]
        Log.i(Arrays.toString(args));
        int i=0;
        //for unix, the 1st argument is the class name, so need to +1
        //but for win7, the 1st argument is the 10(max_class)
        String os = System.getProperty("os.name");
        if(!os.toLowerCase().startsWith("win")){
            i++;
        }
        int max_class=Integer.parseInt(args[i++]);
        int rows_train=Integer.parseInt(args[i++]);
        int targetWidth=Integer.parseInt(args[i++]);
        int targetHeight=Integer.parseInt(args[i++]);
        String trainValPath=args[i++];
        String isDataExist=args[i++];
        String trainPath=trainValPath+"train_imagenet.format";
        String valPath=trainValPath+"val_imagenet.format";
        String modelFile=trainValPath+"cnn.model";
        if(!isDataExist.equals("1")) {
            Log.i("----> 1.Start to resize and binarized the source images...");
            ImagePreprocess.resizeBinImageFile(max_class, rows_train, targetWidth, targetHeight, trainPath, valPath);
            Log.i("End to resize and binarized the source images");
        }else{
            Log.i("Training Dataset and Validation dataset are already there,we could start to train the model now.");
        }
        //**********************************************************
        //2.load the data from Data/train.format
        //String filePath="Data/train.format";
        Log.i("----> 2.Start to train the CNN model...");
        String filePath=trainPath;
        Dataset dataset=Dataset.load(filePath,",",targetWidth*targetHeight);
        //3. build all layers of CNN
        LayerBuilder builder=new LayerBuilder();
        //build input layer
        builder.addLayer(Layer.buildInputLayer(new Size(targetWidth, targetHeight)));
        //build 1st convolutional layer
        builder.addLayer(Layer.buildConvLayer(6, new Size(5, 5)));
        //build 1st sampling layer
        builder.addLayer(Layer.buildSampLayer(new Size(2, 2)));
        //build 2st convolutional layer
        builder.addLayer(Layer.buildConvLayer(12, new Size(5, 5)));
        //build 2st sampling layer
        builder.addLayer(Layer.buildSampLayer(new Size(2, 2)));
        //build output layer
        builder.addLayer(Layer.buildOutputLayer(10));

        //set the minbatch size is 50
        int batchSize=50;
        CNN cnn=new CNN(builder,batchSize);
        //4. train the image dataset, and repeat 3 iterations
        cnn.train(dataset, 3);
        Log.i("End to train the CNN model...");
        Log.i("----> 3. Saving the trained the CNN model...");
        String modelName = modelFile;
        cnn.saveModel(modelName);
        dataset.clear();
        dataset=null;
        Log.i("----> 4. Validate model with validataion dataset");
        Dataset valSet=Dataset.load(valPath,",",targetWidth *targetHeight);
        cnn.test(valSet);
        valSet.clear();
        valSet=null;
        Log.i("----> 5. All Done.");

       // CNN cnn1=CNN.loadModel(modelName);
       // Log.i(cnn1+"");

        //Dataset testset = Dataset.load(valPath, ",", -1);
        //cnn.predict(testset, "Data/test.predict");
    }
}
