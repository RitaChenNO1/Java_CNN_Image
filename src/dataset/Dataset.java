package dataset;

import util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chzhenzh on 5/24/2016.
 */
public class Dataset {

    private List<Record> records;
    private int labelIndex;

    public static Dataset load(String filePath,String delimiter, int labelIndex )
    {
        Dataset dataset=new Dataset();
        //initial the records
        dataset.records = new ArrayList<Record>();
        dataset.labelIndex=labelIndex;
        File file=new File(filePath);
        //load file context into records
        try {
            BufferedReader in=new BufferedReader(new FileReader(file));
            String line;
            try {
                //read the content in , line by line
                while ((line = in.readLine()) != null) {
                    String[] datas=line.split(delimiter);
                    int data_len=datas.length;
                    if(data_len==0){
                        continue;
                    }
                    double[] data=new double[data_len];
                    for(int i=0;i<data_len;i++){
                        data[i]=Double.parseDouble(datas[i]);
                    }
                    Record record=new Record(data,labelIndex);
                    //add the new row to dataset
                    dataset.records.add(record);
                }
               // System.out.println();
                Log.i("It loaded rows "+dataset.records.size());
                //close file
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return dataset;
    }

    /**
     * get the size of dataset
     * @return
     */
    public int size(){
        return this.records.size();
    }

    public Record getRecord(int index)
    {
        return records.get(index);
    }
}



