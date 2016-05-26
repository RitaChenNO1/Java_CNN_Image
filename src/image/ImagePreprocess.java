package image;
/**
 * Created by chzhenzh on 4/27/2016.
 */

public class ImagePreprocess {

    public static void main(String args[]) {

        ImageDAO i=new ImageDAO();
        //for local test
       // String imagePath="src/";
        //String file="Data/train.txt";
        //String save2File="Data/train0.format";
        //for centos image-net data
        String imagePath="/home/grid/RIM2/Datasets/ILSVRC2015_CLS/Data/CLS-LOC/train/";
        String file="/home/grid/RIM2/Datasets/ILSVRC2015_CLS/ImageSets/CLS-LOC/train_cls.txt";
        String save2File="/home/grid/Rita/java/Data/all.format";
        int max_class=10;
        int rows_train=1000;
        String save2TrainFile="/home/grid/Rita/java/Data/train_imagenet.format";
        String save2ValFile="/home/grid/Rita/java/Data/val_imagenet.format";

        String delimiter1=" ";
        //the / need to be encoded
        String delimiter2="\\/";
        int targetWidth=28;
        int targetHeight=28;
        //save all image as 0/1 binary format
        //i.imageClass(file,delimiter1, delimiter2, imagePath, targetWidth, targetHeight, save2File,-1,rows_train,,save2TrainFile,save2ValFile);

        //save 10 class, and 1000 as train, rest as test
        i.imageClass(file,delimiter1, delimiter2, imagePath, targetWidth, targetHeight, save2File,max_class,rows_train,save2TrainFile,save2ValFile);

    }
}
