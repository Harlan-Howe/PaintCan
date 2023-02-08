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
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {

    }

    @Override
    public void mouseExited(MouseEvent e)
    {

    }
}
