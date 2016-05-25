package cnn;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chzhenzh on 5/24/2016.
 */
public class LayerBuilder {
    public List<Layer> mLayers;

    public LayerBuilder(){
        mLayers=new ArrayList<Layer>();
    }

    public LayerBuilder(Layer l){
        this();
        mLayers.add(l);
    }

    public LayerBuilder addLayer(Layer l){
        mLayers.add(l);
        return this;
    }

}
