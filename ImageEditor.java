import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class ImageEditor {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new ImageEditorFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}

class ImageEditorFrame extends JFrame {
    JPanel panelTop = new JPanel();
    JButton buttonSaveImage = new JButton("Save Image");
    JButton buttonOpenFile = new JButton("Open File");
    JLabel labelImage = new JLabel();
    JScrollPane scrollPaneImage = new JScrollPane(labelImage);
    JButton buttonRotate = new JButton ("Rotate 90 degrees to the right");
    JButton buttonReset = new JButton ("Reset to original");
    JButton buttonEdgeDetection = new JButton("Edge Detection");
    JButton buttonBrightnessUp = new JButton ("Brightness +");
    JButton buttonBrightnessDown = new JButton ("Brightness -");


    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private BufferedImage image;
    private BufferedImage originalImage;

    //85% of the OS resolution so it does not take up the whole screen
    private static final int DEFAULT_WIDTH = (int) (screenSize.getWidth() * 0.85);
    private static final int DEFAULT_HEIGHT = (int) (screenSize.getHeight() * 0.85);
    boolean fileOpened = false;

    public ImageEditorFrame() {
        setTitle("Image Editor - DEVELOPED BY GROUP 7");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        panelTop.add(buttonOpenFile);
        panelTop.add(buttonReset);
        panelTop.add(buttonSaveImage);
        panelTop.add(buttonRotate);
        panelTop.add(buttonEdgeDetection);
        panelTop.add(buttonBrightnessUp);
        panelTop.add(buttonBrightnessDown);

        add(panelTop, BorderLayout.NORTH);
        add(scrollPaneImage, BorderLayout.CENTER);

        //The following buttons are disabled by default
        buttonReset.setEnabled(false);
        buttonSaveImage.setEnabled(false);
        buttonEdgeDetection.setEnabled(false);
        buttonRotate.setEnabled(false);
        buttonBrightnessUp.setEnabled(false);
        buttonBrightnessDown.setEnabled(false);

        add(new JComponent() {
            public void paintComponent(Graphics g) {
                if (image != null) g.drawImage(image, 0, 0, null);
            }
        });

        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setEnabled(false);

        JMenuItem brightnessUp = new JMenuItem("Brightness +");
        JMenuItem brightnessDown = new JMenuItem("Brightness -");
        JMenuItem edgeDetection = new JMenuItem("Edge detection");
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(brightnessUp);
        brightnessUp.setEnabled(false);

        editMenu.add(brightnessDown);
        brightnessDown.setEnabled(false);

        editMenu.add(edgeDetection);
        edgeDetection.setEnabled(false);

        JMenuItem rotation = new JMenuItem("Rotation");
        editMenu.add(rotation);
        rotation.setEnabled(false);

        JMenu help = new JMenu ("Help");
        JMenuItem aboutUs = new JMenuItem("About");
        help.add(aboutUs);

        aboutUs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"HKU SPACE CC" +
                        "\nCourse: Objected-Oriented Programming\n" +
                        "Group Project - Image Editor\n"+
                        "2018 CL02 Group 7\n\n" +
                        "Coded with blood, sweat and love by: \nRaiyan Reza\nAnri Kitami\nMax Chui\nHoward Tang\nVincent Wong\n\n" +
                        "Version: 1.0\nCodename: Fish and Chips\n\nCopyright (2018) The Turnitin League","About this amazing program",JOptionPane.INFORMATION_MESSAGE);
            }
        });

        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openFile();
                if (fileOpened)
                {
                    brightnessDown.setEnabled(true);
                    brightnessUp.setEnabled(true);
                    saveItem.setEnabled(true);
                    rotation.setEnabled(true);
                    edgeDetection.setEnabled(true);
                    saveItem.setEnabled(true);
                }
            }
        });
        fileMenu.add(openItem);

        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                image = originalImage;
                repaint();
            }
        });

        buttonOpenFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();

                if (fileOpened)
                {
                    brightnessDown.setEnabled(true);
                    brightnessUp.setEnabled(true);
                    saveItem.setEnabled(true);
                    rotation.setEnabled(true);
                    edgeDetection.setEnabled(true);
                }
            }
        });

        buttonBrightnessUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float a = 1.1f;
                float b = 0;
                RescaleOp op = new RescaleOp(a, b, null);
                filter(op);
            }
        });

        buttonBrightnessDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float a = 1.0f;
                float b = -8.0f;
                RescaleOp op = new RescaleOp(a, b, null);
                filter(op);
            }
        });

        buttonSaveImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        buttonEdgeDetection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float[] elements = {0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f, 0.0f};
                convolve(elements);
            }
        });

        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                saveFile();
            }
        });
        fileMenu.add(saveItem);

        JMenuItem exitItem = new JMenuItem("Quit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        // brightness up method
        brightnessUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                float a = 1.1f;
                float b = 0;
                RescaleOp op = new RescaleOp(a, b, null);
                filter(op);
            }
        });


        //Brightness Down Method
        brightnessDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                float a = 1.0f;
                float b = -8.0f;
                RescaleOp op = new RescaleOp(a, b, null);
                filter(op);
            }
        });

        // edge detection method
        edgeDetection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                float[] elements = {0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f, 0.0f};
                convolve(elements);
            }
        });


        buttonRotate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int imageWidth = image.getWidth();
                int iamgeHeight = image.getHeight();
                BufferedImage newImage = new BufferedImage(iamgeHeight, imageWidth, image.getType());

                for (int i = 0; i < imageWidth; i++)
                    for (int j = 0; j < iamgeHeight; j++)
                        newImage.setRGB(iamgeHeight - 1 - j, i, image.getRGB(i, j));
                image = newImage;
                repaint();
            }
        });

        // rotation method
        rotation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int imageWidth = image.getWidth();
                int iamgeHeight = image.getHeight();
                BufferedImage newImage = new BufferedImage(iamgeHeight, imageWidth, image.getType());

                for (int i = 0; i < imageWidth; i++)
                    for (int j = 0; j < iamgeHeight; j++)
                        newImage.setRGB(iamgeHeight - 1 - j, i, image.getRGB(i, j));
                image = newImage;
                repaint();
            }
        });

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(help);
        setJMenuBar(menuBar);
    }

    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        String[] extensions = ImageIO.getReaderFileSuffixes();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", extensions));
        int r = chooser.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        try {
            Image img = ImageIO.read(chooser.getSelectedFile());
            image = new BufferedImage(img.getWidth(null), img.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);
            originalImage = new BufferedImage(img.getWidth(null), img.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);
            image.getGraphics().drawImage(img, 0, 0, null);
            originalImage.getGraphics().drawImage(img, 0, 0, null);

            buttonEdgeDetection.setEnabled(true);
            buttonReset.setEnabled(true);
            buttonRotate.setEnabled(true);
            buttonSaveImage.setEnabled(true);
            buttonBrightnessDown.setEnabled(true);
            buttonBrightnessUp.setEnabled(true);

            fileOpened = true;
        }
        catch (NullPointerException npe)
        {
            JOptionPane.showMessageDialog(null, "Please report this to the developer:" +
                    "\nERROR: " + npe + "\nWe apologise for the inconvenience caused.","Opps! Something went wrong!",JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Please report this to the developer:" +
                    "\nERROR: " + e + "\nWe apologise for the inconvenience caused.","Opps! Something went wrong!",JOptionPane.ERROR_MESSAGE);
        }
        repaint();
    }

    public void saveFile() {
        JFileChooser saver = new JFileChooser("./");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(".jpeg", "jpg");
        saver.setFileFilter(filter);
        int status = saver.showSaveDialog(null);
        if (status == JFileChooser.APPROVE_OPTION) {
            try {
                File jpgFile = new File(saver.getSelectedFile() + ".jpg");
                ImageIO.write(image, "jpg", jpgFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Please report this to the developer:" +
                        "\nERROR: " + e + "\nWe apologise for the inconvenience caused.","Opps! Something went wrong!",JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            //Nothing will be done
        }
    }


    private void filter(BufferedImageOp op) {
        if (image == null) return;
        image = op.filter(image, null);
        repaint();
    }


    private void convolve(float[] elements) {
        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op);
    }
}