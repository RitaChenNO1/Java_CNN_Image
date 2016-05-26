package image;

//import util.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 * Created by chzhenzh on 5/6/2016.
 * https://github.com/DiegoCatalano/Catalano-Framework/tree/master/Catalano.Image/src/Catalano/Imaging
 */
public class ImageDAO {
    public int THRESHOLD = 125;
    public int rgb[][];
    /**
     *
     * @param file  the source file with content: n15075141/n15075141_10011.JPEG 0
     * @param delimiter1 n15075141/n15075141_10011.JPEG 0 , split with blank
     * @param delimiter2 n15075141/n15075141_10011.JPEG 0 ,split with /
     * @param imagePath  the source path of images: /home/grid/RIM2/Datasets/ILSVRC2015_CLS/Annotations/CLS-LOC/train
     * @param targetWidth  zoom image to 28*28
     * @param targetHeight
     * @param save2File  save the pixel to file
     */
    public void imageClass(String file,String delimiter1,String delimiter2,String imagePath,int targetWidth, int targetHeight,String save2File
            ,int max_class,int rows_train,String save2TrainFile,String save2ValFile){

        try {
            BufferedReader in= null;
            in = new BufferedReader(new FileReader(file));
            String line;
            int imageClassID=-1;
            ImageDataset imageDataset=new ImageDataset();
            imageDataset.initRecord();
            //to find the class head
            String tmpClassName="";
            String previousimageClassName="";
            //set the rows of a class
            int innerClassID=0;
            //read the content in , line by line
            //eg line: n15075141/n15075141_10011.JPEG 0
            while ((line = in.readLine()) != null) {
                String[] datas=line.split(delimiter1);
                int data_len=datas.length;
                if(data_len==0){
                    continue;
                }
                String fileName=datas[0];
                //if it's the first image of a class, then set the image class ID
                // need to check the imageClassName

                //n15075141/n15075141_10011.JPEG
                String[] datas1=fileName.split(delimiter2);
                String imageClassName=datas1[0];
                String imageFileName=datas1[1];
                tmpClassName=imageClassName;
                //the counter for image class
                if(!tmpClassName.equals(previousimageClassName))
                {
                    imageClassID++;
                    innerClassID=0;
                }
                System.out.println("the class:"+imageClassID);
                //we don't need more than 10 classed, so go out
                if(imageClassID>=max_class && max_class!=-1)
                {
                    break;
                }
                innerClassID++;
                //get the pixel array
                String imagePixels=resize_Bin_Image(imagePath,fileName+".JPEG",targetWidth,targetHeight);
                ImageRecord record=new ImageRecord(imageClassName,imageFileName,innerClassID+"",imagePixels,imageClassID+"");
                imageDataset.add(record);
                //record it, before go to next one
                previousimageClassName=imageClassName;
            }
            if(max_class==-1){
                imageDataset.saveRecords2File(save2File,"PixelsAndLabel");
                //imageDataset.saveRecords2File(save2File,"Pixels");
            }else{
                imageDataset.saveRecords2TrainValFile(save2File, "PixelsAndLabel",max_class, rows_train, save2TrainFile,save2ValFile);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String resize_Bin_Image(String imagePath,String filename,int targetWidth, int targetHeight)
    {
        try {
            //Log.i(imagePath + filename);
            BufferedImage src = ImageIO.read(new File(imagePath+filename));  // get the source image
            Image image=src.getScaledInstance(targetWidth,targetHeight,Image.SCALE_DEFAULT);
            //Zoom the image
            //TYPE_INT_RGB
            //TYPE_BYTE_BINARY
            BufferedImage tag = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_BINARY);

            Graphics2D g = tag.createGraphics();
            // draw the small image into Graphics2D
            //here must make the image into Graphics, otherwise the RGB could not be get
            g.drawImage(image, 0, 0, null);
            g.dispose();

            /*
            OutputStream out = new FileOutputStream(targetfileName);
            ImageIO.write(tag, "JPEG", out);//
            out.close();
            */
            int width = tag.getWidth();
            int height = tag.getHeight();
            //System.out.println(width+":"+height);
            int x = tag.getMinX();
            int y = tag.getMinY();
            int sizex=width-x;
            int sizey=height-y;
            int allsize=sizex*sizey;
            int[] pixels=new int[allsize];
            this.rgb = new int[allsize][3];
            int[] pixcelsBinarize=new int[allsize];
            //step 4, store the pixels
            int type= tag.getType();

            //System.out.println(type);
            if ( type ==BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
                tag.getRaster().getDataElements(x, y, width, height, pixels );
            else
                tag.getRGB( x, y, width, height, pixels, 0, width );
            //step 5, store RGB from  the pixels
            //after binarize, it'll (0,0,0), or (255,255,255)
            for (int i = 0; i < allsize; i++) {
                rgb[i][0] = (pixels[i] & 0xff0000 ) >> 16 ;
                //rgb[i][1] = (pixels[i]  & 0xff00 ) >> 8 ;
                //rgb[i][2] = (pixels[i]  & 0xff );
                //System.out.println("i=" + i +  ":(" +rgb[i][0] + ","+ rgb[i][1] + "," + rgb[i][2] + ")");
                if(rgb[i][0]>THRESHOLD)
                {
                    pixcelsBinarize[i]=1;
                }
            }
            String pixels_str=Arrays.toString(pixcelsBinarize).replace("[","").replace("]","").replace(" ", "");
            //System.out.println(pixcelsBinarize.length);
            //System.out.println(pixels_str);
            /*
            PrintWriter pw=new PrintWriter(new File(dataFileName));
            pw.write(pixels_str+"\n");
            pw.flush();
            pw.close();*/
            return pixels_str;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /*
    public int[][] ImagePreprocess(String filename){
        File file = new File(filename);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //step 2, get the attribute of image
        int width = bi.getWidth();
        int height = bi.getHeight();
        System.out.println(width+":"+height);
        int x = bi.getMinX();
        int y = bi.getMinY();
        int sizex=width-x;
        int sizey=height-y;
        int allsize=sizex*sizey;
        //step 3,initial arrary to store the pixels and RGB
        int[] pixels=new int[allsize];
        this.rgb = new int[allsize][3];
        //step 4, store the pixels
        int type= bi.getType();
        //System.out.println(type);
        if ( type ==BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
            bi.getRaster().getDataElements(x, y, width, height, pixels );
        else
            bi.getRGB( x, y, width, height, pixels, 0, width );
        //step 5, store RGB from  the pixels
        for (int i = 0; i < allsize; i++) {
            rgb[i][0] = (pixels[i] & 0xff0000 ) >> 16 ;
            rgb[i][1] = (pixels[i]  & 0xff00 ) >> 8 ;
            rgb[i][2] = (pixels[i]  & 0xff );
            //System.out.println("i=" + i +  ":(" +rgb[i][0] + ","+ rgb[i][1] + "," + rgb[i][2] + ")");
        }
        return rgb;
    }
    */
/*
    public int [][] binarizeImage()
    {
        int[][] black_white=new int[][];
        return black_white;
    }
*/


}
