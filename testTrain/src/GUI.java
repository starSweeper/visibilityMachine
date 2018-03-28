/* Class to separate training and testing data */

//Needed for GUI
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Needed for pie chart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.io.File; //Needed to access images
import java.util.ArrayList; //Needed for easily navigable lists
import java.util.Collections; //Needed to shuffle images

class GUI {
    //Total number of images found for each category
    private int clearCount = 0, foggyCount = 0, smokyCount = 0, goodCount = 0, badCount = 0;

    //Total number of training images user wants in each category
    private int clearTotal = 0, foggyTotal = 0, smokyTotal = 0, goodTotal = 0, badTotal = 0;

    //Total number of images (training and testing) user wants in each category
    private int clearLimit = 0, foggyLimit = 0, smokyLimit = 0, goodLimit = 0, badLimit = 0;

    //Lists of images found
    private ArrayList<File> clearImages = new ArrayList<>();
    private ArrayList<File> foggyImages = new ArrayList<>();
    private ArrayList<File> smokyImages = new ArrayList<>();
    private ArrayList<File> badImages = new ArrayList<>();
    private ArrayList<File> goodImages = new ArrayList<>();

    //Percentage(Default) of images to be used for testing
    private int cfsTestingPercent = 20;
    private int gbTestingPercent = 20;

    GUI(){
        //Find and count clear images
        clearCount = gatherImages("trainOrTest/clear", clearCount, clearImages);
        clearCount = gatherImages("testData/clear", clearCount, clearImages);
        clearCount = gatherImages("trainingData/clear", clearCount, clearImages);

        //Find and count foggy images
        foggyCount = gatherImages("trainOrTest/foggy", foggyCount, foggyImages);
        foggyCount = gatherImages("testData/foggy", foggyCount, foggyImages);
        foggyCount = gatherImages("trainingData/foggy", foggyCount, foggyImages);

        //Find and count smoky images
        smokyCount = gatherImages("trainOrTest/smoky", smokyCount, smokyImages);
        smokyCount = gatherImages("testData/smoky", smokyCount, smokyImages);
        smokyCount = gatherImages("trainingData", smokyCount, smokyImages);

        //Find and count good visibility images
        goodCount = gatherImages("trainOrTest/goodVisibility", goodCount, goodImages);
        goodCount = gatherImages("testGoodOrBad/good", goodCount, goodImages);
        goodCount = gatherImages("trainGoodOrBad", goodCount, goodImages);

        //Find and count bad visibility images
        badCount = gatherImages("trainOrTest/badVisibility", badCount, badImages);
        badCount = gatherImages("testGoodOrBad/bad", badCount, badImages);
        badCount = gatherImages("trainGoodOrBad/bad", badCount, badImages);

        displayInterface(); //Display the GUI
    }

    //Taken from Labeling Interface
    //Create and update data set for clear/smoky/foggy pie chart
    private PieDataset updateCSFDataSet(){
        DefaultPieDataset dataSet = new DefaultPieDataset();
        dataSet.setValue("Clear(" + clearCount + ")" , clearCount);
        dataSet.setValue("Smoky(" + smokyCount + ")", smokyCount);
        dataSet.setValue("Foggy(" + foggyCount + ")", foggyCount);
        return dataSet;
    }

    //Taken from Labeling Interface
    //Create and update data set for good/bad pie chart
    private PieDataset updateGBDataSet(){
        DefaultPieDataset dataSet = new DefaultPieDataset();
        dataSet.setValue("Good(" + goodCount + ")", goodCount);
        dataSet.setValue("Bad(" + badCount + ")", badCount);
        return dataSet;
    }

    //Taken from Labeling Interface
    //Create a pie chart and convert it to an ImageIcon
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

