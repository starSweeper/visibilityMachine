/* This class is for calculating the averages of image groups and generating HTML code to display them in the GUI */

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

public class Categories {
    //ArrayLists to help calculate averages
    private ArrayList<Double> tempList = new ArrayList<>();
    private ArrayList<Double> humidityList = new ArrayList<>();
    private ArrayList<Double> dewPointList = new ArrayList<>();
    private ArrayList<Integer> redList = new ArrayList<>();
    private ArrayList<Integer> blueList = new ArrayList<>();
    private ArrayList<Integer> greenList = new ArrayList<>();
    private ArrayList<Integer> yellowList = new ArrayList<>();
    private ArrayList<Integer> pinkList = new ArrayList<>();
    private ArrayList<Integer> grayList = new ArrayList<>();
    private ArrayList<Integer> blackList = new ArrayList<>();
    private ArrayList<Integer> whiteList = new ArrayList<>();
    private String group = ""; //Which category is this? String for display purposes.

    private ArrayList<LabelImage> imageList = new ArrayList<>(); //A list of all the images in this category
    private JScrollPane imageGallery; //Displays similar images to a specific image of interest

    //Averages for all images in a category
    private double tempAvg = 0.0, humidityAvg = 0.0 ,dewPointAvg = 0.0;
    private int redAvg = 0, blueAvg = 0, greenAvg = 0, yellowAvg = 0;
    private int pinkAvg = 0, grayAvg = 0, blackAvg = 0,whiteAvg = 0;

    //Calculate averages for an ArrayList<Double>
    private double calculateDoubleAverages(ArrayList<Double> theList){
        double listTotal = 0;

        for (double aTheList : theList) {
            listTotal += aTheList;
        }

        double average = listTotal / theList.size();

        return Math.round(average * 100)/100;
    }

    //Calculate averages for an ArrayList<Integer>
    private int calculateIntegerAverages(ArrayList<Integer> theList){
        int listTotal = 0;

        for (int aTheList : theList) {
            listTotal += aTheList;
        }

        return listTotal / theList.size();
    }

    //Updates averages and array lists for each image group
    public void updateLists(LabelImage currentImage){
        tempList.add(Double.valueOf(currentImage.getTempF()));
        tempAvg = calculateDoubleAverages(tempList);
        humidityList.add(Double.valueOf(currentImage.getHumidity()));
        humidityAvg = calculateDoubleAverages(humidityList);
        dewPointList.add(Double.valueOf(currentImage.getDewPoint()));
        dewPointAvg = calculateDoubleAverages(dewPointList);
        redList.add(Integer.valueOf(currentImage.getRedCount()));
        redAvg = calculateIntegerAverages(redList);
        blueList.add(Integer.valueOf(currentImage.getBlueCount()));
        blueAvg = calculateIntegerAverages(blueList);
        greenList.add(Integer.valueOf(currentImage.getGreenCount()));
        greenAvg = calculateIntegerAverages(greenList);
        yellowList.add(Integer.valueOf(currentImage.getYellowCount()));
        yellowAvg = calculateIntegerAverages(yellowList);
        pinkList.add(Integer.valueOf(currentImage.getPinkCount()));
        pinkAvg = calculateIntegerAverages(pinkList);
        grayList.add(Integer.valueOf(currentImage.getGrayCount()));
        grayAvg = calculateIntegerAverages(grayList);
        blackList.add(Integer.valueOf(currentImage.getBlackCount()));
        blackAvg = calculateIntegerAverages(blackList);
        whiteList.add(Integer.valueOf(currentImage.getWhiteCount()));
        whiteAvg = calculateIntegerAverages(whiteList);
        imageList.add(currentImage);
    }

