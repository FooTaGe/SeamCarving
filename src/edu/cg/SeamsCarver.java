package edu.cg;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class SeamsCarver extends ImageProcessor {
	
	//MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage apply();
	}

	//MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;

	//TODO: Add some additional fields:
	private HashMap<Pixel, Boolean> removedPixels;
	private ImageArrayList costMatrix;
    private ImageArrayList gradientImage;
    private ImageArrayList tempImage;
    private ImageArrayList greyImage;


	//MARK: Constructor
	public SeamsCarver(Logger logger, BufferedImage workingImage,
			int outWidth, RGBWeights rgbWeights) {
		super(logger, workingImage, rgbWeights, outWidth, workingImage.getHeight()); 
		
		numOfSeams = Math.abs(outWidth - inWidth);
		
		if(inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");
		
		if(numOfSeams > inWidth/2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");
		
		//Sets resizeOp with an appropriate method reference
		if(outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if(outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;
		
		//TODO: Initialize your additional fields and apply some preliminary calculations:

		costMatrix = new ImageArrayList(inWidth, inHeight);
		removedPixels = new HashMap<>(inHeight * numOfSeams);
        greyImage = ImageArrayList.createFromImage(greyscale(workingImage));
	}
	
	//MARK: Methods
	public BufferedImage resize() {
		return resizeOp.apply();
	}
	
	//MARK: Unimplemented methods
	private BufferedImage reduceImageWidth() {
		calculateCostMatrix();

		for (int i = 0; i < numOfSeams ; i++) {
			removeMinSeam();
			updateCostMatrix();
		}

		return createReducedImage();
	}

    private BufferedImage createReducedImage() {
	    BufferedImage ans = newEmptyOutputSizedImage();
        for (int y = 0; y < tempImage.getHeight(); y++) {
            for (int x = 0; x < tempImage.getRowSize(y); x++) {
                Pixel currPixel = tempImage.get(x, y);
                int i = currPixel.x;
                int j = currPixel.y;
                ans.setRGB(x, y, workingImage.getRGB(i, j));
            }
        }
        return ans;
    }

    private void updateCostMatrix() {
		//ToDo 0_o effiecnt update
		int width = costMatrix.getRowSize(0);
		int height = costMatrix.getHeight();

        for (int i = 0; i < width; i++) {
            costMatrix.set(i, 0, gradientImage.get(i, 0).clone());
        }

        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long val = gradientImage.get(x, y).value + getMinParent(x, y);
                costMatrix.set(x, y, new Pixel(x, y, val));
            }
        }
	}

	private void removeMinSeam() {
		long minValue = Long.MAX_VALUE;
		int minPos = 0;

		for (int i = 0; i < tempImage.getRowSize(0); i++) {
			long currVal = costMatrix.get(i, tempImage.getHeight() - 1).value;
		    if (costMatrix.get(i, tempImage.getHeight() - 1).value < minValue) {
				minValue = costMatrix.get(i, tempImage.getHeight() - 1).value;
				minPos = i;
			}
		}

		// Add base to hashmap
		int x = minPos;
		Pixel currPixel;


        int[] father = {x, tempImage.getHeight() - 1};

		//Backtrack and add the rest
		while (father != null) {
            currPixel = tempImage.get(father[0], father[1]);
		    removedPixels.put(greyImage.get(currPixel.x, currPixel.y), true);
            int[] son = {father[0], father[1]};
		    father = backTrack(father);
            deleteAndUpdatePixel(son);
		}
	}

    private void deleteAndUpdatePixel(int[] pos) {
	    int x = pos[0];
	    int y= pos[1];
	    tempImage.remove(x, y);
	    gradientImage.remove(x, y);
	    costMatrix.remove(x, y);
	    if(x > 0){
            updateGradient(x-1, y);
        }
        if (x < tempImage.getRowSize(y) - 1){
            updateGradient(x, y);
        }


    }

    private void updateGradient(int x, int y) {
        int dx = 0;
        int dy = 0;
	    if (x == tempImage.getRowSize(y) - 1) {
            dx = (int) Math.pow(tempImage.get(x, y).value - tempImage.get(x - 1, y).value ,2);
        } else {
	        dx = (int) Math.pow(tempImage.get(x, y).value - tempImage.get(x + 1, y).value ,2);
        }

        if (y == tempImage.getHeight() - 1) {
            dy = (int) Math.pow(tempImage.get(x, y).value - tempImage.get(x, y - 1).value ,2);
        } else {
            dy = (int) Math.pow(tempImage.get(x, y).value - tempImage.get(x, y + 1).value ,2);
        }
        int value = (int) Math.sqrt((dx + dy) / 2);
        gradientImage.get(x, y).value = value;
    }

    private int[] backTrack(int[] son) {
		if (son[1] == 0) {
			return null;
		}
		int x = son[0];
		int y = son[1];
        long cv = 0;
        long cl = 0;
        long cr = 0;

		//each one is {x, y, value}

		long[] upParent = {x, y - 1, 0};
		long[] leftParent = {x - 1, y - 1, 0};
		long[] rightParent = {x + 1, y - 1, 0};
		long[] parent = new long[3];

        if(x > 0 && x < tempImage.getRowSize(y) - 1){
            cv = Math.abs(tempImage.get(x - 1, y).value - tempImage.get(x + 1, y).value);
            cl = cv + Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x - 1, y).value);
            cr = cv + Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x + 1, y).value);
        }
        else if(x == 0){
            cr = Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x + 1, y).value);
        }
        else if (x == tempImage.getRowSize(y) - 1){
            cl = Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x - 1, y).value);
        }

        upParent[2] = costMatrix.get(x, y - 1).value + gradientImage.get(x, y).value +  cv;
        leftParent[2] = x > 0 ? costMatrix.get(x - 1, y - 1).value + gradientImage.get(x, y).value + cl : Long.MAX_VALUE;
        rightParent[2] = x != tempImage.getRowSize(y) - 1 ? costMatrix.get(x + 1, y - 1).value + gradientImage.get(x, y).value + cr : Long.MAX_VALUE;

        long currValue = costMatrix.get(x, y).value;
        if(currValue == upParent[2]){
            int[] ans = {(int)upParent[0], (int)upParent[1]};
            return  ans;
        }
        else if(currValue == leftParent[2]){
            int[] ans = {(int)leftParent[0], (int)leftParent[1]};
            return  ans;
        }
        else{
            int[] ans = {(int)rightParent[0], (int)rightParent[1]};
            return  ans;
        }
	}

	private void calculateCostMatrix() {
        gradientImage = ImageArrayList.createFromImage(gradientMagnitude(workingImage));
        tempImage = ImageArrayList.createFromImage(greyscale(workingImage));
	    int width = tempImage.getRowSize(0);
	    int height = tempImage.getHeight();


	    //insert values of first row
        for (int i = 0; i < width; i++) {
			costMatrix.set(i, 0, gradientImage.get(i, 0).clone());
		}

		for (int y = 1; y < height; y++) {
			for (int x = 0; x < width; x++) {
				long val = gradientImage.get(x, y).value + getMinParent(x, y);
			    costMatrix.set(x, y, new Pixel(x, y, val));
			}
		}
	}

	private long getMinParent(int x, int y) {
		//TODO add C shtut
		int width = tempImage.getRowSize(y);
        long leftParent, upParent, rightParent, parent;
        long cv = 0;
        long cl = 0;
        long cr = 0;
		leftParent = x != 0 ? costMatrix.get(x - 1,y - 1).value : Long.MAX_VALUE;
		rightParent = x != (width - 1) ? costMatrix.get(x + 1, y - 1).value : Long.MAX_VALUE;
		upParent = costMatrix.get(x, y - 1).value;
		if(x > 0 && x < width - 1){
            cv = Math.abs(tempImage.get(x - 1, y).value - tempImage.get(x + 1, y).value);
            cl = cv + Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x - 1, y).value);
            cr = cv + Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x + 1, y).value);
        }
        else if(x == 0){
		    cr = Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x + 1, y).value);
        }
        else if (x == width - 1){
		    cl = Math.abs(tempImage.get(x, y - 1).value - tempImage.get(x - 1, y).value);
        }
        leftParent += cl;
        rightParent += cr;
        upParent += cv;

        // Find min
		parent = Math.min(leftParent, rightParent);
		parent = Math.min(parent, upParent);
		return parent;
	}


	private BufferedImage increaseImageWidth() {
		//TODO: Implement this method, remove the exception.
        calculateCostMatrix();

        for (int i = 0; i < numOfSeams; i++) {
            removeMinSeam();
            updateCostMatrix();
        }

        return createEnlargedImage();
    }

    private BufferedImage createEnlargedImage() {
        BufferedImage ans = newEmptyOutputSizedImage();
        ArrayList<ArrayList<Integer>> temp = new ArrayList<>(outHeight);
        for (int y = 0; y < workingImage.getHeight(); y++) {
            temp.add(new ArrayList<Integer>(outWidth));
            for (int x = 0; x < workingImage.getWidth(); x++) {
                temp.get(y).add(workingImage.getRGB(x, y));
                if (removedPixels.containsKey(greyImage.get(x, y))){
                    temp.get(y).add(workingImage.getRGB(x, y));
                }
            }
        }

        for (int y = 0; y < outHeight; y++) {
            for (int x= 0; x < outWidth; x++) {
                ans.setRGB(x, y, temp.get(y).get(x));
            }
        }
        return ans;
    }
	
	public BufferedImage showSeams(int seamColorRGB) {
        calculateCostMatrix();

        for (int i = 0; i < numOfSeams ; i++) {
            removeMinSeam();
            updateCostMatrix();
        }

        return createSeamsImage();
	}

    private BufferedImage createSeamsImage() {
        BufferedImage ans = newEmptyInputSizedImage();
        Color red = new Color(255, 0, 0);
        for (int y = 0; y < workingImage.getHeight(); y++) {
            for (int x = 0; x < workingImage.getWidth(); x++) {
                if (removedPixels.containsKey(greyImage.get(x, y))){
                    ans.setRGB(x, y, red.getRGB());
                }
                else {
                    ans.setRGB(x, y, workingImage.getRGB(x, y));
                }
            }
        }

        return ans;
    }
}
