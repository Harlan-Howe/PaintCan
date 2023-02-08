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
    }
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        synchronized (workingImageMutex)
        {
            g.setColor(activeColor);
            g.fillRect(0,0, getWidth(), 2);
            if (workingImage != null)
                g.drawImage(myCanvas, 0, 2, this);
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
}
