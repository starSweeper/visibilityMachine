/* This class is used to create a Graphical User Interface */

//Needed for collapsible JXTask Panes
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

//Needed for pie chart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

//Needed for pulling data from JSON
import org.json.JSONArray;
import org.json.JSONObject;

//Needed for GUI
import javax.swing.*;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jdesktop.swingx.JXLabel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

//Needed to read from/write to files, copy, or alter them
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

//Needed for ArrayList, and to sort ArrayList
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GUI {
    private ArrayList<LabelImage> images;  // An array of LabelImage objects: The images to be labeled
    private boolean timeToMoveOn = false; //Used to determine if we are ready to display new information
    private boolean moveToNextPhoto = false; //Used to determine if we are ready to move to next image
    private LabelImage currentImage; //The current image being labeled
    private JSONObject clusterList; //Cluster information from JSON file
    private File clusterFile; //clusters.json
    private JFrame window = new JFrame(); //The window that holds all GUI components
    private JButton skipButton = new JButton("SKIP"); //Allows users to "skip" images without labeling them
    private JButton clearButton = new JButton("CLEAR"); //Allows users to label images as "clear"
    private JButton foggyButton = new JButton("FOGGY"); //Allows users to label images as "foggy"
    private JButton smokyButton = new JButton("SMOKY"); //Allows users to label images as "smoky"
    private JButton goodButton = new JButton("GOOD VISIBILITY"); //Allows users to label "good" visibility images
    private JButton badButton = new JButton("BAD VISIBILITY"); //Allows users to label "bad" visibility images
    private JPanel endOfTheLine = new JPanel(); //A panel that appears when there are no images left to label
    private boolean updateGUI = false; //Check if it is time to update the GUI after image order has been changed
    private JProgressBar progressBar; //A way for users to track their progress
    private int clearCount,smokyCount,foggyCount,goodCount,badCount,skipCount,unlabeledCount; //Image counts
    private JScrollPane currentGallery; //JScrollPane to temporary hold the current gallery being displayed

    //Category objects to keep track of images in each category
    private Categories skipImages = new Categories();
    private Categories clearImages = new Categories();
    private Categories smokyImages = new Categories();
    private Categories foggyImages = new Categories();
    private Categories goodImages = new Categories();
    private Categories badImages = new Categories();

    //JXTaskPanes holding statistics for each category
    private JXTaskPane imageStatPane, monthStatPane, clusterStatPane, clearStatPane, smokyStatPane, foggyStatPane;
    private JXTaskPane goodStatPane, badStatPane, skipStatPane;

    //GUI constructor
    GUI(ArrayList<LabelImage> imagesList, JSONObject allClusters, File cFile){
        String opencvpath = System.getProperty("user.dir") + "\\";
        System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");

        images = imagesList;
        clusterList = allClusters;
        clusterFile = cFile;

        //Set up window (main GUI Frame)
        window.setFocusable(true);
        window.requestFocusInWindow();
        window.setExtendedState(Frame.MAXIMIZED_BOTH);
        window.setLayout(new CardLayout()); //Allows easy change of components
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Exit the program when the exit button is pressed
        window.setTitle("Image Labeling Interface");

        //Create progress bar
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(imagesList.size());
        progressBar.setString(progressBar.getValue() + "/" + progressBar.getMaximum() + " images processed");
        progressBar.setStringPainted(true);

        unlabeledCount = imagesList.size(); //Get unlabeled count for pie charts

        imageStatPane = new JXTaskPane();
        imageStatPane.setTitle("Current Image Information");
        imageStatPane.setCollapsed(false);

        clusterStatPane = new JXTaskPane();
        clusterStatPane.setTitle("Cluster Information");
        clusterStatPane.setCollapsed(true);

        monthStatPane = new JXTaskPane();
        monthStatPane.setTitle("Month Information");
        monthStatPane.setCollapsed(true);

        clearStatPane = new JXTaskPane();
        clearStatPane.setTitle("Clear Image Averages");
        clearStatPane.setCollapsed(true);

        smokyStatPane = new JXTaskPane();
        smokyStatPane.setTitle("Smoky Image Averages");
        smokyStatPane.setCollapsed(true);

        foggyStatPane = new JXTaskPane();
        foggyStatPane.setTitle("Foggy Image Averages");
        foggyStatPane.setCollapsed(true);

        goodStatPane = new JXTaskPane();
        goodStatPane.setTitle("Good Visibility Image Averages");
        goodStatPane.setCollapsed(true);

        badStatPane = new JXTaskPane();
        badStatPane.setTitle("Bad Visibility Image Averages");
        badStatPane.setCollapsed(true);

        skipStatPane = new JXTaskPane();
        skipStatPane.setTitle("Skipped Image Averages");
        skipStatPane.setCollapsed(true);

        //Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu sortingMenu = new JMenu("Sorting");
        JMenuItem sortByMonth = new JMenuItem("Sort by month");
        sortByMonth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGUI = true;
                sortArrayListByMonth();
                JOptionPane.showMessageDialog(null,
                        "Changes will take place with the next image.");
                interfaceG();
            }
        });
        JMenuItem sortByCluster = new JMenuItem("Sort by cluster");
        sortByCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGUI = true;
                sortArrayListByCluster();
                JOptionPane.showMessageDialog(null,
                        "Changes will take place with the next image.");
                interfaceG();
            }
        });
        sortingMenu.add(sortByMonth);
        sortingMenu.add(sortByCluster);
        menuBar.add(sortingMenu);
        window.setJMenuBar(menuBar);

        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                images.remove(currentImage);
                removeFromJSON();
                updateProgressBar();
                skipImages.updateLists(currentImage);

                String oldPath = currentImage.getPath();
                File controversialImage = new File(currentImage.getPath());
                controversialImage.renameTo(new File("controversialImages/" + currentImage.getName()));
                controversialImage = new File("controversialImages/" + currentImage.getName());
                currentImage.updatePath(controversialImage.getAbsolutePath());
                updateLog(new java.util.Date() + " Renamed " + oldPath + " to " + currentImage.getPath());

                timeToMoveOn = true;
                moveToNextPhoto = true;
                skipCount++;
                unlabeledCount--;
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    Files.copy(Paths.get(currentImage.getPath()), Paths.get("trainOrTest/clear/" +
                            currentImage.getName()), StandardCopyOption.REPLACE_EXISTING);
                }catch(Exception ex){
                    System.out.println("Error while copying image to new folder");
                    System.out.print(ex.toString());
                }

                updateLog(new java.util.Date() + " Copied " + currentImage.getPath() +
                        " to visibilityMachine/trainOrTest/clear/");
                clearImages.updateLists(currentImage);
                timeToMoveOn = true;
                clearCount++;
            }
        });

        foggyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Files.copy(Paths.get(currentImage.getPath()), Paths.get("trainOrTest/foggy/" +
                            currentImage.getName()), StandardCopyOption.REPLACE_EXISTING);

                    updateLog(new java.util.Date() + " Copied " + currentImage.getPath() +
                            " to visibilityMachine/trainOrTest/foggy/");
                }catch(Exception ex){
                    System.out.println("Error while copying image to new folder");
                    System.out.println(ex.toString());
                }
                foggyImages.updateLists(currentImage);
                timeToMoveOn = true;
                foggyCount++;
            }
        });

        smokyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Files.copy(Paths.get(currentImage.getPath()), Paths.get("trainOrTest/smoky/" +
                            currentImage.getName()), StandardCopyOption.REPLACE_EXISTING);
                }catch(Exception ex){
                    System.out.println("Error while copying image to new folder");
                    System.out.print(ex.toString());
                }

                updateLog(new java.util.Date() + " Copied " + currentImage.getPath() +
                        " to visibilityMachine/trainOrTest/smoky/");
                smokyImages.updateLists(currentImage);
                timeToMoveOn = true;
                smokyCount++;
            }
        });

        goodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                images.remove(currentImage);
                removeFromJSON();
                updateProgressBar();
                goodImages.updateLists(currentImage);

                String oldPath = currentImage.getPath();
                File goodVisibilityImage = new File(currentImage.getPath());
                goodVisibilityImage.renameTo(new File("trainOrTest/goodVisibility/" +
                        currentImage.getName()));

                goodVisibilityImage = new File(("trainOrTest/goodVisibility/" +
                        currentImage.getName()));
                currentImage.updatePath(goodVisibilityImage.getAbsolutePath());
                updateLog(new java.util.Date() + " Renamed " + oldPath + " to " +
                        goodVisibilityImage.getPath());
                timeToMoveOn = true;
                goodCount++;
                unlabeledCount--;
            }
        });

        badButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                images.remove(currentImage);
                removeFromJSON();
                updateProgressBar();
                badImages.updateLists(currentImage);

                String oldPath = currentImage.getPath();
                File badVisibilityImage = new File(currentImage.getPath());
                badVisibilityImage.renameTo(new File("trainOrTest/badVisibility/"
                        + currentImage.getName()));

                badVisibilityImage = new File("trainOrTest/badVisibility/"
                        + currentImage.getName());
                currentImage.updatePath(badVisibilityImage.getAbsolutePath());
                updateLog(new java.util.Date() + " Renamed " + oldPath + " to " +
                        badVisibilityImage.getPath());
                timeToMoveOn = true;
                badCount++;
                unlabeledCount--;
            }
        });

        endOfTheLine.setLayout(new BorderLayout());
        endOfTheLine.add(new JLabel(new ImageIcon("endOfLine.png")), BorderLayout.WEST);
        endOfTheLine.add(new JLabel(new ImageIcon("AlbertCassian.jpg")), BorderLayout.EAST);
    }

    //Writes a line to a log called label_log.txt
    private void updateLog(String logEntry){
        try {
            FileWriter fw = new FileWriter("label_log.txt", true);
            fw.write(logEntry + "\n");
            fw.close();
        }
        catch(Exception E){
            System.out.println("Error while updating log");
            System.out.println(E.toString());
        }
    }

    //Create and update data set for clear/smoky/foggy pie chart
    private PieDataset updateCSFDataSet(){
        DefaultPieDataset dataSet = new DefaultPieDataset();
        dataSet.setValue("Clear(" + clearCount + ")" , clearCount);
        dataSet.setValue("Smoky(" + smokyCount + ")", smokyCount);
        dataSet.setValue("Foggy(" + foggyCount + ")", foggyCount);
        dataSet.setValue("Skipped(" + skipCount + ")", skipCount);
        dataSet.setValue("Unlabeled(" + unlabeledCount + ")", unlabeledCount);
        return dataSet;
    }

    //Create and update data set for good/bad pie chart
    private PieDataset updateGBDataSet(){
        DefaultPieDataset dataSet = new DefaultPieDataset();
        dataSet.setValue("Good(" + goodCount + ")", goodCount);
        dataSet.setValue("Bad(" + badCount + ")", badCount);
        dataSet.setValue("Skipped(" + skipCount + ")", skipCount);
        dataSet.setValue("Unlabeled(" + unlabeledCount + ")", unlabeledCount);
        return dataSet;
    }

    //Create a pie chart provided a data set and title
    private ImageIcon createChart(String title, PieDataset dataSet){
        JFreeChart chart = ChartFactory.createPieChart(title, dataSet, true, false, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setIgnoreZeroValues(true); //Makes it so null values do not appear on chart
        plot.setLabelGap(0.02);

        return new ImageIcon(chart.createBufferedImage(400,250)); //Return chart as an ImageIcon
    }

    //Update the progress bar as images are processed
    private void updateProgressBar(){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(progressBar.getValue() + 1);
                progressBar.setString(progressBar.getValue() + "/" + progressBar.getMaximum() + " images processed");
            }
        });
    }

    //Sort a LabelImage ArrayList by month
    private void sortArrayListByMonth(){
        Collections.sort(images, new Comparator<LabelImage>() {
            @Override
            public int compare(LabelImage o1, LabelImage o2) {
                Integer month1 = Integer.parseInt(o1.getMonth());
                Integer month2 = Integer.parseInt(o2.getMonth());

                return month2.compareTo(month1);
            }
        });
    }

    //Sort a LabelImage ArrayList by cluster
    private void sortArrayListByCluster(){
        Collections.sort(images, new Comparator<LabelImage>() {
            @Override
            public int compare(LabelImage o1, LabelImage o2) {
                Integer cluster1 = o1.getParentCluster().getClusterNum();
                Integer cluster2 = o2.getParentCluster().getClusterNum();
                return cluster2.compareTo(cluster1);
            }
        });
    }

    //Remove an image from clusters.json
    private void removeFromJSON(){
        try{
            JSONArray clusters = clusterList.getJSONArray("clusters");
            int currentCluster = currentImage.getParentCluster().getClusterNum();

            //For every cluster in the file (going backwards), look for the right cluster
            for(int i = clusters.length() - 1; i >= 0; i--){
                JSONObject current = clusters.getJSONObject(i);
                if(current.getInt("clusterNum") == (currentCluster - 1)){
                    JSONArray currentImages = current.getJSONArray("images");
                    //For every image in the cluster, look for the right image
                    for(int j = currentImages.length() - 1; j >= 0; j--){
                        if(currentImages.getJSONObject(j).getInt("imageID") == currentImage.getId()){
                            currentImages.remove(j);

                            FileWriter saveCluster = new FileWriter(clusterFile, false);
                            saveCluster.write(clusterList.toString());
                            saveCluster.close();

                            updateLog(new java.util.Date() + " Removed " + currentImage.getName() +
                                    " from cluster #" + currentImage.getParentCluster().getClusterNum());
                            return;
                        }
                    }
                }
            }
        }
        catch(Exception E){
            System.out.println("Error while removing image from clusters.json");
            System.out.println(E.toString());
        }
    }

    // matToBufferedImage method Taken from open-cv Java video Basics repository:
    // https://github.com/opencv-java/video-basics/blob/1ff61b226b306cc04d8a08910ad5cc7edd91e1a3/src/it/polito/elite/teaching/cv/utils/Utils.java
    //Converts a Mat to a BufferedImage
    private static BufferedImage matToBufferedImage(Mat original)
    {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1)
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        }
        else
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    //Display images to be labeled
    public void interfaceG() {
        //Check if we need to restart the list because images have been resorted
        if(updateGUI){
            updateGUI = false;
            return;
        }
        //If there are any images...
        if (images.size() > 0) {
            //For every image in the list, going backwards (default sorting order is by cluster)
            for (int i = images.size() - 1; i >= 0; i--) {
                timeToMoveOn = false; //Not ready to move on to next stage of labeling
                moveToNextPhoto = false; //Not ready to move on to next image
                currentImage = images.get(i); //Set the current image

                //Set up clear/foggy/smoky prompt panel
                JPanel newClearFoggySmokyPanel = new JPanel();
                newClearFoggySmokyPanel.setLayout(new BorderLayout());

                //Add skip button
                JPanel skipPanel = new JPanel();
                skipPanel.setOpaque(true);
                skipPanel.setBackground(Color.WHITE);
                skipPanel.add(skipButton);

                //Add progress bar
                JPanel progressPanel = new JPanel();
                progressPanel.setOpaque(true);
                progressPanel.setBackground(Color.WHITE);
                progressPanel.add(progressBar);

                //Add editable title
                JPanel titlePanel = new JPanel();
                titlePanel.setOpaque(true);
                titlePanel.setBackground(Color.WHITE);
                JTextField imageTitle = new JTextField();
                titlePanel.add(imageTitle);
                imageTitle.setText(currentImage.getName());
                imageTitle.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (JOptionPane.showConfirmDialog(window, "Are you sure you want to rename " +
                                "this file to\n" + imageTitle.getText() + "?") == JOptionPane.YES_OPTION) {
                            currentImage.changeName(imageTitle.getText());
                            updateLog(new java.util.Date() + " Renamed " + currentImage.getPath()
                                    + " to " + currentImage.getPath());
                            try {
                                FileWriter saveCluster = new FileWriter(clusterFile, false);
                                saveCluster.write(clusterList.toString());
                                saveCluster.close();
                            }
                            catch (Exception err){
                                System.out.println("Error while renaming image");
                                System.out.println(err.toString());
                            }
                        }
                    }
                });

                //Display regular photo and canny edge photo
                ImageIcon photo = new ImageIcon(currentImage.getPath());
                Mat cannyPhoto = new Mat();
                Mat uncannyPhoto = Imgcodecs.imread(currentImage.getPath());  //Convert image to Mat
                Imgproc.cvtColor(uncannyPhoto, cannyPhoto, Imgproc.COLOR_BGR2GRAY); //Convert image (Mat) to gray scale
                Imgproc.Canny(cannyPhoto,cannyPhoto,50,100); //Canny edge detection
                ImageIcon cannyDisplay = new ImageIcon(matToBufferedImage(cannyPhoto)); //Convert Mat to ImageIcon
                JLabel photoBox = new JLabel(); //Holds image with no padding or empty space
                photoBox.setIcon(photo);
                photoBox.setVerticalAlignment(SwingConstants.TOP);
                photoBox.setHorizontalTextPosition(JLabel.CENTER);
                photoBox.setHorizontalAlignment(JLabel.CENTER);

                //Mouse listener for image (toggle between regular image and canny image)
                photoBox.addMouseListener(new java.awt.event.MouseAdapter(){
                    boolean canny = false; //Do we want to display regular image or canny image?

                    //If user hovers over regular image, display canny image
                    public void mouseEntered(java.awt.event.MouseEvent event){
                        if(!canny) {
                            photoBox.setIcon(cannyDisplay);
                        }
                    }

                    //If user stops hovering over image while not set to canny, display regular image
                    public void mouseExited(java.awt.event.MouseEvent event){
                        if(!canny) {
                            photoBox.setIcon(photo);
                        }
                    }

                    //If user clicks on image while in regular mode, switch to canny mode or vice-versa
                    public void mouseClicked(java.awt.event.MouseEvent event) {
                        if(!canny) {
                            photoBox.setIcon(cannyDisplay);
                            canny = true;
                        }
                        else{
                            photoBox.setIcon(photo);
                            canny = false;
                        }
                    }
                });

                //JPanel to hold the JLabel that holds the image. (Ensures no padding or empty space.
                JPanel photoPanel = new JPanel();
                photoPanel.setOpaque(true);
                photoPanel.setBackground(Color.WHITE);
                photoPanel.setSize(new Dimension(photo.getIconHeight(),photo.getIconWidth()));
                photoPanel.add(photoBox);

                //Display date and time image was taken
                JLabel dateTime = new JLabel("<html><br/><strong>Time:</strong> " + currentImage.getTime() +
                        "<br/><strong>Date:</strong> " + currentImage.getDate() + "</html>");
                dateTime.setOpaque(true);
                dateTime.setBackground(Color.WHITE);
                dateTime.setFont(new Font("Serif", Font.PLAIN, 14));
                dateTime.setHorizontalAlignment(JLabel.CENTER);
                dateTime.setVerticalAlignment(JLabel.TOP);

                //Add image and date/time information to a container
                JPanel imageContainer = new JPanel(); //Holds the image and the date/time info
                imageContainer.setLayout(new BorderLayout());
                imageContainer.add(photoPanel, BorderLayout.NORTH);
                imageContainer.add(dateTime,BorderLayout.CENTER);

                //Add image galleries (change to different gallery using radio buttons)
                JPanel gallerySelector = new JPanel(); //Holds the gallery (changeable) and the radio buttons
                CardLayout cardLayout = new CardLayout(); //Makes it easier to switch JScrollPanes (image galleries)
                gallerySelector.setLayout(cardLayout);
                JPanel gallery = new JPanel(); //Changeable image gallery (contains JScrollPanes that are switched)
                gallery.setLayout(new BorderLayout());

                //Create new image galleries
                currentImage.getParentCluster().setImageGallery(currentImage);
                clearImages.setImageGallery(currentImage);
                foggyImages.setImageGallery(currentImage);
                smokyImages.setImageGallery(currentImage);
                skipImages.setImageGallery(currentImage);

                //Create "cards" to display the new image galleries
                JScrollPane clusterCard = currentImage.getParentCluster().getImageGallery();
                JScrollPane clearCard = clearImages.getImageGallery();
                JScrollPane foggyCard = foggyImages.getImageGallery();
                JScrollPane smokyCard = smokyImages.getImageGallery();
                JScrollPane skippedCard = skipImages.getImageGallery();

                //Set default gallery to the cluster gallery
                currentGallery = clusterCard;
                gallerySelector.add(clusterCard);

                //Create cluster button and add listener
                JRadioButton cluster = new JRadioButton("Cluster");
                cluster.setOpaque(true);
                cluster.setBackground(Color.WHITE);
                cluster.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        gallerySelector.remove(currentGallery);
                        gallerySelector.add(clusterCard);
                        clusterCard.setVisible(true);
                        gallerySelector.revalidate();
                        currentGallery = clusterCard;
                    }
                });

                //Create clear button and add listener
                JRadioButton clear = new JRadioButton("Clear");
                clear.setOpaque(true);
                clear.setBackground(Color.WHITE);
                clear.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        gallerySelector.remove(currentGallery);
                        gallerySelector.add(clearCard);
                        clearCard.setVisible(true);
                        gallerySelector.revalidate();
                        currentGallery = clearCard;
                    }
                });

                //Create foggy button and add listener
                JRadioButton foggy = new JRadioButton("Foggy");
                foggy.setOpaque(true);
                foggy.setBackground(Color.WHITE);
                foggy.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        gallerySelector.remove(currentGallery);
                        gallerySelector.add(foggyCard);
                        foggyCard.setVisible(true);
                        gallerySelector.revalidate();
                        currentGallery = foggyCard;
                    }
                });

                //Create smoky button and add listener
                JRadioButton smoky = new JRadioButton("Smoky");
                smoky.setOpaque(true);
                smoky.setBackground(Color.WHITE);
                smoky.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        gallerySelector.remove(currentGallery);
                        gallerySelector.add(smokyCard);
                        smokyCard.setVisible(true);
                        gallerySelector.revalidate();
                        currentGallery = smokyCard;
                    }
                });

                //Create skipped button and add listener
                JRadioButton skipped = new JRadioButton("Skipped");
                skipped.setOpaque(true);
                skipped.setBackground(Color.WHITE);
                skipped.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        gallerySelector.remove(currentGallery);
                        gallerySelector.add(skippedCard);
                        skippedCard.setVisible(true);
                        gallerySelector.revalidate();
                        currentGallery = skippedCard;
                    }
                });

                //Add radio buttons to button group
                ButtonGroup buttonGroup1 = new ButtonGroup();
                buttonGroup1.add(cluster);
                buttonGroup1.add(clear);
                buttonGroup1.add(foggy);
                buttonGroup1.add(smoky);
                buttonGroup1.add(skipped);

                //Create a JLabel to hold radio buttons
                JLabel buttonLabel1 = new JLabel();
                buttonLabel1.setLayout(new FlowLayout());
                buttonLabel1.setPreferredSize(new Dimension(100,30));
                buttonLabel1.setOpaque(true);
                buttonLabel1.setBackground(Color.WHITE);

                //Add radio buttons to JLabel
                buttonLabel1.add(cluster);
                buttonLabel1.add(clear);
                buttonLabel1.add(foggy);
                buttonLabel1.add(smoky);
                buttonLabel1.add(skipped);
                cluster.setSelected(true);

                //Add radio buttons and gallery selector to container
                gallery.add(buttonLabel1, BorderLayout.NORTH);
                gallery.add(gallerySelector, BorderLayout.SOUTH);

                //Add pie chart to display user progress in each category
                JPanel chartPanel = new JPanel();
                chartPanel.setOpaque(true);
                chartPanel.setBackground(Color.WHITE);
                JLabel pieLabel = new JLabel("",createChart("Clear/Smoky/Foggy",
                        updateCSFDataSet()),JLabel.CENTER);
                chartPanel.add(pieLabel);

                //Add image statistics
                JXLabel imageStats = new JXLabel(currentImage.getCurrentImageHTML());
                imageStats.setFont(new Font("Serif", Font.PLAIN, 14));
                imageStatPane.removeAll();
                imageStatPane.add(imageStats);

                //Add cluster statistics
                JXLabel clusterStats = new JXLabel(currentImage.getClusterHTML());
                clusterStats.setFont(new Font("Serif", Font.PLAIN, 14));
                clusterStatPane.removeAll();
                clusterStatPane.add(clusterStats);

                //Add month statistics
                JXLabel monthStats = new JXLabel(currentImage.getMonthlyHTML());
                monthStats.setFont(new Font("Serif", Font.PLAIN, 14));
                monthStatPane.removeAll();
                monthStatPane.add(monthStats);

                //Add statistics for clear images
                JXLabel clearStats = new JXLabel(clearImages.generateCategoryHTML(currentImage,"Clear"));
                clearStats.setFont(new Font("Serif", Font.PLAIN, 14));
                clearStatPane.removeAll();
                clearStatPane.add(clearStats);

                //Add statistics for smoky images
                JXLabel smokyStats = new JXLabel(smokyImages.generateCategoryHTML(currentImage,"Smoky"));
                smokyStats.setFont(new Font("Serif", Font.PLAIN, 14));
                smokyStatPane.removeAll();
                smokyStatPane.add(smokyStats);

                //Add statistics for foggy images
                JXLabel foggyStats = new JXLabel(foggyImages.generateCategoryHTML(currentImage,"Foggy"));
                foggyStats.setFont(new Font("Serif", Font.PLAIN, 14));
                foggyStatPane.removeAll();
                foggyStatPane.add(foggyStats);

                //Add statistics for good visibility images
                JXLabel goodStats = new JXLabel(goodImages.generateCategoryHTML
                        (currentImage, "Good Visibility"));
                goodStats.setFont(new Font("Serif", Font.PLAIN, 14));
                goodStatPane.removeAll();
                goodStatPane.add(goodStats);

                //Add statistics for bad visibility images
                JXLabel badStats = new JXLabel(badImages.generateCategoryHTML
                        (currentImage, "Bad Visibility"));
                goodStats.setFont(new Font("Serif", Font.PLAIN, 14));
                badStatPane.removeAll();
                badStatPane.add(badStats);

                //Add statistics for skipped images
                JXLabel skippedStats = new JXLabel(skipImages.generateCategoryHTML
                        (currentImage, "Skipped"));
                skippedStats.setFont(new Font("Serif", Font.PLAIN, 14));
                skipStatPane.removeAll();
                skipStatPane.add(skippedStats);

                //Create container to hold statistics JXTaskPanes
                JXTaskPaneContainer statisticsPanel = new JXTaskPaneContainer();
                statisticsPanel.setBorder(BorderFactory.createEmptyBorder());

                ///If statistics exist, display them
                statisticsPanel.add(imageStatPane);
                statisticsPanel.add(clusterStatPane);
                statisticsPanel.add(monthStatPane);
                if(clearCount > 0){
                    statisticsPanel.add(skipStatPane);
                }
                if(foggyCount > 0){
                    statisticsPanel.add(foggyStatPane);
                }
                if(smokyCount > 0){
                    statisticsPanel.add(smokyStatPane);
                }
                if(goodCount > 0){
                    statisticsPanel.add(goodStatPane);
                }
                if(badCount > 0){
                    statisticsPanel.add(badStatPane);
                }
                if(skipCount > 0){
                    statisticsPanel.add(skipStatPane);
                }

                //Add image gallery, pie chart to what will be the west(left) JPanel of the center JPanel in the JFrame
                JPanel centerWest = new JPanel(); //West panel
                centerWest.setOpaque(true);
                centerWest.setBackground(Color.WHITE);
                centerWest.setLayout(new BorderLayout());
                centerWest.add(gallery, BorderLayout.NORTH);
                centerWest.add(chartPanel,BorderLayout.SOUTH);

                //Add clear/foggy/smoky labeling buttons to what will be one of 2 south(bottom)
                //JPanels of the center JPanel in the JFrame
                JPanel labelingButtons1 = new JPanel();
                labelingButtons1.setOpaque(true);
                labelingButtons1.setBackground(Color.WHITE);
                labelingButtons1.add(clearButton);
                labelingButtons1.add(foggyButton);
                labelingButtons1.add(smokyButton);

                //Add title, image to what will be the center(middle) JPanel of the center JPanel in the JFrame
                JPanel centerCenter = new JPanel(); //Center panel of the center panel
                centerCenter.setLayout(new BorderLayout());
                centerCenter.add(titlePanel, BorderLayout.NORTH);
                centerCenter.add(imageContainer,BorderLayout.CENTER);
                centerCenter.add(labelingButtons1, BorderLayout.SOUTH);

                //Add statistics to component that will be the east(right) component of the center JPanel in the JFrame
                JScrollPane centerEast = new JScrollPane(statisticsPanel);
                centerEast.setBorder(BorderFactory.createEmptyBorder());

                //Add skip button and progress bar to what will be the north(top) JPanel in the JFrame
                JPanel top = new JPanel(); //North panel
                top.setOpaque(true);
                top.setBackground(Color.WHITE);
                top.setLayout(new BorderLayout());
                top.add(skipPanel, BorderLayout.WEST);
                top.add(progressPanel,BorderLayout.EAST);

                //Add components to what will be the center(middle) JPanel in the JFrame
                JPanel center = new JPanel();
                center.setLayout(new BorderLayout());
                center.add(centerWest,BorderLayout.WEST);
                center.add(centerCenter, BorderLayout.CENTER);
                center.add(centerEast, BorderLayout.EAST);

                //Add components to newClearFoggySmokyPanel
                newClearFoggySmokyPanel.add(top, BorderLayout.NORTH);
                newClearFoggySmokyPanel.add(center, BorderLayout.CENTER);

                //Add components to JFrame
                window.add(newClearFoggySmokyPanel, BorderLayout.CENTER);
                window.setVisible(true);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                //Wait for user to label or skip image
                boolean checking = true;
                while (checking) {
                    if (!timeToMoveOn) {
                        try {
                            Thread.sleep(1);

                        } catch (Exception e) {
                            System.out.println("Error while waiting for user to label image");
                            System.out.println(e.toString());
                        }
                    } else {
                        timeToMoveOn = false;
                        checking = false;
                    }
                }

                //If we are ready to move on (good/bad), and did not skip image
                if (!moveToNextPhoto) {
                    //Set up good/bad prompt panel
                    JPanel newGoodBadPanel = new JPanel();
                    newGoodBadPanel.setLayout(new BorderLayout());

                    //Add good/bad visibility labeling buttons to what will be the second of 2 south(bottom)
                    //JPanels of the center JPanel in the JFrame
                    JPanel labelingButtons2 = new JPanel();
                    labelingButtons2.setOpaque(true);
                    labelingButtons2.setBackground(Color.WHITE);
                    labelingButtons2.add(goodButton);
                    labelingButtons2.add(badButton);
                    centerCenter.remove(labelingButtons1); //Remove old labelingButtons panel
                    centerCenter.add(labelingButtons2, BorderLayout.SOUTH); //Add new labelingButtons panel

                    //Remove the clear/smoky/foggy pie chart and add a good/bad one
                    chartPanel.remove(pieLabel);
                    JLabel chartLabel = new JLabel(createChart("Good/Bad",updateGBDataSet()));
                    chartPanel.add(chartLabel);

                    //Remove the clear/smoky/foggy buttons and add good/bad ones
                    gallerySelector.remove(currentGallery);
                    goodImages.setImageGallery(currentImage);
                    badImages.setImageGallery(currentImage);

                    //Create "cards" to display the new image galleries
                    JScrollPane goodCard = goodImages.getImageGallery();
                    JScrollPane badCard = badImages.getImageGallery();

                    //Set default gallery to the cluster gallery
                    currentGallery = clusterCard;
                    gallerySelector.add(clusterCard);

                    //Create good button and add listener
                    JRadioButton good = new JRadioButton("Good");
                    good.setOpaque(true);
                    good.setBackground(Color.WHITE);
                    good.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            gallerySelector.remove(currentGallery);
                            gallerySelector.add(goodCard);
                            goodCard.setVisible(true);
                            gallerySelector.revalidate();
                            currentGallery = goodCard;
                        }
                    });

                    //Create bad button and add listener
                    JRadioButton bad = new JRadioButton("Bad");
                    bad.setOpaque(true);
                    bad.setBackground(Color.WHITE);
                    bad.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            gallerySelector.remove(currentGallery);
                            gallerySelector.add(badCard);
                            badCard.setVisible(true);
                            gallerySelector.revalidate();
                            currentGallery = badCard;
                        }
                    });

                    //Add radio buttons to button group
                    ButtonGroup buttonGroup2 = new ButtonGroup();
                    buttonGroup2.add(cluster);
                    buttonGroup2.add(good);
                    buttonGroup2.add(bad);
                    buttonGroup2.add(skipped);

                    //Create a JLabel to hold radio buttons
                    JLabel buttonLabel2 = new JLabel();
                    buttonLabel2.setOpaque(true);
                    buttonLabel2.setBackground(Color.WHITE);
                    buttonLabel2.setLayout(new FlowLayout());
                    buttonLabel2.setPreferredSize(new Dimension(100,30));

                    //Add radio buttons to JLabel
                    buttonLabel2.add(cluster);
                    buttonLabel2.add(good);
                    buttonLabel2.add(bad);
                    buttonLabel2.add(skipped);
                    cluster.setSelected(true);

                    //Add radio buttons to JLabel
                    gallery.remove(buttonLabel1); //Remove old buttonPanel
                    gallery.add(buttonLabel2, BorderLayout.NORTH); //Add new buttonPanel

                    //Add components to newGoodBadPanel
                    newGoodBadPanel.add(top, BorderLayout.NORTH);
                    newGoodBadPanel.add(center, BorderLayout.CENTER);

                    //Remove old components and add new ones to JFrame
                    window.remove(newClearFoggySmokyPanel);
                    window.add(newGoodBadPanel);
                    window.setVisible(true);
                    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    //Wait for user to label or skip image
                    checking = true;
                    while (checking) {
                        if (!timeToMoveOn) {
                            try {
                                Thread.sleep(1);
                            } catch (Exception e) {
                                System.out.println("Error while waiting for user to label image");
                                System.out.println(e.toString());
                            }
                        } else {
                            timeToMoveOn = false;
                            checking = false;
                        }
                    }
                    window.remove(newGoodBadPanel);
                } else {
                    window.remove(newClearFoggySmokyPanel);
                }
            }
            //If we are out of images, display no more images panel
            window.add(endOfTheLine);
            window.setVisible(true);
        }
        else{
            //If there are no images, display no more images panel
            window.add(endOfTheLine);
            window.setVisible(true);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }
}
