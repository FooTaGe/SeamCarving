import java.util.ArrayList;

public class ImageArrayList {
    private ArrayList<ArrayList> image;
    private int m_height;
    private int m_width;

    public ImageArrayList (int m_width, int m_height){

    }

    public static ImageArrayList createSizeOf (ImageArrayList sample){
        return new ImageArrayList(sample.getWidth(), sample.getHeight());
    }

    public int getWidth(){
        return m_width;
    }

    public int getHeight(){
        return m_height;
    }
}
