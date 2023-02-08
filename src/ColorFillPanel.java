import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;


public class ColorFillPanel extends JPanel
{
    private Color activeColor;
    private BufferedImage startImage;
    private BufferedImage workingImage;
    private File lastFile = null;

    private Object workingImageMutex;

    public ColorFillPanel()
    {
        super();
        activeColor = Color.RED;
        workingImageMutex = new Object();
    }

    public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
        repaint();
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
    public void doLoadImage()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select an Image");
        if (lastFile!= null)
            chooser.setSelectedFile(lastFile);
        String[] extensions = {"jpg","gif","jpeg","png"};
        chooser.setFileFilter(new FileNameExtensionFilter("images",extensions));
        int result = chooser.showOpenDialog(this);
        if (JFileChooser.APPROVE_OPTION == result)
        {
            lastFile = chooser.getSelectedFile();
            String fileName = lastFile.getPath();
            startImage = ((BufferedImage)(new ImageIcon(fileName).getImage()));
        }
        resetWorkingImage();
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

}
