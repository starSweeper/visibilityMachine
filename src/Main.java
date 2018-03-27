/*
    Image Labeling Interface. Developed by Amanda Joy Panell 2017/2018
    A program to label images to test and train Visibility Machine
    "Smart labeling" features added January 2018

    Smart Labeling Features:
        ~Similar images are grouped together into clusters using K-Means Clustering
        ~User is able to rename images during labeling
        ~User is able to sort images by cluster or by date
        ~Current image is compared to other images from their cluster and other images taken in the same month
        ~Image statistics are displayed on the right of the image
        ~Similar images (same cluster) are displayed on the left of the image
        ~Progress bar and pie charts show number of unlabeled images, and how many images are labeled in each category

    See readme.txt, or readme.pdf for more information.
*/

import org.json.*; //Needed to easily pull data from JSON files
import java.io.*; //Needed to read from/write to files
import java.nio.file.Files; //Needed to read from/write to files
import java.nio.file.Paths; //Needed to read from/write to files
import java.util.ArrayList;  //For easily navigable lists

public class Main {
    public static void main(String[] args){
        ArrayList<LabelImage> newImages = new ArrayList<>();
        ArrayList<String> monthList = new ArrayList<>();

        try {
            //Get image averages for each month from "new_image_month_stats.txt" file
            FileInputStream monthFile = new FileInputStream("new_image_month_stats.txt");
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(monthFile));
            String newLine; //For reading file line by line
            while((newLine = buffRead.readLine()) != null){
                monthList.add(newLine);
            }
            buffRead.close(); //Close file

            //Get cluster information (including image paths) from "clusters.json"
            JSONObject clusterJSON;
            String clusterString = new String(Files.readAllBytes(Paths.get("clusters.json")));
            clusterJSON = new JSONObject(clusterString);
            JSONArray groups = clusterJSON.getJSONArray("clusters");
            //Create a new cluster object for each cluster in file
            for(int i = 0; i < groups.length(); i++){
                JSONObject cluster = groups.getJSONObject(i);
                JSONArray clusterImages = cluster.getJSONArray("images");
                Cluster newCluster = new Cluster(cluster);
                //For each image in the cluster, create a new LabelImage item
                for(int j = 0; j < clusterImages.length(); j++){
                    JSONObject image = clusterImages.getJSONObject(j);
                    LabelImage newImage = new LabelImage(image, newCluster);
                    //Add month information to new image
                    for (String aMonthList : monthList) {
                        String[] monthSplit = aMonthList.split(" ");
                        if (monthSplit[1].equals(newImage.getMonth())) {
                            newImage.setMonthlyHTML(aMonthList);
                        }
                    }
                    newCluster.addImage(newImage);
                    newImages.add(newImage);
                }
            }

            //Generate Graphical User Interface
            GUI displayReady = new GUI(newImages, clusterJSON, new File("clusters.json"));
            displayReady.interfaceG();
        }
        catch(IndexOutOfBoundsException exception){
            System.out.println("Looks like there are no images left to label!");
            System.out.println(exception.toString());
        }
        catch(Exception E){
            System.out.println("Error in Main function");
            System.out.println(E.toString());
        }
    }
}