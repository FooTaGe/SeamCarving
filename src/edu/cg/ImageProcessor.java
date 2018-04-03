package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {
	
	//MARK: Fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;
	
	//MARK: Constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage,
			RGBWeights rgbWeights, int outWidth, int outHeight) {
		super(); //Initializing for each loops...
		
		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}
	
	public ImageProcessor(Logger logger,
			BufferedImage workingImage,
			RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights,
				workingImage.getWidth(), workingImage.getHeight());
	}
	
	//MARK: Change picture hue - example
	public BufferedImage changeHue() {
		setForEachInputParameters();
	    logger.log("Prepareing for hue changing...");
		
		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;
		
		BufferedImage ans = newEmptyInputSizedImage();
		
		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r*c.getRed() / max;
			int green = g*c.getGreen() / max;
			int blue = b*c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});
		
		logger.log("Changing hue done!");
		
		return ans;
	}

	public BufferedImage greyscale(BufferedImage image){
		setForEachParameters(image.getWidth(), image.getHeight());
	    int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int weightSum = r + g + b;

		BufferedImage ans = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

		forEach((y, x) -> {
			Color c = new Color(image.getRGB(x, y));
			int red = r*c.getRed();
			int green = g*c.getGreen();
			int blue = b*c.getBlue();
			int greyColor = (red + green + blue) / weightSum;
			Color color = new Color(greyColor, greyColor, greyColor);
			ans.setRGB(x, y, color.getRGB());
		});

		return ans;
	}

	public BufferedImage greyscale() {
		logger.log("Prepareing for greyscaling...");
		BufferedImage ans = greyscale(workingImage);
		logger.log("Greyscaling done!");

		return ans;
	}

	public BufferedImage gradientMagnitude(BufferedImage image) {

	    setForEachParameters(image.getWidth(), image.getHeight());
        BufferedImage greyImage = greyscale(image);
		BufferedImage ans = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

		forEach((y, x) -> {
			Color c = new Color(greyImage.getRGB(x, y));
			int luma = c.getBlue();

			int nextXluma = getNextXLuma(x, y, greyImage);
			int nextYluma = getNextYLuma(x, y, greyImage);

			int xdiff = (int) Math.pow(luma - nextXluma, 2);
			int ydiff = (int) Math.pow(luma - nextYluma, 2);

			int magnitude = (int) Math.sqrt((xdiff + ydiff) / 2);

			Color color = new Color(magnitude, magnitude, magnitude);
			ans.setRGB(x, y, color.getRGB());
		});


		return ans;
	}

	public BufferedImage gradientMagnitude() {
		logger.log("Prepareing for gradient magnitude...");
		BufferedImage ans = gradientMagnitude(workingImage);
		logger.log("Gradient magnitude done!");

		return ans;
	}

	private int getNextXLuma(Integer x, Integer y, BufferedImage image) {
		if (x == image.getWidth() - 1) {
			Color c = new Color(image.getRGB(x - 1, y));
			return c.getBlue();
		} else {
			Color c = new Color(image.getRGB(x + 1, y));
			return c.getBlue();
		}
	}

	private int getNextYLuma(Integer x, Integer y, BufferedImage image) {
		if (y == image.getHeight() - 1) {
			Color c = new Color(image.getRGB(x, y - 1));
			return c.getBlue();
		} else {
			Color c = new Color(image.getRGB(x, y + 1));
			return c.getBlue();
		}
	}

	public BufferedImage nearestNeighbor() {
	    setForEachInputParameters();
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("nearestNeighbor");
	}
	
	public BufferedImage bilinear() {
	    setForEachInputParameters();
		//TODO: Implement this method, remove the exception.
		logger.log("applies bilinear interpolation.");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int weightSum = r + g + b;

		BufferedImage ans = newEmptyOutputSizedImage();

		double xScale = (double) (inWidth - 1) / (outWidth - 1);
		double yScale = (double) (inHeight - 1) / (outHeight - 1);

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				//converting to original image relative position
				double xFit = xScale * x;
				double yFit = yScale * y;
				int xLeft = (int) Math.floor(xFit);
				int xRight = (int) Math.ceil(xFit);
				int yUp = (int) Math.ceil(yFit);
				int yDown = (int) Math.floor(yFit);

				Color bilinearC = getLinear(xFit, yFit, xLeft, xRight, yUp, yDown);
				ans.setRGB(x, y, bilinearC.getRGB());
			}

		}


		logger.log("Bi done!");

		return ans;
	}

	private Color getLinear(double x, double y, int xLeft, int xRight, int yUp, int yDown) {
		double xWeight1 = (xRight - x) / (xRight - xLeft);
		double xWeight2 = (x - xLeft) / (xRight - xLeft);
		double yWeight1 = (yUp - y) / (yUp - yDown);
		double yWeight2 = (y - yDown) / (yUp - yDown);

		Color Q11 = new Color(workingImage.getRGB(xLeft, yDown));
		Color Q21 = new Color(workingImage.getRGB(xRight, yDown));
		Color Q12 = new Color(workingImage.getRGB(xLeft, yUp));
		Color Q22 = new Color(workingImage.getRGB(xRight, yUp));

		double rx1 = xWeight1 * Q11.getRed() + xWeight2 * Q21.getRed();
		double gx1 = xWeight1 * Q11.getGreen() + xWeight2 * Q21.getGreen();
		double bx1 = xWeight1 * Q11.getBlue() + xWeight2 * Q21.getBlue();

		double rx2 = xWeight1 * Q12.getRed() + xWeight2 * Q22.getRed();
		double gx2 = xWeight1 * Q12.getGreen() + xWeight2 * Q22.getGreen();
		double bx2 = xWeight1 * Q12.getBlue() + xWeight2 * Q22.getBlue();

		int r = (int) (yWeight1 * rx1 + yWeight2 * rx2);
		int g = (int) (yWeight1 * gx1 + yWeight2 * gx2);
		int b = (int) (yWeight1 * bx1 + yWeight2 * bx2);

		return new Color(r, g, b);
	}


	//MARK: Utilities
	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}

	public final void setForEachInputParameter(int width, int height){
	    setForEachParameters(width, height);
    }
	
	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}
	
	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}
	
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();
		
		forEach((y, x) -> 
			output.setRGB(x, y, workingImage.getRGB(x, y))
		);
		
		return output;
	}
}