    //Find and count images from different folders
    private int gatherImages(String folderPath, int count, ArrayList<File> aList){
        try {
            File folder = new File(folderPath);
            File[] filesInFolder = folder.listFiles();

            for (File aFilesInFolder : filesInFolder) {
                //Find extension
                int dotLocation = aFilesInFolder.getCanonicalPath().lastIndexOf(".");
                String extension = aFilesInFolder.getCanonicalPath().substring(dotLocation + 1);

                //If image is a jpg or png, count it and add it to the appropriate list
                if (extension.equals("jpg") || extension.equals("png")) {
                    count += 1;
                    aList.add(aFilesInFolder);
                }
            }
        }
        catch (Exception e){
            System.out.println("Exception while gathering images from " + folderPath);
            System.out.println(e.toString());
        }
        return count;
    }

    //Move images to new locations
    private void partitionImages(ArrayList<File> fileList, int testingCount, int limit, String testFolder, String trainFolder, String trainTestFolder){
        Collections.shuffle(fileList); //Shuffle the list

        //Sort images into training, testing, and neither
        for (int i = 0; i < fileList.size(); i++){
            File newName;
            if(i < testingCount){
                newName = new File(testFolder + fileList.get(i).getName());
            }
            else if(i >= testingCount && i < limit){
                newName = new File(trainFolder + fileList.get(i).getName());
            }
            else{
                newName = new File(trainTestFolder + fileList.get(i).getName());
            }

            //Move the file
            fileList.get(i).renameTo(newName);
            fileList.set(i,newName);
        }
    }

    //Find smallest number in list
    private int min(int[] intList){
        int smallestElect = 999999999; //Pretend this is infinity
        for(int number : intList){
            if(number < smallestElect){
                smallestElect = number;
            }
        }
        return smallestElect;
    }

