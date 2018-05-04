/* This class is for clusters of similar images(found using K-Means clustering, and information related to them. */

import org.json.JSONObject; //Needed for pulling data from JSON
import javax.swing.*;  //Needed for GUI
import java.awt.*;
import java.net.URL; //Needed for adding images to GUI component
import java.util.ArrayList; //For easily navigable lists

public class Cluster {
    private ArrayList<LabelImage> imageList = new ArrayList<>(); //List of clustered images
    private int clusterNum,clusterTotal; //Current cluster number, and total number of images in cluster
    private int redAverage,blueAverage,greenAverage,yellowAverage; //Average number of red/blue/green/yellow pixels
    private int pinkAverage,grayAverage,blackAverage,whiteAverage; //Average number of pink/gray/black/white pixels
    private Double tempAverage, humidityAverage,dewPointAverage; //Average temperature, dew point, and humidity
    private JScrollPane imageGallery; //Displays similar images to a specific image of interest

    //Cluster constructor
    Cluster(JSONObject clusteredImages){
        try {
            //Pull variables from file
            clusterNum = clusteredImages.getInt("clusterNum") + 1;
            clusterTotal = clusteredImages.getInt("clusterTotal");
            redAverage = clusteredImages.getInt("redAvg");
            blueAverage = clusteredImages.getInt("blueAvg");
            greenAverage = clusteredImages.getInt("greenAvg");
            yellowAverage = clusteredImages.getInt("yellowAvg");
            pinkAverage = clusteredImages.getInt("pinkAvg");
            grayAverage = clusteredImages.getInt("grayAvg");
            blackAverage = clusteredImages.getInt("blackAvg");
            whiteAverage = clusteredImages.getInt("whiteAvg");
            tempAverage = clusteredImages.getDouble("tempAvg");
            humidityAverage = clusteredImages.getDouble("humidityAvg");
            dewPointAverage = clusteredImages.getDouble("dewPointAvg");
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }

    //Generate a new image gallery for the current image being labeled in the GUI
    public void setImageGallery(LabelImage currentImage) {
        //Get dimensions for an images 50% smaller than current image. This will make the gallery much smaller
        String height = String.valueOf((new ImageIcon(imageList.get(0).getPath()).getIconHeight()/100) * 50);
        String width = String.valueOf((new ImageIcon(imageList.get(0).getPath()).getIconWidth()/100) * 50);

        if(imageList.size() == 0){
            String noImages = "<HTML><br/><Strong><center>No similar images to display!" +
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
        //for (LabelImage anImageList : imageList) {
        for (int i = 0; i < imageList.size() && i < 9; i++){
            if (!(imageList.get(i).getPath().equals(currentImage.getPath()))) {
                imageCount++;
                try {
                    //Add image to HTMLString
                    HTMLString += ("<img width = " + width + " height = " + height + " src=" +
                            new URL("file:///" + imageList.get(i).getPath()) + ">");
                } catch (Exception E) {
                    System.out.println(E.getMessage());
                }

                //Add a new line if needed to make sure images in rows of 3
                if (imageCount > 0 && (imageCount) % 3 == 0) {
                    HTMLString += "<br/>";
                }
            }
        }
        HTMLString += "</html>"; //Finish HTML String
        JLabel images = new JLabel(HTMLString); //Create new JLabel to hold gallery
        imageGallery = new JScrollPane(images);
        images.setHorizontalAlignment(JLabel.CENTER);
        Dimension newDimension = imageGallery.getPreferredSize();
        newDimension.width += imageGallery.getVerticalScrollBar().getPreferredSize().width;
        newDimension.height = (Integer.valueOf(height) * 3) + 15;
        imageGallery.setPreferredSize(newDimension);
    }

    //Add a new image to the cluster
    public void addImage(LabelImage imageToAdd){
        imageList.add(imageToAdd);
    }

    public JScrollPane getImageGallery(){
        return imageGallery;
    }

    public ArrayList<LabelImage> getImageList() {
        return imageList;
    }

    public int getClusterNum(){
        return clusterNum;
    }

    public int getClusterTotal() {
        return clusterTotal;
    }

    public int getBlueAverage() {
        return blueAverage;
    }

    public int getGrayAverage() {
        return grayAverage;
    }

    public int getBlackAverage() {
        return blackAverage;
    }

    public int getGreenAverage() {
        return greenAverage;
    }

    public int getPinkAverage() {
        return pinkAverage;
    }

    public int getRedAverage() {
        return redAverage;
    }

    public int getWhiteAverage() {
        return whiteAverage;
    }

    public int getYellowAverage() {
        return yellowAverage;
    }

    public Double getDewPointAverage() {
        return dewPointAverage;
    }

    public Double getHumidityAverage() {
        return humidityAverage;
    }

    public Double getTempAverage() {
        return tempAverage;
    }
}
