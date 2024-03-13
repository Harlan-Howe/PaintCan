import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorButton extends JButton
{
    private Color myColor;
    private Image img;
    public static final int IMAGE_SIZE = 20;

    public ColorButton()
    {
        this(Color.RED);
    }

    public ColorButton(Color c)
    {
        super();
        myColor = c;
        img = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(myColor);
        g.fillRect(0,0,IMAGE_SIZE,IMAGE_SIZE);
        ImageIcon icon = new ImageIcon(img);
        this.setIcon(icon);
    }

    public Color getMyColor()
    {
        return myColor;
    }
}
