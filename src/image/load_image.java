package image; /**
 * Created by chzhenzh on 4/27/2016.
 */

public class load_image {

    public static void main(String args[]) {
        //step 1, read image file to buffer
        String filename="src/n15075141_10011.JPEG";
        imageDAO i=new imageDAO();
        int[][] rgb=i.load_image(filename);
        System.out.println(rgb.length);
    }
}
