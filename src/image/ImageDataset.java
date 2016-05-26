package image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chzhenzh on 5/26/2016.
 */
public class ImageDataset {
    private List<ImageRecord> imageRecords;
    public void saveRecords2File(String filePath,String pixelAndLabel){
        //set the tag;
        int tag=1;
        switch (pixelAndLabel){
            case "PixelsAndLabel":
                tag=1;
                break;
            case "Pixels":
                tag=0;
                break;
            default:
                break;
        }

        try {
            PrintWriter pw=new PrintWriter(new File(filePath));
            Iterator<ImageRecord> iter=this.iter();
            while(iter.hasNext()) {
                ImageRecord record = iter.next();
                String imagePixels=record.getImagePixels();
                String imageClassID=record.getImageClassID();
                if(tag==0) {
                    pw.write(imagePixels  + "\n");
                }else{
                    pw.write(imagePixels + "," + imageClassID + "\n");
                }
            }
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveRecords2TrainValFile(String filePath,String pixelAndLabel,int max_class,int rows_train,String save2TrainFile,String save2ValFile){
        //set the tag;
        int tag=1;
        switch (pixelAndLabel){
            case "PixelsAndLabel":
                tag=1;
                break;
            case "Pixels":
                tag=0;
                break;
            default:
                break;
        }
        try {
            PrintWriter pw_train=new PrintWriter(new File(save2TrainFile));
            PrintWriter pw_val=new PrintWriter(new File(save2ValFile));
            Iterator<ImageRecord> iter=this.iter();
            while(iter.hasNext()) {
                ImageRecord record = iter.next();
                int imageClass=Integer.parseInt(record.getImageClassID());
                //we don't need more than 10 classed, so go out
                if(imageClass>=max_class)
                {
                    break;
                }
                String imagePixels=record.getImagePixels();
                String imageClassID=record.getImageClassID();
                int imageInnerClassID=Integer.parseInt(record.getImageInnerClassID());
                if(imageInnerClassID<=rows_train) {
                    if (tag == 0) {
                        pw_train.write(imagePixels + "\n");
                    } else {
                        pw_train.write(imagePixels + "," + imageClassID + "\n");
                    }
                }else{
                    if (tag == 0) {
                        pw_val.write(imagePixels + "\n");
                    } else {
                        pw_val.write(imagePixels + "," + imageClassID + "\n");
                    }
                }
            }
            pw_train.flush();
            pw_train.close();
            pw_val.flush();
            pw_val.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * clear the data  from memory
     */
    public void clear(){
        imageRecords.clear();
    }

    public Iterator<ImageRecord> iter() {
        return imageRecords.iterator();
    }

    public void add(ImageRecord ir)
    {
        this.imageRecords.add(ir);
    }

    public void initRecord()
    {
        this.imageRecords = new ArrayList<ImageRecord>();
    }

    public List<ImageRecord> getImageRecords() {
        return imageRecords;
    }

    public void setImageRecords(List<ImageRecord> imageRecords) {
        this.imageRecords = imageRecords;
    }
}
