package image;

/**
 * Created by chzhenzh on 5/26/2016.
 */
public class ImageRecord {

    private String imageClassName;
    private String imageFileName;
    private String imageInnerClassID;
    private String imagePixels;
    private String imageClassID;



    public ImageRecord(String imageClassName,String imageFileName,String imageInnerClassID,String imagePixels,String imageClassID)
    {
        this.imageClassName=imageClassName;
        this.imageFileName=imageFileName;
        this.imageInnerClassID=imageInnerClassID;
        this.imagePixels=imagePixels;
        this.imageClassID=imageClassID;
    }

    //*********************************Start 1. generate the getter and setter*******************************

    public String getImageClassName() {
        return imageClassName;
    }

    public void setImageClassName(String imageClassName) {
        this.imageClassName = imageClassName;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getImagePixels() {
        return imagePixels;
    }

    public void setImagePixels(String imagePixels) {
        this.imagePixels = imagePixels;
    }

    public String getImageClassID() {
        return imageClassID;
    }

    public void setImageClassID(String imageClassID) {
        this.imageClassID = imageClassID;
    }

    public String getImageInnerClassID() {
        return imageInnerClassID;
    }

    public void setImageInnerClassID(String imageInnerClassID) {
        this.imageInnerClassID = imageInnerClassID;
    }
//*********************************End 1. generate the getter and setter*******************************



}
