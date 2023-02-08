import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ColorFillFrame extends JFrame implements ActionListener
{
    private Box colorPanel;
    private ColorFillPanel mainPanel;
    private JButton addButton;
    private ArrayList<ColorButton> colorButtons;
    private JColorChooser chooser;

    public static final Color[] startColors = {Color.RED, Color.GREEN, Color.BLUE};


    public ColorFillFrame()
    {
        super("Color Fill");
        getContentPane().setLayout(new BorderLayout());
        buildColorPanel();
        mainPanel = new ColorFillPanel();
        getContentPane().add(colorPanel, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        setSize(800,800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void buildColorPanel()
    {
        colorPanel = Box.createVerticalBox();
        BufferedImage img = new BufferedImage(ColorButton.IMAGE_SIZE, ColorButton.IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,ColorButton.IMAGE_SIZE, ColorButton.IMAGE_SIZE);
        g.setColor(Color.BLACK);
        g.drawLine(2, ColorButton.IMAGE_SIZE/2,
                   ColorButton.IMAGE_SIZE-2,ColorButton.IMAGE_SIZE/2);
        g.drawLine(ColorButton.IMAGE_SIZE/2,ColorButton.IMAGE_SIZE-2,
                ColorButton.IMAGE_SIZE/2, 2);

        addButton = new JButton(new ImageIcon(img));
        colorPanel.add(addButton);
        addButton.addActionListener(this);
        colorButtons = new ArrayList<ColorButton>();
        for (Color c: startColors)
        {
            ColorButton cb = new ColorButton(c);
            colorButtons.add(cb);
            colorPanel.add(cb);
            cb.addActionListener(this);
        }
    }

    public void makeNewButton()
    {
        if (chooser == null)
            chooser = new JColorChooser();
        int result = JOptionPane.showConfirmDialog(this, chooser);
        System.out.println(result);
        if (result == JOptionPane.OK_OPTION)
        {
            System.out.println("making button.");
            Color c = chooser.getColor();
            ColorButton cb = new ColorButton(c);
            colorButtons.add(cb);
            colorPanel.add(cb);
            cb.addActionListener(this);
            colorPanel.revalidate();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == addButton)
            makeNewButton();
        else
        {
            Color c = ((ColorButton) e.getSource()).getMyColor();
            mainPanel.setActiveColor(c);
            repaint();
        }
    }
}