    //Creates and returns HTML string with averages for an image group
    public String generateCategoryHTML(LabelImage currentImage, String whichGroup){
        group = whichGroup;

        String tempHTML = "<html><strong>Averages for " + whichGroup + " Images:</strong><br/>";
        //Cluster parentCluster = currentImage.getParentCluster();

        if(Double.parseDouble(currentImage.getTempF()) >= (tempAvg + ((tempAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Temperature: " + tempAvg + "</font><br/>";
        }
        else if(Double.parseDouble(currentImage.getTempF()) < (tempAvg - ((tempAvg/100) * 10))){
            tempHTML += "<font color\"blue\">Temperature: " + tempAvg + "</font><br/>";
        }
        else{
            tempHTML += "Temperature: " + tempAvg + "<br/>";
        }

        if(Double.parseDouble(currentImage.getHumidity()) >= (humidityAvg + ((humidityAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Humidity: " + humidityAvg + "</font><br/>";
        }
        else if(Double.parseDouble(currentImage.getHumidity()) < (humidityAvg - ((humidityAvg/100) * 10))){
            tempHTML += "<font color\"blue\">Humidity: " + humidityAvg + "</font><br/>";
        }
        else{
            tempHTML += "Humidity: " + humidityAvg + "<br/>";
        }

        if(Double.parseDouble(currentImage.getDewPoint()) >= (dewPointAvg + ((dewPointAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Dew Point: " + dewPointAvg + "</font><br/>";
        }
        else if(Double.parseDouble(currentImage.getDewPoint()) < (dewPointAvg - ((dewPointAvg/100) * 10))){
            tempHTML += "<font color\"blue\">Dew Point: " + dewPointAvg + "</font><br/>";
        }
        else{
            tempHTML += "Dew Point: " + dewPointAvg + "<br/>";
        }

        if(Double.parseDouble(currentImage.getRedCount()) >= (redAvg + ((redAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Red Pixels: " + redAvg + "</font>&emsp;";
        }
        else if(Double.parseDouble(currentImage.getRedCount()) < (redAvg - ((redAvg/100) * 10))){
            tempHTML += "<font color=\"blue\">Red Pixels: " + redAvg + "</font>&emsp;";
        }
        else{
            tempHTML += "Red Pixels: " + redAvg + "&emsp;";
        }

        if(Double.parseDouble(currentImage.getBlueCount()) >= (blackAvg + ((blueAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Blue Pixels: " + blueAvg + "</font><br/>";
        }
        else if(Double.parseDouble(currentImage.getBlueCount()) < (blueAvg - ((blueAvg/100) * 10))){
            tempHTML += "<font color\"blue\">Blue Pixels: " + blueAvg + "</font><br/>";
        }
        else{
            tempHTML += "Blue Pixels: " + blueAvg + "<br/>";
        }

        if(Double.parseDouble(currentImage.getGreenCount()) >= (greenAvg + ((greenAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Green Pixels: " + greenAvg + "</font>&emsp;";
        }
        else if(Double.parseDouble(currentImage.getGreenCount()) < (greenAvg - ((greenAvg/100) * 10))){
            tempHTML += "<font color=\"blue\">Green Pixels: " + greenAvg + "</font>&emsp;";
        }
        else{
            tempHTML += "Green Pixels: " + greenAvg + "&emsp;";
        }

        if(Double.parseDouble(currentImage.getYellowCount()) >= (yellowAvg + ((yellowAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Yellow Pixels: " + yellowAvg + "</font><br/>";
        }
        else if(Double.parseDouble(currentImage.getYellowCount()) < (yellowAvg - ((yellowAvg/100) * 10))){
            tempHTML += "<font color\"blue\">Yellow Pixels: " + yellowAvg + "</font><br/>";
        }
        else{
            tempHTML += "Yellow Pixels: " + yellowAvg + "<br/>";
        }

        if(Double.parseDouble(currentImage.getPinkCount()) >= (pinkAvg + ((pinkAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Pink Pixels: " + pinkAvg+ "</font>&emsp;";
        }
        else if(Double.parseDouble(currentImage.getPinkCount()) < (pinkAvg - ((pinkAvg/100) * 10))){
            tempHTML += "<font color=\"blue\">Pink Pixels: " + pinkAvg + "</font>&emsp;";
        }
        else{
            tempHTML += "Pink Pixels: " + pinkAvg + "&emsp;";
        }

        if(Double.parseDouble(currentImage.getGrayCount()) >= (grayAvg + ((grayAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Gray Pixels: " + grayAvg + "</font><br/>";
        }
        else if(Double.parseDouble(currentImage.getGrayCount()) < (grayAvg - ((grayAvg/100) * 10))){
            tempHTML += "<font color\"blue\">Gray Pixels: " + grayAvg + "</font><br/>";
        }
        else{
            tempHTML += "Gray Pixels: " + grayAvg + "<br/>";
        }

        if(Double.parseDouble(currentImage.getBlackCount()) >= (blackAvg + ((blackAvg/100) * 10))){
            tempHTML += "<font color=\"red\">Black Pixels: " + blackAvg + "</font>&emsp;";
        }
        else if(Double.parseDouble(currentImage.getBlackCount()) < (blackAvg - ((blackAvg/100) * 10))){
            tempHTML += "<font color=\"blue\">Black Pixels: " + blackAvg + "</font>&emsp;";
        }
        else{
            tempHTML += "Black Pixels: " + blackAvg + "&emsp;";
        }

        if(Double.parseDouble(currentImage.getWhiteCount()) >= (whiteAvg + ((whiteAvg/100) * 10))){
            tempHTML += "<font color=\"red\">White Pixels: " + whiteAvg + "</font><br/>";
        }
        else if(Double.parseDouble(currentImage.getWhiteCount()) < (whiteAvg - ((whiteAvg/100) * 10))){
            tempHTML += "<font color\"blue\">White Pixels: " + whiteAvg + "</font><br/>";
        }
        else{
            tempHTML += "White Pixels: " + whiteAvg + "<br/></html>";
        }

        return tempHTML;
    }

    //Returns image gallery
    public JScrollPane getImageGallery(){
        return imageGallery;
    }

    //Generate a new image gallery for the current image being labeled in the GUI
    public void setImageGallery(LabelImage currentImage) {
        //Get dimensions for an images 50% smaller than current image. This will make the gallery much smaller
        String height = String.valueOf((new ImageIcon(currentImage.getPath()).getIconHeight()/100) * 50);
        String width = String.valueOf((new ImageIcon(currentImage.getPath()).getIconWidth()/100) * 50);

        if(imageList.size() == 0){
            String noImages = "<HTML><br/><Strong><center>No " + group + " images in this category yet!" +
                    "</center></Strong></html>";
            JLabel images = new JLabel(noImages); //Create new JLabel to hold gallery
            imageGallery = new JScrollPane(images);
            images.setHorizontalAlignment(JLabel.CENTER);
            Dimension newDimension = imageGallery.getPreferredSize();
            newDimension.width += imageGallery.getVerticalScrollBar().getPreferredSize().width;
            newDimension.height = (Integer.valueOf(height) * 3) + 15;
            imageGallery.setPreferredSize(newDimension);

            return;
        }

        String HTMLString = "<html>"; //Fresh start for HTML code
        int imageCount = 0;

        //For each image (Until there are 9 images in gallery), add image to gallery if it is not the current image
        for (LabelImage anImageList : imageList) {
            if (!(anImageList.getPath().equals(currentImage.getPath()))) {
                imageCount++;
                try {
                    //Add image to HTMLString
                    HTMLString += ("<img width = " + width + " height = " + height + " src=" +
                            new URL("file:///" + anImageList.getPath()) + ">");
                } catch (Exception E) {
                    System.out.println(E.getMessage());
                }

                //Add a new line if needed to make sure images in rows of 3
                if (imageCount > 0 && (imageCount) % 3 == 0) {
                    HTMLString += "<br/>";
                }
            }
        }
        HTMLString += "<br/><Strong><center>Similar images</center></Strong></html>"; //Finish HTML String
        JLabel images = new JLabel(HTMLString); //Create new JLabel to hold gallery
        imageGallery = new JScrollPane(images);
        images.setHorizontalAlignment(JLabel.CENTER);
        Dimension newDimension = imageGallery.getPreferredSize();
        newDimension.width += imageGallery.getVerticalScrollBar().getPreferredSize().width;
        newDimension.height = (Integer.valueOf(height) * 3) + 15;
        imageGallery.setPreferredSize(newDimension);
    }
}