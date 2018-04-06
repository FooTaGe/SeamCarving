package edu.cg.menu;

public class Pixel {
    public long value;
    public int  x;
    public int y;

    public Pixel(int i_x, int i_y, long i_val){
        x = i_x;
        y = i_y;
        value = i_val;
    }

    public Pixel clone(){
        return new Pixel(this.x, y, this.value);
    }
}
