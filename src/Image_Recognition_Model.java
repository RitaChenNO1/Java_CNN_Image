import cnn.CNN;
import cnn.Layer;
import cnn.LayerBuilder;
import cnn.Size;
import dataset.Dataset;

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
public class Image_Recognition_Model {

    public static void main(String args[]){

        //1. split to train dataset, validation dataset, test dataset

        //GPU?

        //load images

        // HOG (Histogram of Oriented Gradients) features

        //reside the image to N*N

        //CNN to learn the training dataset
        //a pixel belong to an object, set it to 1, otherwise 0;
        //for the same kind of objects, use CNN to learn

        // masks: full,bottom, top, left and right halves

        //output of the image object is d*d


        //**********************************************************
        //1.load the data from Data/train.format
        String filePath="Data/train.format";
        Dataset dataset=Dataset.load(filePath,",",748);
        //2. build all layers of CNN
        LayerBuilder builder=new LayerBuilder();
        //build input layer
        builder.addLayer(Layer.buildInputLayer(new Size(28, 28)));
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

        //train the image dataset, and repeat 3 iterations
        cnn.train(dataset,3);
    }
}
