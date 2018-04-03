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
	private HashMap<int[], Boolean> removedPixels;
	long[][] costMatrix;
	private BufferedImage tempImage;
	private BufferedImage gradientImage;

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

		costMatrix = new long[inWidth][inHeight];
		removedPixels = new HashMap<int[], Boolean>(inHeight * numOfSeams);
		tempImage = workingImage;
	}
	
	//MARK: Methods
	public BufferedImage resize() {
		return resizeOp.apply();
	}
	
	//MARK: Unimplemented methods
	private BufferedImage reduceImageWidth() {
		//TODO: Implement this method, remove the exception.
		calculateCostMatrix();

		for (int i = 0; i < numOfSeams ; i++) {
			findMinSeam();
			updateTempImage();
			updateCostMatrix();
		}

		return tempImage;
	}

	private void updateCostMatrix() {
		//ToDo 0_o effiecnt update
		costMatrix = new long[tempImage.getWidth()][ tempImage.getHeight()];
		calculateCostMatrix();
	}

	private void updateTempImage() {
		BufferedImage newImage = new BufferedImage(tempImage.getWidth() - 1, tempImage.getHeight(), tempImage.getType());
		boolean afterSeam = false;
		for (int y = 0; y < tempImage.getHeight(); y++) {
			for (int x = 0; x < tempImage.getWidth() - 1; x++) {
				int[] pos = {x, y};
				if(removedPixels.containsKey(pos)) {
					afterSeam = true;

				}
				int newX = afterSeam ? x + 1 : x;
				newImage.setRGB(x, y, tempImage.getRGB(newX, y));
			}
			afterSeam = false;
		}
		tempImage = newImage;
	}

	private void findMinSeam() {
		long minValue = Long.MAX_VALUE;
		int minPos = 0;

		for (int i = 0; i < tempImage.getWidth(); i++) {
			if (costMatrix[i][tempImage.getHeight() - 1] < minValue) {
				minValue = costMatrix[i][tempImage.getHeight() - 1];
				minPos = i;
			}
		}
		int x = minPos;
		int[] father = {x, tempImage.getHeight() - 1};
		removedPixels.put(father, true);

		while (father != null) {
			father = backTrack(father);
			removedPixels.put(father, true);
		}
	}

	private int[] backTrack(int[] father) {
		if (father[1] == 0) {
			return null;
		}
		int x = father[0];
		int y = father[1];

		//each one is {x, y, value}

		long[] upParent = {x, y - 1, 0};
		long[] leftParent = {x - 1, y - 1, 0};
		long[] rightParent = {x + 1, y - 1, 0};
		long[] parent = new long[3];


		leftParent[2] = x != 0 ? costMatrix[x - 1][y - 1] : Long.MAX_VALUE;
		rightParent[2] = x != (tempImage.getWidth() - 1) ? costMatrix[x + 1][y - 1] : Long.MAX_VALUE;
		upParent[2] = costMatrix[x][y - 1];
		parent = leftParent[2] < rightParent[2] ? leftParent : rightParent;
		parent = parent[2] < upParent[2] ? parent : upParent;

		int[] fatherLoc = {(int) parent[0], (int) parent[1]};

		return fatherLoc;
	}

	private void calculateCostMatrix() {
		//insert values of first row
		gradientImage = gradientMagnitude(tempImage);
		for (int i = 0; i < tempImage.getWidth(); i++) {
			costMatrix[i][0] = getGradient(i, 0);
		}
		for (int y = 1; y < tempImage.getHeight(); y++) {
			for (int x = 0; x < tempImage.getWidth(); x++) {
				costMatrix[x][y] = getGradient(x, y) + getMinParent(x, y);

			}
		}
	}

	private long getMinParent(int x, int y) {
		//TODO add C shtut
		long leftParent, upParent, rightParent, parent;
		leftParent = x != 0 ? costMatrix[x - 1][y - 1] : Long.MAX_VALUE;
		rightParent = x != (tempImage.getWidth() - 1) ? costMatrix[x + 1][y - 1] : Long.MAX_VALUE;
		upParent = costMatrix[x][y - 1];
		parent = Math.min(leftParent, rightParent);
		parent = Math.min(parent, upParent);
		return parent;
	}

	private int getGradient (int x, int y) {
		return new Color(gradientImage.getRGB(x, y)).getBlue();
	}

	private BufferedImage increaseImageWidth() {
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("increaseImageWidth");
	}
	
	public BufferedImage showSeams(int seamColorRGB) {
		//TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}
}