    //Create and display GUI elements
    private void displayInterface(){
        JFrame window = new JFrame();

        //Find the smallest clear/foggy/smoky count, and the smallest good/bad count
        int[] cfsCounts = {clearCount, foggyCount, smokyCount};
        int[] gbCounts = {goodCount, badCount};
        String smallestCFS = String.valueOf(min(cfsCounts));
        String smallestGB = String.valueOf(min(gbCounts));

        //Create header
        JTextArea header = new JTextArea("Partition Data: Training Data or Testing Data?");
        header.setEditable(false);
        header.setFont(new Font("Serif", Font.PLAIN, 20));
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(true);
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(header);

        //Create a JTextArea to say "Use " for each category total panel.
        JTextArea clearUse = new JTextArea("Use ");
        clearUse.setEditable(false);
        JTextArea foggyUse = new JTextArea("Use ");
        foggyUse.setEditable(false);
        JTextArea smokyUse = new JTextArea("Use ");
        smokyUse.setEditable(false);
        JTextArea goodUse = new JTextArea("Use ");
        goodUse.setEditable(false);
        JTextArea badUse = new JTextArea("Use ");
        badUse.setEditable(false);
        JTextArea shh = new JTextArea("SHHHH... "); //Used to make clear/foggy/smoky and good/bad sides look even
        shh.setEditable(false);
        shh.setForeground(Color.WHITE);

        //Set default number of training images for each category
        clearTotal = (int)(clearLimit * ((double)cfsTestingPercent/100.0));
        foggyTotal = (int)(foggyLimit * ((double)cfsTestingPercent/100.0));
        smokyTotal = (int)(smokyLimit * ((double)cfsTestingPercent/100.0));
        goodTotal = (int)(goodLimit * ((double)gbTestingPercent/100.0));
        badTotal = (int)(badLimit * ((double)gbTestingPercent/100.0));

        //Create JTextArea to allow user to edit clearLimit
        JTextField clearLimitField = new JTextField(smallestCFS, String.valueOf(clearCount).length());
        clearLimitField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        clearLimit = Integer.valueOf(clearLimitField.getText());
        clearLimitField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int number = Integer.valueOf(clearLimitField.getText());
                    if (number < 0) {
                        JOptionPane.showMessageDialog(window, "Please enter a number greater than 0");
                    } else if (number > clearCount) {
                        JOptionPane.showMessageDialog(window, "Please enter a number less than " + clearCount);
                    } else {
                        clearLimit = number;
                        clearTotal = (int)(clearLimit * ((double)cfsTestingPercent/100.0));
                    }
                }
                catch (NumberFormatException num){
                    JOptionPane.showMessageDialog(window, "Please enter a number between 0 and " + clearCount);
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(window,
                            "Something went wrong, please try again\n" + ex.toString());
                }
            }
        });

        //Create JTextArea to allow user to edit foggyLimit
        JTextField foggyLimitField = new JTextField(smallestCFS, String.valueOf(foggyCount).length());
        foggyLimitField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        foggyLimit = Integer.valueOf(foggyLimitField.getText());
        foggyLimitField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int number = Integer.valueOf(foggyLimitField.getText());
                    if (number < 0) {
                        JOptionPane.showMessageDialog(window, "Please enter a number greater than 0");
                    } else if (number > foggyCount) {
                        JOptionPane.showMessageDialog(window, "Please enter a number less than " + foggyCount);
                    } else {
                        foggyLimit = number;
                        foggyTotal = (int)(foggyLimit * ((double)cfsTestingPercent/100.0));
                    }
                }
                catch (NumberFormatException num){
                    JOptionPane.showMessageDialog(window, "Please enter a number between 0 and " + foggyCount);
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(window,
                            "Something went wrong, please try again\n" + ex.toString());
                }
            }
        });

        //Create JTextArea to allow user to edit smokyLimit
        JTextField smokyLimitField = new JTextField(smallestCFS, String.valueOf(smokyCount).length());
        smokyLimitField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        smokyLimit = Integer.valueOf(smokyLimitField.getText());
        smokyLimitField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int number = Integer.valueOf(smokyLimitField.getText());
                    if (number < 0) {
                        JOptionPane.showMessageDialog(window, "Please enter a number greater than 0");
                    } else if (number > smokyCount) {
                        JOptionPane.showMessageDialog(window, "Please enter a number less than " + smokyCount);
                    } else {
                        smokyLimit = number;
                        smokyTotal = (int)(smokyLimit * ((double)cfsTestingPercent/100.0));
                    }
                }
                catch (NumberFormatException num){
                    JOptionPane.showMessageDialog(window, "Please enter a number between 0 and " + smokyCount);
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(window,
                            "Something went wrong, please try again\n" + ex.toString());
                }
            }
        });

        //Create JTextArea to allow user to edit goodLimit
        JTextField goodLimitField = new JTextField(smallestGB, String.valueOf(goodCount).length());
        goodLimitField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        goodLimit = Integer.valueOf(goodLimitField.getText());
        goodLimitField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int number = Integer.valueOf(goodLimitField.getText());
                    if (number < 0) {
                        JOptionPane.showMessageDialog(window, "Please enter a number greater than 0");
                    } else if (number > goodCount) {
                        JOptionPane.showMessageDialog(window, "Please enter a number less than " + goodCount);
                    } else {
                        goodLimit = number;
                        goodTotal = (int)(goodLimit * ((double)gbTestingPercent/100.0));
                    }
                }
                catch (NumberFormatException num){
                    JOptionPane.showMessageDialog(window, "Please enter a number between 0 and " + goodCount);
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(window,
                            "Something went wrong, please try again\n" + ex.toString());
                }
            }
        });

        //Create JTextArea to allow user to edit badLimit
        JTextField badLimitField = new JTextField(smallestGB, String.valueOf(badCount).length());
        badLimitField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        badLimit = Integer.valueOf(badLimitField.getText());
        badLimitField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int number = Integer.valueOf(badLimitField.getText());
                    if (number < 0) {
                        JOptionPane.showMessageDialog(window, "Please enter a number greater than 0");
                    } else if (number > badCount) {
                        JOptionPane.showMessageDialog(window, "Please enter a number less than " + badCount);
                    } else {
                        badLimit = number;
                        badTotal = (int)(badLimit * ((double)gbTestingPercent/100.0));
                    }
                }
                catch (NumberFormatException num){
                    JOptionPane.showMessageDialog(window, "Please enter a number between 0 and " + badCount);
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(window,
                            "Something went wrong, please try again\n" + ex.toString());
                }
            }
        });

        //Used to make clear/foggy/smoky and good/bad sides look even
        JTextField shhField = new JTextField("", 3);
        shhField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        shhField.setForeground(Color.WHITE);
        shhField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(window, "What part of 'SHHHH!!' do you not understand??");
            }
        });

        //Create JTextField to display total number in each category
        JTextArea clearTotalDisplay = new JTextArea(" out of " + clearCount + " total clear images.");
        clearTotalDisplay.setEditable(false);
        JTextArea foggyTotalDisplay = new JTextArea(" out of " + foggyCount + " total foggy images.");
        foggyTotalDisplay.setEditable(false);
        JTextArea smokyTotalDisplay = new JTextArea(" out of " + smokyCount + " total smoky images.");
        smokyTotalDisplay.setEditable(false);
        JTextArea goodTotalDisplay = new JTextArea(" out of " + goodCount + " total good visibility images.");
        goodTotalDisplay.setEditable(false);
        JTextArea badTotalDisplay = new JTextArea(" out of " + badCount + " total bad visibility images.");
        badTotalDisplay.setEditable(false);

        //Used to make clear/foggy/smoky and good/bad sides look even
        JTextArea shhDisplay = new JTextArea("You can't see me. I'm invisible. SHHH I say!!");
        shhDisplay.setEditable(false);
        shhDisplay.setForeground(Color.WHITE);

        //JPanel that allows user to set clear image limit, and view the total
        JPanel clearTotalPanel = new JPanel();
        clearTotalPanel.setOpaque(true);
        clearTotalPanel.setBackground(Color.WHITE);
        clearTotalPanel.add(clearUse);
        clearTotalPanel.add(clearLimitField);
        clearTotalPanel.add(clearTotalDisplay);

        //JPanel that allows user to set foggy image limit, and view the total
        JPanel foggyTotalPanel = new JPanel();
        foggyTotalPanel.setOpaque(true);
        foggyTotalPanel.setBackground(Color.WHITE);
        foggyTotalPanel.add(foggyUse);
        foggyTotalPanel.add(foggyLimitField);
        foggyTotalPanel.add(foggyTotalDisplay);

        //JPanel that allows user to set smoky image limit, and view the total
        JPanel smokyTotalPanel = new JPanel();
        smokyTotalPanel.setOpaque(true);
        smokyTotalPanel.setBackground(Color.WHITE);
        smokyTotalPanel.add(smokyUse);
        smokyTotalPanel.add(smokyLimitField);
        smokyTotalPanel.add(smokyTotalDisplay);

        //JPanel that allows user to set good visibility image limit, and view the total
        JPanel goodTotalPanel = new JPanel();
        goodTotalPanel.setOpaque(true);
        goodTotalPanel.setBackground(Color.WHITE);
        goodTotalPanel.add(goodUse);
        goodTotalPanel.add(goodLimitField);
        goodTotalPanel.add(goodTotalDisplay);

        //JPanel that allows user to set bad visibility image limit, and view the total
        JPanel badTotalPanel = new JPanel();
        badTotalPanel.setOpaque(true);
        badTotalPanel.setBackground(Color.WHITE);
        badTotalPanel.add(badUse);
        badTotalPanel.add(badLimitField);
        badTotalPanel.add(badTotalDisplay);

        //Used to make clear/foggy/smoky and good/bad sides look even
        JPanel imInvisible = new JPanel();
        imInvisible.setOpaque(true);
        imInvisible.setBackground(Color.WHITE);
        imInvisible.add(shh);
        imInvisible.add(shhField);
        imInvisible.add(shhDisplay);


        //Add pie charts to allow user to see how many images are in each category
        JLabel cfsPieChart = new JLabel("",createChart("Clear/Smoky/Foggy",
                updateCSFDataSet()),JLabel.CENTER);
        JLabel gbPieChart = new JLabel("",createChart("GoodVisibility /Bad Visibility",
                updateGBDataSet()), JLabel.CENTER);

        //Add a JSlider to allow user to choose how they would like to split testing and training images
        JSlider cfsSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 20);
        cfsSlider.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        cfsSlider.setOpaque(true);
        cfsSlider.setBackground(Color.WHITE);
        cfsSlider.setMajorTickSpacing(10);
        cfsSlider.setMinorTickSpacing(1);
        cfsSlider.setPaintTicks(true);
        cfsSlider.setPaintLabels(true);

        //Display the current percentages
        JLabel cfsTestingLabel = new JLabel();
        cfsTestingLabel.setText("20% testing");
        JLabel cfsTrainingPercentage = new JLabel();
        cfsTrainingPercentage.setText("80% training");

        //Make appropriate changes to values when slider is moved
        cfsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                cfsTestingPercent = source.getValue();
                cfsTestingLabel.setText(source.getValue() + "% testing");
                cfsTrainingPercentage.setText((100 - source.getValue()) + "% training");
                clearTotal = (int)(clearLimit * ((double)cfsTestingPercent/100.0));
                foggyTotal = (int)(foggyLimit * ((double)cfsTestingPercent/100.0));
                smokyTotal = (int)(smokyLimit * ((double)cfsTestingPercent/100.0));
            }
        });

        //Add a JSlider to allow user to choose how they would like to split testing and training images
        JSlider gbSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 20);
        gbSlider.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        gbSlider.setOpaque(true);
        gbSlider.setBackground(Color.WHITE);
        gbSlider.setMajorTickSpacing(10);
        gbSlider.setMinorTickSpacing(1);
        gbSlider.setPaintTicks(true);
        gbSlider.setPaintLabels(true);

        //Display the current percentages
        JLabel gbTestingLabel = new JLabel();
        gbTestingLabel.setText("20% testing");
        JLabel gbTrainingPercentage = new JLabel();
        gbTrainingPercentage.setText("80% training");

        //Make appropriate changes to values when slider is moved
        gbSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                gbTestingPercent = source.getValue();
                gbTestingLabel.setText(source.getValue() + "% testing");
                gbTrainingPercentage.setText((100 - source.getValue()) + "% training");
                goodTotal = (int)(goodLimit * ((double)gbTestingPercent/100.0));
                badTotal = (int)(badLimit * ((double)gbTestingPercent/100.0));
            }
        });

        //Create a JPanel for the clear/foggy/smoky slider
        JPanel cfsSliderPanel = new JPanel();
        cfsSliderPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        cfsSliderPanel.setOpaque(true);
        cfsSliderPanel.setBackground(Color.WHITE);
        cfsSliderPanel.setLayout(new BoxLayout(cfsSliderPanel,BoxLayout.X_AXIS));
        cfsSliderPanel.add(cfsTestingLabel);
        cfsSliderPanel.add(cfsSlider);
        cfsSliderPanel.add(cfsTrainingPercentage);

        //Create a JPanel for the good/bad slider
        JPanel gbSliderPanel = new JPanel();
        gbSliderPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        gbSliderPanel.setOpaque(true);
        gbSliderPanel.setBackground(Color.WHITE);
        gbSliderPanel.setLayout(new BoxLayout(gbSliderPanel, BoxLayout.X_AXIS));
        gbSliderPanel.add(gbTestingLabel);
        gbSliderPanel.add(gbSlider);
        gbSliderPanel.add(gbTrainingPercentage);

        //A button to partition clear/foggy/smoky training and testing images
        JButton partitionCFSButton = new JButton("Partition Clear Foggy Smoky Images");
        partitionCFSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                partitionImages(clearImages, clearTotal, clearLimit,"testData/clear/",
                        "trainingData/clear/", "trainOrTest/clear/");
                partitionImages(foggyImages, foggyTotal, foggyLimit,"testData/foggy/",
                        "trainingData/foggy/", "trainOrTest/foggy/");
                partitionImages(smokyImages, smokyTotal, smokyLimit, "testData/smoky/",
                        "trainingData/smoky/", "trainOrTest/smoky/");
            }
        });
        JPanel cfsButtonPanel = new JPanel();
        cfsButtonPanel.setOpaque(true);
        cfsButtonPanel.setBackground(Color.WHITE);
        cfsButtonPanel.add(partitionCFSButton);

        //A button to partition good/bad visibility  training and testing images
        JButton partitionGBButton = new JButton("Partition Good/Bad Visibility Images");
        partitionGBButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                partitionImages(goodImages, goodTotal, goodLimit, "trainGoodOrBad/good/",
                        "testGoodOrBad/good/", "trainOrTest/good/");
                partitionImages(badImages, badTotal, badLimit, "trainGoodOrBar/bad/",
                        "testGoodOrBad/bad/", "trainOrTest/good/");
            }
        });
        JPanel gbButtonPanel = new JPanel();
        gbButtonPanel.setOpaque(true);
        gbButtonPanel.setBackground(Color.WHITE);
        gbButtonPanel.add(partitionGBButton);

        //A button to partition all category training and testing images
        JButton partitionBothButton = new JButton("PartitionBoth");
        partitionBothButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                partitionImages(clearImages, clearTotal, clearLimit,"testData/clear/",
                        "trainingData/clear/", "trainOrTest/clear/");
                partitionImages(foggyImages, foggyTotal, foggyLimit,"testData/foggy/",
                        "trainingData/foggy/", "trainOrTest/foggy/");
                partitionImages(smokyImages, smokyTotal, smokyLimit, "testData/smoky/",
                        "trainingData/smoky/", "trainOrTest/smoky/");
                partitionImages(goodImages, goodTotal, goodLimit, "trainGoodOrBad/good/",
                        "testGoodOrBad/good/", "trainOrTest/good/");
                partitionImages(badImages, badTotal, badLimit, "trainGoodOrBar/bad/",
                        "testGoodOrBad/bad/", "trainOrTest/good/");
            }
        });
        JPanel partitionBothPanel = new JPanel();
        partitionBothPanel.setOpaque(true);
        partitionBothPanel.setBackground(Color.WHITE);
        partitionBothPanel.add(partitionBothButton);

        //JPanel for partitioning Clear, Smoky, Foggy images
        JPanel clearFoggySmoky = new JPanel();
        clearFoggySmoky.setOpaque(true);
        clearFoggySmoky.setBackground(Color.WHITE);
        clearFoggySmoky.setLayout(new BoxLayout(clearFoggySmoky, BoxLayout.Y_AXIS));
        clearFoggySmoky.add(clearTotalPanel);
        clearFoggySmoky.add(foggyTotalPanel);
        clearFoggySmoky.add(smokyTotalPanel);
        clearFoggySmoky.add(cfsPieChart);
        clearFoggySmoky.add(cfsSliderPanel);
        clearFoggySmoky.add(cfsButtonPanel);

        //JPanel for partitioning Good, Bad visibility images
        JPanel goodBad = new JPanel();
        goodBad.setOpaque(true);
        goodBad.setBackground(Color.WHITE);
        goodBad.setLayout(new BoxLayout(goodBad, BoxLayout.Y_AXIS));
        goodBad.add(goodTotalPanel);
        goodBad.add(badTotalPanel);
        goodBad.add(imInvisible); //Used to make clear/foggy/smoky and good/bad sides look even
        goodBad.add(gbPieChart);
        goodBad.add(gbSliderPanel);
        goodBad.add(gbButtonPanel);

        //Set up JFrame and display GUI
        window.setTitle("Partition Data");
        window.setLayout(new BorderLayout());
        window.add(headerPanel, BorderLayout.NORTH);
        window.add(partitionBothPanel, BorderLayout.SOUTH);
        window.add(clearFoggySmoky, BorderLayout.WEST);
        window.add(goodBad, BorderLayout.EAST);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }
}
