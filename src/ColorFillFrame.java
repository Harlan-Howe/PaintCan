import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class ColorFillFrame extends JFrame implements ActionListener, ChangeListener
{
    private Box colorPanel, thresholdPanel;
    private ColorFillPanel mainPanel;
    private JButton addButton;
    private ButtonGroup colorButtonGroup;
    private ArrayList<ColorButton> colorButtons;
    private JColorChooser chooser;
    private File lastFile = null;
    private JMenuItem openMenuItem;
    private JSlider thresholdSlider;

    public static final Color[] startColors = {Color.RED, Color.GREEN, Color.BLUE};
    public static final int MIN_THRESHOLD = 0;
    public static final int MAX_THRESHOLD = 255;

    public ColorFillFrame()
    {
        super("Color Fill");
        getContentPane().setLayout(new BorderLayout());
        buildMenu();
        buildColorPanel();
        buildThresholdPanel();
        mainPanel = new ColorFillPanel();
        getContentPane().add(colorPanel, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(thresholdPanel, BorderLayout.EAST);
        setSize(800,800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void buildMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(this);
        fileMenu.add(openMenuItem);

        this.setJMenuBar(menuBar);
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
            addColorButton(c);
        }
        colorButtons.get(0).setSelected(true);
    }

    public void buildThresholdPanel()
    {
        thresholdPanel = Box.createVerticalBox();
        thresholdSlider = new JSlider(JSlider.VERTICAL, MIN_THRESHOLD, MAX_THRESHOLD, 1);
        thresholdSlider.addChangeListener(this);
        thresholdSlider.setMajorTickSpacing(16);
        thresholdSlider.setPaintTicks(true);
        thresholdPanel.add(new JLabel("Threshold"));
        thresholdPanel.add(new JLabel(""+MAX_THRESHOLD));
        thresholdPanel.add(thresholdSlider);
        thresholdPanel.add(new JLabel(""+MIN_THRESHOLD));

        for (Component jc: thresholdPanel.getComponents())
        {
            ((JComponent)jc).setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    public void addColorButton(Color c)
    {
        ColorButton cb = new ColorButton(c);
        colorButtons.add(cb);
        colorPanel.add(cb);
        cb.addActionListener(this);
        if (colorButtonGroup == null)
            colorButtonGroup = new ButtonGroup();
        colorButtonGroup.add(cb);
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
            addColorButton(c);
            colorButtons.get(colorButtons.size()-1).setSelected(true);
            colorPanel.revalidate();
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
            try
            {
                BufferedImage img = ImageIO.read(lastFile);
                mainPanel.setStartImage(img);
                this.pack();
            }
            catch (Exception exp)
            {
                exp.printStackTrace();
            }
        }
        mainPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == addButton)
            makeNewButton();
        else if (e.getSource() == openMenuItem)
            doLoadImage();
        else
        {
            Color c = ((ColorButton) e.getSource()).getMyColor();
            mainPanel.setActiveColor(c);
            repaint();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        mainPanel.setThreshold(thresholdSlider.getValue());
    }
}
