import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;


public class ColorFillPanel extends JPanel
{
    private Color activeColor;
    private BufferedImage startImage;
    private BufferedImage workingImage;
    private File lastFile = null;

    public ColorFillPanel()
    {
        super();
        activeColor = Color.RED;

    }

    public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
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
        repaint();
    }
}
