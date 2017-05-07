import java.awt.*;       // Using AWT's Graphics and Color
import java.awt.event.*; // Using AWT event classes and listener interfaces
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;    // Using Swing's components and containers

/** Custom Drawing Code Template */
// A Swing application extends javax.swing.JFrame
public class Main extends JFrame {
    // Define constants
    public static final int CANVAS_WIDTH  = 980;
    public static final int CANVAS_HEIGHT = 575;
    public static final int COORDINATES_WIDTH = 30;

    // Declare an instance of the drawing canvas,
    // which is an inner class called DrawCanvas extending javax.swing.JPanel.
    private DrawCanvas canvas;

    // Constructor to set up the GUI components and event handlers
    public Main() throws IOException {
        canvas = new DrawCanvas();    // Construct the drawing canvas
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        // Set the Drawing JPanel as the JFrame's content-pane
        Container cp = getContentPane();
        cp.add(canvas);
        // or "setContentPane(canvas);"

        setDefaultCloseOperation(EXIT_ON_CLOSE);   // Handle the CLOSE button
        pack();              // Either pack() the components; or setSize()
        setTitle("......");  // "super" JFrame sets the title
        setVisible(true);    // "super" JFrame show
    }

    /**
     * Define inner class DrawCanvas, which is a JPanel used for custom drawing.
     */
    private class DrawCanvas extends JPanel {
        private DrawCanvas() throws IOException {
        }

        String fileName = "/rainbow gradient.jpg";
        BufferedImage image = ImageIO.read(getClass().getResource(fileName));
        BufferedImage newImage = getNewImage(image);


        // Override paintComponent to perform painting
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);     // paint parent's background
            setBackground(Color.BLACK);  // set background color for JPanel
            g.drawImage(newImage, 0, 0, null);

        }
    }

    public BufferedImage getNewImage(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        byte[] newPixels = new byte[pixels.length];
        final int width = image.getWidth();
        final int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] pixelArray2d = getPixelArray(hasAlphaChannel,height,width,pixels);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentPixel = ((y * width) + x) * 3; //gets current pixel for byte array access

                Point r = Pix2Complex(x, y, height, width); //converts pixels to complex point
                r = function(r); //applies desired function to complex point
                Point w = Complex2Pix(r.getXD(), r.getYD(), height, width); //returns complex point to pixel representation
                int wX = w.getXI();
                int wY = w.getYI();



                if (wX >= width || wX < 0 || wY >= height || wY < 0) { //checks that pixel is within image dimensions
                    newPixels[currentPixel] = 0;
                } else {
                    int argb = pixelArray2d[wY][wX];
                    newPixels[currentPixel    ] = (byte) ((byte) argb & 0xff);
                    newPixels[currentPixel + 1] = (byte) ((byte) (argb >> 8) & 0xff);
                    newPixels[currentPixel + 2] = (byte) ((byte) (argb >> 16) & 0xff);


                }

            }
        }
        try {
            newImage.setData(Raster.createRaster(newImage.getSampleModel(), new DataBufferByte(newPixels, newPixels.length), new java.awt.Point()) );
            return newImage;
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }

        return newImage;
    }

    public int[][] getPixelArray(boolean hasAlphaChannel, int height, int width, byte[] pixels) {
        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); //alpha
                argb += ((int) pixels[pixel + 1] & 0xff); //blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); //green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); //red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }
        return result;
    }

    public Point Pix2Complex(int pixelX, int pixelY, int height, int width) {
        double x = pixelX - (width / 2.0); //adjust pixels about origin
        double y = height - pixelY;      //reverse coordinates so positive is on top
        y = y - (height / 2.0);
        x = x / width * COORDINATES_WIDTH; //scale width to [-15,15]
        double adjustment = (double)COORDINATES_WIDTH / (double)width;
        y = y * adjustment; //scale height to proportional with bitmap input image
        Point r = new Point(x,y);

        return r;
    }

    public Point Complex2Pix(double im, double re, int height, int width) {
        re = re * (double)width / (double)COORDINATES_WIDTH;
        im = im * width / COORDINATES_WIDTH;
        re = re + (height / 2.0);//-1 accounts for conversion errors
        re = height - re;
        im = im + (width / 2.0);

        Point r = new Point((int)im, (int)re);
        return r;
    }

    public Point function(Point r) {
        ComplexNumber n = new ComplexNumber(r.getYD(), r.getXD());
        n = ComplexNumber.multiply(n, new ComplexNumber(0.0,1.0));
        return new Point(n.getIm(), n.getRe());
    }

    // The entry main method
    public static void main(String[] args) {
        // Run the GUI codes on the Event-Dispatching thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Main(); // Let the constructor do the job
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}