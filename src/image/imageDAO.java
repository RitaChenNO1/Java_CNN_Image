package image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by chzhenzh on 5/6/2016.
 * https://github.com/DiegoCatalano/Catalano-Framework/tree/master/Catalano.Image/src/Catalano/Imaging
 */
public class imageDAO {
    public int THRESHOLD = 160;
    public int rgb[][];
    public int black_white[][];

    public int[][] load_image(String filename){
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
/*
    public int [][] binarizeImage()
    {
        int[][] black_white=new int[][];
        return black_white;
    }
*/
}
