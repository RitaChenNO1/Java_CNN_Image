package dataset;

import java.util.Arrays;

/**
 * Created by chzhenzh on 5/24/2016.
 * Record data sample:
 * the last column is image class label, before it, they are the binarized pixels
 * 0,0,1,0,1,1,0,8
 */
public class Record {
    private double[] attr;
    private double label;

    //get the every row from data, and set it to the Record
    public Record(double[]data,int labelIndex){
        int data_len=data.length;
        //since the predict data, don't have label
        if(labelIndex==-1){
            this.attr=data;
        }else{
            this.label=data[labelIndex];
            //the label could be the 0 column, or the last column
            if(label==0){
                this.attr= Arrays.copyOfRange(data,1,data_len);
            }else{
                this.attr=Arrays.copyOfRange(data,0,data_len-1);
            }
        }
    }

    //*********************************** set and get for all variables***********************************
    public double[] getAttr() {
        return attr;
    }

    public void setAttr(double[] attr) {
        this.attr = attr;
    }

    public double getLabel() {
        return label;
    }

    public void setLabel(double label) {
        this.label = label;
    }
    //*********************************** set and get for all variables***********************************
}
