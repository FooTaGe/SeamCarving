import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageArrayList {
    private ArrayList<ArrayList<Pixel>> image;
    private int m_height;
    private int m_width;

    public ImageArrayList (int width, int height){
        m_height = height;
        m_width = width;
        image = new ArrayList<>(width);
        for (int i = 0; i < m_width; i++) {
            image.set(i, new ArrayList<Pixel>(m_height));
        }
    }

    public static ImageArrayList createSizeOf (ImageArrayList sample){
        return new ImageArrayList(sample.getWidth(), sample.getHeight());
    }

    public static ImageArrayList createFromImage(BufferedImage i_image){
        ImageArrayList ans = new ImageArrayList(i_image.getWidth(), i_image.getHeight());
        for (int y = 0; y < i_image.getHeight(); y++) {
            for (int x = 0; x < i_image.getWidth(); x++) {
                ans.set(x, y, new Pixel(x, y, new Color(i_image.getRGB(x, y)).getBlue()));
            }
        }
        return ans;
    }


    public void set(int x, int y, Pixel i_pix) {
        if (x < m_width && y < m_height) {
            image.get(x).set(y, i_pix);
        } else {
            throw new ArrayIndexOutOfBoundsException("The 2D array was asked to get something out of bounds");
        }
    }
    public Pixel get(int x, int y){
        if(x < m_width && y < m_height){
            return image.get(x).get(y);
        }
        else {
            throw new ArrayIndexOutOfBoundsException("The 2D array was asked to get something out of bounds");
        }
    }
    public int getWidth(){
        return m_width;
    }

    public int getHeight(){
        return m_height;
    }
}
