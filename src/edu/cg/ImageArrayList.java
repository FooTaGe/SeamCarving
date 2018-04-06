package edu.cg;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageArrayList {
    private ArrayList<ArrayList<Pixel>> image;


    public ImageArrayList (int width, int height){
        image = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            image.add(i, new ArrayList<Pixel>(width));
            for (int j = 0; j < width; j++) {
                image.get(i).add(new Pixel(j, i, 0));
            }
        }
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
        image.get(y).set(x, i_pix);
    }

    public Pixel get(int x, int y){
        return image.get(y).get(x);
    }

    public int getRowSize(int y){
        return image.get(y).size();
    }

    public int getHeight(){
        return image.size();
    }

    public void remove(int x, int y){
        image.get(y).remove(x);
    }

}
