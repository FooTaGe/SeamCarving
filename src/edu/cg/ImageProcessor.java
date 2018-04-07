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
        logger.log("applies nearest neighbor.");

        setForEachOutputParameters();
        double xScale = (double) (inWidth - 1) / (outWidth - 1);
        double yScale = (double) (inHeight - 1) / (outHeight - 1);

		BufferedImage ans = newEmptyOutputSizedImage();

		forEach((y, x) -> {
		    int oldX = (int) Math.round(xScale * x);
		    int oldY = (int) Math.round(yScale * y);
			ans.setRGB(x, y, workingImage.getRGB(oldX, oldY));
		});

        logger.log("nearest neighbor done.");
        return ans;
	}
	
	public BufferedImage bilinear() {
	    setForEachOutputParameters();
		//TODO: Implement this method, remove the exception.
		logger.log("applies bilinear interpolation.");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int weightSum = r + g + b;

		BufferedImage ans = newEmptyOutputSizedImage();


		double xScale = (double) (inWidth - 1) / (outWidth - 1);
		double yScale = (double) (inHeight - 1) / (outHeight - 1);

        forEach((y, x) -> {
            int xLeft = 0;
            int xRight = 0;
            int yUp = 0;
            int yDown = 0;
            //converting to original image relative position
            double xFit = xScale * x;
            double yFit = yScale * y;
            if (xFit != inWidth - 1) {
                xLeft = (int) Math.floor(xFit);
                xRight = (int) Math.floor(xFit + 1);
            } else {
                xLeft = (int) xFit - 1;
                xRight = (int) xFit;
            }
            if (yFit != inHeight - 1) {
                yUp = (int) Math.floor(yFit);
                yDown = (int) Math.floor(yFit + 1);
            } else {
                yUp = (int) yFit - 1;
                yDown = (int) yFit;
            }

            Color bilinearC = getLinear(xFit, yFit, xLeft, xRight, yUp, yDown);
            ans.setRGB(x, y, bilinearC.getRGB());
        });


		logger.log("Bi done!");

		return ans;
	}

	private Color getLinear(double x, double y, int xLeft, int xRight, int yUp, int yDown) {
		double xWeight1 = (xRight - x) / (xRight - xLeft);
		double xWeight2 = (x - xLeft) / (xRight - xLeft);
		double yWeight1 = (yUp - y) / (yUp - yDown);
		double yWeight2 = (y - yDown) / (yUp - yDown);

		Color leftDown = new Color(workingImage.getRGB(xLeft, yDown));
		Color rightDown = new Color(workingImage.getRGB(xRight, yDown));
		Color leftUp = new Color(workingImage.getRGB(xLeft, yUp));
		Color rightUp = new Color(workingImage.getRGB(xRight, yUp));


		double xYDownR = xWeight1 * leftDown.getRed() + xWeight2 * rightDown.getRed();
		double xYDownG = xWeight1 * leftDown.getGreen() + xWeight2 * rightDown.getGreen();
		double xYDownB = xWeight1 * leftDown.getBlue() + xWeight2 * rightDown.getBlue();


		double xYUpR = xWeight1 * leftUp.getRed() + xWeight2 * rightUp.getRed();
		double xYUpG = xWeight1 * leftUp.getGreen() + xWeight2 * rightUp.getGreen();
		double xYUpB = xWeight1 * leftUp.getBlue() + xWeight2 * rightUp.getBlue();

		int r = (int) (yWeight1 * xYDownR + yWeight2 * xYUpR);
		int g = (int) (yWeight1 * xYDownG + yWeight2 * xYUpG);
		int b = (int) (yWeight1 * xYDownB + yWeight2 * xYUpB);



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
