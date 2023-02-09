import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;


public class ColorFillPanel extends JPanel implements MouseListener
{
    private Color activeColor;
    private BufferedImage startImage;
    private BufferedImage workingImage;
    private int startX, startY;
    private FillThread fillingThread;
    private double thresholdSquared;

    private Object workingImageMutex;

    public ColorFillPanel()
    {
        super();
        activeColor = Color.RED;
        workingImageMutex = new Object();
        addMouseListener(this);
    }

    public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
        repaint();
    }

    public void setStartImage(BufferedImage startImage)
    {
        this.startImage = startImage;
        resetWorkingImage();
        setPreferredSize(new Dimension(startImage.getWidth()+2, startImage.getHeight()));
    }

    public void setThreshold(double threshold)
    {
        thresholdSquared = Math.pow(threshold,2);
        System.out.println("Threshold squared is now "+thresholdSquared);
    }

    /**
     * determines whether the two given colors have a color distance less than the threshold.
     * @param c1
     * @param c2
     * @return whether colorDistance(c1,c2) <= threshold
     */
    public boolean colorMatch(Color c1, Color c2)
    {
        return Math.pow(c1.getRed()-c2.getRed(),2)+
                Math.pow(c1.getGreen()-c2.getGreen(),2)+
                Math.pow(c1.getBlue()-c2.getBlue(),2) <= thresholdSquared;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        synchronized (workingImageMutex)
        {
            g.setColor(activeColor);
            g.fillRect(getWidth()-2,0, 2, getHeight());
            if (workingImage != null)
                g.drawImage(workingImage, 0, 0, this);
        }
    }

    /**
     * deepcopies startImage into workingImage, thus resetting the image onscreen back to the original. (synchronized)
     */
    public void resetWorkingImage()
    {
        synchronized (workingImageMutex)
        {
            workingImage = deepcopy(startImage);
        }
        repaint();
    }

    /**
     * Deep copies the given BufferedImage's data into a new memory bank. Changing one image will not change the other.
     * @param bufferImage - the image to copy
     * @return the deep copy of this image.
     * @author https://bytenota.com/java-cloning-a-bufferedimage-object/
     */
    public static BufferedImage deepcopy(BufferedImage bufferImage) {
        ColorModel colorModel = bufferImage.getColorModel();
        WritableRaster raster = bufferImage.copyData(null);
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    /**
     * finds the color of the pixel at (x, y) in workingImage. If (x, y) is out of bounds, throws a RuntimeException.
     * @param x
     * @param y
     * @return Color of pixel in working image at (x, y)
     * Note: locks mutex for workingImage, so this will wait for another thread to unlock mutex.
     */
    public Color getColorAt(int x, int y)
    {

        if (x>=0 && x<workingImage.getWidth() && y >=0 && y<workingImage.getHeight())
            synchronized (workingImageMutex)
            {
                return new Color(workingImage.getRGB(x, y));
            }
        else
            throw new RuntimeException("You attempted to get color for a point ("+x+", "+y+") that is out of bounds.");
    }

    /**
     * changes the color of the pixel at (x, y) in workingImage to the given color. If (x, y) is out of bounds, throws
     * a RuntimeException.
     * @param x
     * @param y
     * @param c - Color to put at (x, y).
     * Note: locks mutex for workingImage, so this will wait for another thread to unlock mutex.
     */
    public void setColorAt(int x, int y, Color c)
    {
        if (x>=0 && x<workingImage.getWidth() && y >=0 && y<workingImage.getHeight())
            synchronized (workingImageMutex)
            {
                workingImage.setRGB(x, y, c.getRGB());
            }
        else
            throw new RuntimeException("You attempted to set color for a point ("+x+", "+y+") that is out of bounds.");
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {

    }

    @Override
    public void mousePressed(MouseEvent e)
    {

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        System.out.println("Released at: ("+e.getX()+", "+e.getY()+").");
        if (fillingThread == null)
        {
            fillingThread = new FillThread();
            fillingThread.start();
        }
        startX = e.getX();
        startY = e.getY();
        fillingThread.startFillProcess();
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {

    }

    @Override
    public void mouseExited(MouseEvent e)
    {

    }

    class FillThread extends Thread
    {
        private boolean shouldInterrupt;
        private boolean doingFillProcess;


        public FillThread()
        {
            shouldInterrupt = false;
            doingFillProcess = false;
        }

        public void interrupt() { shouldInterrupt = true;}
        public void startFillProcess() {doingFillProcess = true;}
        public boolean isDoingFillProcess() {return doingFillProcess;}


        public void run() // this is what gets called when we tell this thread to start(). You should _NEVER_ call run()
                         // directly.
        {
            System.out.println("fill thread has started.");
            while(true)
            {
                if (workingImage != null && doingFillProcess)
                {
                    System.out.println("Initiating FillProcess.");
                    if (startX<0 || startX >=workingImage.getWidth() ||startY < 0 || startY >= workingImage.getHeight())
                    {
                        System.out.println("Start point was out of bounds. Resetting. " +
                                "(This might happen when we are selecting a file.");
                        doingFillProcess = false;
                        continue;
                    }
                    Color startColor = getColorAt(startX, startY);
                    System.out.println("Starting color: "+startColor);

                    fillWithColor(startX, startY, startColor);

                    if (!shouldInterrupt)
                        doingFillProcess = false; // we finished without being interrupted.
                }
                try
                {
                    Thread.sleep(250); // wait a quarter second before you consider running again.
                }catch (InterruptedException iExp)
                {
                    iExp.printStackTrace();
                }
                shouldInterrupt = false; // reset this to try again, if needed.
            }
        }


        public void fillWithColor(int x, int y, Color colorToReplace)
        {
            // mandatory base case #0 - if we've been interrupted.
            if (shouldInterrupt)
                return; // bail out if we need to cancel and leave.

           // System.out.println("fill with color "+activeColor+" replacing "+colorToReplace+" at ("+x+", "+y+").");

            //TODO: write this part of the program! Consider base case(s) and then write the program that changes the
            // color of the pixel and makes the recursive call.
            if (x<0 || x>=workingImage.getWidth() || y<0 || y>= workingImage.getHeight())
                return;

            if (!colorMatch(colorToReplace,getColorAt(x,y)))
                return;

            setColorAt(x,y,activeColor);


            for (int i=-1; i<2; i++)
                for (int j=-1; j<2; j++)
                {
                    if (i==0 && j==0)
                        continue;
//                    if (x+i<0 || x+i>=workingImage.getWidth() || y+j < 0 || y+j >= workingImage.getHeight())
//                        continue;
//                    if (colorMatch(colorToReplace, getColorAt(x+i, y+j)))
                        fillWithColor(x+i, y+j, colorToReplace);
                }

        }

    }

}
