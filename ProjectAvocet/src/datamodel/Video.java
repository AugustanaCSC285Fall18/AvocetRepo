package datamodel;

import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {
	
	private String filePath;
	private transient VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds; 
	private Point origin = new Point();
	
		
	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		//fill in some reasonable default/starting values for several fields
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames()-1;
		
		int frameWidth = (int)vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int frameHeight = (int)vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		this.arenaBounds = new Rectangle(0,0,frameWidth,frameHeight);
	}
	
	public synchronized void setCurrentFrameNum(int seekFrame) {
		vidCap.set(Videoio.CV_CAP_PROP_POS_FRAMES, (double) seekFrame);
	}
	public synchronized int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CV_CAP_PROP_POS_FRAMES);
	}
	
	public synchronized Mat readFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	/** 
	 * @return frames per second
	 */
	public synchronized double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}
	public synchronized int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}

	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}
		
	public int getStartFrameNum() {
		return startFrameNum;
	}
	
	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public double getXPixelsPerCm() {
		return xPixelsPerCm;
	}

	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm)/2;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}
	
	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}

	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}
	
	public synchronized int getFrameWidth() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}

	public synchronized int getFrameHeight() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}
	
	synchronized void connectVideoCapture() throws FileNotFoundException {
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}
	
	public void setOriginX(double x) {
		origin.x = x;
	}
	
	public void setOriginY(double y) {
		origin.y = y;
	}
	
	public Point getOrigin() {
		return origin;
	}


}