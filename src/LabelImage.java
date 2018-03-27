/* This class is for images to be labeled and information related to them. */

import org.json.JSONObject; //Needed for pulling data from JSON
import javax.swing.*; //Needed for GUI
import java.io.File; //Needed to read from/write to files

public class LabelImage {
    private String time; //Time the image was taken
    private String date; //Date the image was taken
    private String tempF; //Temperature at the time image was taken
    private String humidity; //Humidity at the time image was taken
    private String dewPoint; //Dew Point at the time image was taken
    private String path; //File path of image (Should be relative to visibilityMachine folder)
    private String name; //Name of image
    private String extension; //Extension of image (should be .png or .jpg)
    private String currentImageHTML,monthlyHTML,clusterHTML; //Strings containing HTML code for image statistics
    private String id, month, monthABC; //Image id, month image was taken, and that same month as a word, not a number
    private String redCount,blueCount,greenCount,yellowCount; //Number of red/blue/green/yellow pixels in image
    private String pinkCount,grayCount,blackCount,whiteCount; //Number of pink/gray/black/white pixels in image
    private Cluster parentCluster; //The cluster this image belongs to.
    private JSONObject selfPortrait; //A JSONObject with all the information about the image

    //LabelImage constructor
    LabelImage(JSONObject newImage, Cluster cluster){
        try {
            selfPortrait = newImage;
            name = newImage.getString("imageTitle");
            path = newImage.getString("imagePath");
            time = newImage.getString("time");
            time = time.substring(0,2) + ":" + time.substring(time.length() - 2);
            date = newImage.getString("date");
            month = date.replace(date.substring(date.length() - 4),"");

            setMonthABC();
            String year = date.substring(date.length() - 2);
            String day = date.substring(month.length(),date.length() - 2);
            date = day + " " + monthABC.substring(0,3).toUpperCase() + " " + year;
            tempF = String.valueOf(newImage.getInt("tempInF"));
            humidity = String.valueOf(newImage.getInt("humidity"));
            dewPoint = String.valueOf(newImage.getInt("dewPointInF"));
            extension = path.substring(path.length() - 4);
            id = String.valueOf(newImage.getInt("imageID"));
            redCount = String.valueOf(newImage.getInt("redCount"));
            blueCount = String.valueOf(newImage.getInt("blueCount"));
            greenCount = String.valueOf(newImage.getInt("greenCount"));
            yellowCount = String.valueOf(newImage.getInt("yellowCount"));
            pinkCount = String.valueOf(newImage.getInt("pinkCount"));
            grayCount = String.valueOf(newImage.getInt("grayCount"));
            blackCount = String.valueOf(newImage.getInt("blackCount"));
            whiteCount = String.valueOf(newImage.getInt("whiteCount"));
            parentCluster = cluster;
            setCurrentImageHTML();
            setClusterHTML();
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }

    //Update the path of an image after it has been changed somehow
    public void updatePath(String newPath){
        path = newPath;
    }

    //Change the name of an image
    public void changeName(String newName){
        String newPath = path.replaceAll(name,newName); //Updated path
        File image = new File(path);
        //If the user did not include the extension in the new name, add it
        if(newName.length() > 3 && newName.substring(newName.length() - 4).equals(extension)) {
            newName += extension;
        }

        //Rename image and update references to it
        try{
            image.renameTo(new File(path.replaceAll(name,newName)));
            //Rename image in JSONObject
            selfPortrait.put("imageTitle",newName);
            selfPortrait.put("imagePath", path.replaceAll(name,newName));

            JOptionPane.showMessageDialog(null,"File successfully renamed!");
            name = newName; //Update object with new name
            path = newPath; //Update object with new path
        }catch(Exception E){
            JOptionPane.showMessageDialog(null,"There was an error. File not renamed!");
        }
    }

    /*Getters*/
    public String getClusterHTML(){
        return clusterHTML;
    }

    public String getMonthlyHTML(){
        return monthlyHTML;
    }

    public String getCurrentImageHTML(){
        return currentImageHTML;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    public String getTime(){
        return time;
    }

    public String getPath(){
        return path;
    }

    public String getMonth(){
        return month;
    }

    public int getId(){
        return Integer.parseInt(id);
    }

    public String getTempF() {
        return tempF;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getDewPoint(){
        return dewPoint;
    }

    public String getRedCount(){
        return redCount;
    }

    public String getBlueCount() {
        return blueCount;
    }

    public String getGreenCount() {
        return greenCount;
    }

    public String getYellowCount(){
        return yellowCount;
    }

    public String getGrayCount(){
        return grayCount;
    }

    public String getBlackCount() {
        return blackCount;
    }

    public String getPinkCount() {
        return pinkCount;
    }

    public String getWhiteCount() {
        return whiteCount;
    }

    public Cluster getParentCluster(){
        return parentCluster;
    }

    /*Setters*/
    private void setMonthABC(){
        switch(Integer.parseInt(month)){
            case 1:
                monthABC = "January";
                break;
            case 2:
                monthABC = "February";
                break;
            case 3:
                monthABC = "March";
                break;
            case 4:
                monthABC = "April";
                break;
            case 5:
                monthABC = "May";
                break;
            case 6:
                monthABC = "June";
                break;
            case 7:
                monthABC = "July";
                break;
            case 8:
                monthABC = "August";
                break;
            case 9:
                monthABC = "September";
                break;
            case 10:
                monthABC = "October";
                break;
            case 11:
                monthABC = "November";
                break;
            case 12:
                monthABC = "December";
                break;
            default:
                monthABC = "Month " + month;
        }
    }

    private void setClusterHTML(){
        clusterHTML = "<html><strong>Averages for Cluster #" + parentCluster.getClusterNum() + " : (" + parentCluster.getClusterTotal() + " Images)</strong><br/>";

        if(Double.parseDouble(tempF) >= (parentCluster.getTempAverage() + ((parentCluster.getTempAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Temperature: " + parentCluster.getTempAverage() + "</font><br/>";
        }
        else if(Double.parseDouble(tempF) < (parentCluster.getTempAverage() - ((parentCluster.getTempAverage()/100) * 10))){
            clusterHTML += "<font color\"blue\">Temperature: " + parentCluster.getTempAverage() + "</font><br/>";
        }
        else{
            clusterHTML += "Temperature: " + parentCluster.getTempAverage() + "<br/>";
        }

        if(Double.parseDouble(humidity) >= (parentCluster.getHumidityAverage() + ((parentCluster.getHumidityAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Humidity: " + parentCluster.getHumidityAverage() + "</font><br/>";
        }
        else if(Double.parseDouble(humidity) < (parentCluster.getHumidityAverage() - ((parentCluster.getHumidityAverage()/100) * 10))){
            clusterHTML += "<font color\"blue\">Humidity: " + parentCluster.getHumidityAverage() + "</font><br/>";
        }
        else{
            clusterHTML += "Humidity: " + parentCluster.getHumidityAverage() + "<br/>";
        }

        if(Double.parseDouble(dewPoint) >= (parentCluster.getDewPointAverage() + ((parentCluster.getDewPointAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Dew Point: " + parentCluster.getDewPointAverage() + "</font><br/>";
        }
        else if(Double.parseDouble(dewPoint) < (parentCluster.getDewPointAverage() - ((parentCluster.getDewPointAverage()/100) * 10))){
            clusterHTML += "<font color\"blue\">Dew Point: " + parentCluster.getDewPointAverage() + "</font><br/>";
        }
        else{
            clusterHTML += "Dew Point: " + parentCluster.getDewPointAverage() + "<br/>";
        }

        if(Double.parseDouble(redCount) >= (parentCluster.getRedAverage() + ((parentCluster.getRedAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Red Pixels: " + parentCluster.getRedAverage() + "</font>&emsp;";
        }
        else if(Double.parseDouble(redCount) < (parentCluster.getRedAverage() - ((parentCluster.getRedAverage()/100) * 10))){
            clusterHTML += "<font color=\"blue\">Red Pixels: " + parentCluster.getRedAverage() + "</font>&emsp;";
        }
        else{
            clusterHTML += "Red Pixels: " + parentCluster.getRedAverage() + "&emsp;";
        }

        if(Double.parseDouble(blueCount) >= (parentCluster.getBlueAverage() + ((parentCluster.getBlueAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Blue Pixels: " + parentCluster.getBlueAverage() + "</font><br/>";
        }
        else if(Double.parseDouble(blueCount) < (parentCluster.getBlueAverage() - ((parentCluster.getBlueAverage()/100) * 10))){
            clusterHTML += "<font color\"blue\">Blue Pixels: " + parentCluster.getBlueAverage() + "</font><br/>";
        }
        else{
            clusterHTML += "Blue Pixels: " + parentCluster.getBlueAverage() + "<br/>";
        }

        if(Double.parseDouble(greenCount) >= (parentCluster.getGreenAverage() + ((parentCluster.getGreenAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Green Pixels: " + parentCluster.getGreenAverage() + "</font>&emsp;";
        }
        else if(Double.parseDouble(greenCount) < (parentCluster.getGreenAverage() - ((parentCluster.getGreenAverage()/100) * 10))){
            clusterHTML += "<font color=\"blue\">Green Pixels: " + parentCluster.getGreenAverage() + "</font>&emsp;";
        }
        else{
            clusterHTML += "Green Pixels: " + parentCluster.getGreenAverage() + "&emsp;";
        }

        if(Double.parseDouble(yellowCount) >= (parentCluster.getYellowAverage() + ((parentCluster.getYellowAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Yellow Pixels: " + parentCluster.getYellowAverage() + "</font><br/>";
        }
        else if(Double.parseDouble(yellowCount) < (parentCluster.getYellowAverage() - ((parentCluster.getYellowAverage()/100) * 10))){
            clusterHTML += "<font color\"blue\">Yellow Pixels: " + parentCluster.getYellowAverage() + "</font><br/>";
        }
        else{
            clusterHTML += "Yellow Pixels: " + parentCluster.getYellowAverage() + "<br/>";
        }

        if(Double.parseDouble(pinkCount) >= (parentCluster.getPinkAverage() + ((parentCluster.getPinkAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Pink Pixels: " + parentCluster.getPinkAverage() + "</font>&emsp;";
        }
        else if(Double.parseDouble(pinkCount) < (parentCluster.getPinkAverage() - ((parentCluster.getPinkAverage()/100) * 10))){
            clusterHTML += "<font color=\"blue\">Pink Pixels: " + parentCluster.getPinkAverage() + "</font>&emsp;";
        }
        else{
            clusterHTML += "Pink Pixels: " + parentCluster.getPinkAverage() + "&emsp;";
        }

        if(Double.parseDouble(grayCount) >= (parentCluster.getGrayAverage() + ((parentCluster.getGrayAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Gray Pixels: " + parentCluster.getGrayAverage() + "</font><br/>";
        }
        else if(Double.parseDouble(grayCount) < (parentCluster.getGrayAverage() - ((parentCluster.getGrayAverage()/100) * 10))){
            clusterHTML += "<font color\"blue\">Gray Pixels: " + parentCluster.getGrayAverage() + "</font><br/>";
        }
        else{
            clusterHTML += "Gray Pixels: " + parentCluster.getGrayAverage() + "<br/>";
        }

        if(Double.parseDouble(blackCount) >= (parentCluster.getBlackAverage() + ((parentCluster.getBlackAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">Black Pixels: " + parentCluster.getBlackAverage() + "</font>&emsp;";
        }
        else if(Double.parseDouble(blackCount) < (parentCluster.getBlackAverage() - ((parentCluster.getBlackAverage()/100) * 10))){
            clusterHTML += "<font color=\"blue\">Black Pixels: " + parentCluster.getBlackAverage() + "</font>&emsp;";
        }
        else{
            clusterHTML += "Black Pixels: " + parentCluster.getBlackAverage() + "&emsp;";
        }

        if(Double.parseDouble(whiteCount) >= (parentCluster.getWhiteAverage() + ((parentCluster.getWhiteAverage()/100) * 10))){
            clusterHTML += "<font color=\"red\">White Pixels: " + parentCluster.getWhiteAverage() + "</font><br/>";
        }
        else if(Double.parseDouble(whiteCount) < (parentCluster.getWhiteAverage() - ((parentCluster.getWhiteAverage()/100) * 10))){
            clusterHTML += "<font color\"blue\">White Pixels: " + parentCluster.getWhiteAverage() + "</font><br/>";
        }
        else{
            clusterHTML += "White Pixels: " + parentCluster.getWhiteAverage() + "<br/></html>";
        }
    }

    public void setMonthlyHTML(String monthString){
        String[] monthStringBits = monthString.split(" ");
        int red = Integer.parseInt(monthStringBits[2]);
        int blue = Integer.parseInt(monthStringBits[3]);
        int green = Integer.parseInt(monthStringBits[4]);
        int yellow = Integer.parseInt(monthStringBits[5]);
        int pink = Integer.parseInt(monthStringBits[6]);
        int gray = Integer.parseInt(monthStringBits[7]);
        int black = Integer.parseInt(monthStringBits[8]);
        int white = Integer.parseInt(monthStringBits[9]);
        Double monthTemp = Double.parseDouble(monthStringBits[10]);
        Double monthHumid = Double.parseDouble(monthStringBits[11]);
        Double monthDewPoint = Double.parseDouble(monthStringBits[12]);

        monthlyHTML = "<html><strong>Averages for " + monthABC + ":</strong><br/>";

        if(Double.parseDouble(tempF) >= (monthTemp + ((monthTemp/100) * 10))){
            monthlyHTML += "<font color=\"red\">Temperature: " + monthTemp + "</font><br/>";
        }
        else if(Double.parseDouble(tempF) < (monthTemp - ((monthTemp/100) * 10))){
            monthlyHTML += "<font color\"blue\">Temperature: " + monthTemp + "</font><br/>";
        }
        else{
            monthlyHTML += "Temperature: " + monthTemp + "<br/>";
        }

        if(Double.parseDouble(humidity) >= (monthHumid + ((monthHumid/100) * 10))){
            monthlyHTML += "<font color=\"red\">Humidity: " + monthHumid + "</font><br/>";
        }
        else if(Double.parseDouble(humidity) < (monthHumid - ((monthHumid/100) * 10))){
            monthlyHTML += "<font color\"blue\">Humidity: " + monthHumid + "</font><br/>";
        }
        else{
            monthlyHTML += "Humidity: " + monthHumid + "<br/>";
        }

        if(Double.parseDouble(dewPoint) >= (monthDewPoint + ((monthDewPoint/100) * 10))){
            monthlyHTML += "<font color=\"red\">Dew Point: " + monthDewPoint + "</font><br/>";
        }
        else if(Double.parseDouble(dewPoint) < (monthDewPoint - ((monthDewPoint/100) * 10))){
            monthlyHTML += "<font color\"blue\">Dew Point: " + monthDewPoint + "</font><br/>";
        }
        else{
            monthlyHTML += "Dew Point: " + monthDewPoint + "<br/>";
        }

        if(Double.parseDouble(redCount) >= (red + ((red/100) * 10))){
            monthlyHTML += "<font color=\"red\">Red Pixels: " + red + "</font>&emsp;";
        }
        else if(Double.parseDouble(redCount) < (red - ((red/100) * 10))){
            monthlyHTML += "<font color=\"blue\">Red Pixels: " + red + "</font>&emsp;";
        }
        else{
            monthlyHTML += "Red Pixels: " + red + "&emsp;";
        }

        if(Double.parseDouble(blueCount) >= (blue + ((blue/100) * 10))){
            monthlyHTML += "<font color=\"red\">Blue Pixels: " + blue + "</font><br/>";
        }
        else if(Double.parseDouble(blueCount) < (blue - ((blue/100) * 10))){
            monthlyHTML += "<font color\"blue\">Blue Pixels: " + blue + "</font><br/>";
        }
        else{
            monthlyHTML += "Blue Pixels: " + blue + "<br/>";
        }

        if(Double.parseDouble(greenCount) >= (green + ((green/100) * 10))){
            monthlyHTML += "<font color=\"red\">Green Pixels: " + green + "</font>&emsp;";
        }
        else if(Double.parseDouble(greenCount) < (parentCluster.getGreenAverage() - ((parentCluster.getGreenAverage()/100) * 10))){
            monthlyHTML += "<font color=\"blue\">Green Pixels: " + green + "</font>&emsp;";
        }
        else{
            monthlyHTML += "Green Pixels: " + green + "&emsp;";
        }

        if(Double.parseDouble(yellowCount) >= (yellow + ((yellow/100) * 10))){
            monthlyHTML += "<font color=\"red\">Yellow Pixels: " + yellow + "</font><br/>";
        }
        else if(Double.parseDouble(yellowCount) < (yellow - ((yellow/100) * 10))){
            monthlyHTML += "<font color\"blue\">Yellow Pixels: " + yellow + "</font><br/>";
        }
        else{
            monthlyHTML += "Yellow Pixels: " + yellow + "<br/>";
        }

        if(Double.parseDouble(pinkCount) >= (pink + ((pink/100) * 10))){
            monthlyHTML += "<font color=\"red\">Pink Pixels: " + pink + "</font>&emsp;";
        }
        else if(Double.parseDouble(pinkCount) < (pink - ((pink/100) * 10))){
            monthlyHTML += "<font color=\"blue\">Pink Pixels: " + pink + "</font>&emsp;";
        }
        else{
            monthlyHTML += "Pink Pixels: " + pink + "&emsp;";
        }

        if(Double.parseDouble(grayCount) >= (gray + ((gray/100) * 10))){
            monthlyHTML += "<font color=\"red\">Gray Pixels: " + gray + "</font><br/>";
        }
        else if(Double.parseDouble(grayCount) < (gray - ((pink/100) * 10))){
            monthlyHTML += "<font color\"blue\">Gray Pixels: " + gray + "</font><br/>";
        }
        else{
            monthlyHTML += "Gray Pixels: " + gray + "<br/>";
        }

        if(Double.parseDouble(blackCount) >= (black + ((black/100) * 10))){
            monthlyHTML += "<font color=\"red\">Black Pixels: " + black + "</font>&emsp;";
        }
        else if(Double.parseDouble(blackCount) < (black - ((black/100) * 10))){
            monthlyHTML += "<font color=\"blue\">Black Pixels: " + black + "</font>&emsp;";
        }
        else{
            monthlyHTML += "Black Pixels: " + black + "&emsp;";
        }

        if(Double.parseDouble(whiteCount) >= (white + ((white/100) * 10))){
            monthlyHTML += "<font color=\"red\">White Pixels: " + white + "</font><br/>";
        }
        else if(Double.parseDouble(whiteCount) < (white - ((white/100) * 10))){
            monthlyHTML += "<font color\"blue\">White Pixels: " + white + "</font><br/>";
        }
        else{
            monthlyHTML += "White Pixels: " + white + "<br/></html>";
        }
    }

    private void setCurrentImageHTML(){
        currentImageHTML = "<html><strong>Current Image:</strong><br/>" +
                "Temperature: " + tempF + "<br/>" +
                "Humidity: " + humidity + "<br/>" +
                "Dew Point: " + dewPoint + "<br/>" +
                "Red Pixels: " + redCount + "&emsp;Blue Pixels: " + blueCount + "<br/>" +
                "Green Pixels: " + greenCount + "&emsp;Yellow Pixels: " + yellowCount + "<br/>" +
                "Pink Pixels: " + pinkCount + "&emsp;Gray Pixels: " + grayCount + "<br/>" +
                "Black Pixels: " + blackCount + "&emsp;White Pixels: " + whiteCount + "<br/></html>";
    }
}
