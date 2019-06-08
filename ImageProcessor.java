
// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP112 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP112 - 2019T1, Assignment 9_and_10
 * Name:Joe Smith
 * Username:smithjose4
 * ID:300488105
 */

import ecs100.*;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JColorChooser;

/** ImageProcessor allows the user to load, display, modify, and save an
 *       image in a number of ways.
 *  The program includes
 *  - Load, commit
 *
 * CORE:
 *  - Brightness adjustment//--------------DONE
 *  - Horizontal and vertical flips and 90 degree rotations (clockwise
 *       and anticlockwise)//-----------------------DONE   ---------------------------
 *  - Merge //DONE                                         
 *  - Save//--------------------DONE
 *
 * --------------------------------------------DONE
 * COMPLETION
 *  - Crop&Zoom---------------------------DONE---------------------------------------------------------
 *  - Blur (3x3 filter)//------------------------------DoNE---------------------------------
 *  - Rotate arbitrary angle-------------------------------DONE--------------------------------
 *
 *
 * CHALLENGE
 *  - General Convolution Filter-----------------------------------------DONE-----------------------
 *  - Pour (spread-fill)
 *  - Red-eye
 */
public class ImageProcessor {
    private Color[][] image =   null; // current version of the image
    private Color[][] result = null;  // result of applying operation to image
    private Color[][] toMerge = null;

    // current selected region (rows, and columns of the image)
    private int regionLeft=-1;
    private int regionTop=-1;
    private int regionWidth;
    private int regionHeight;

    private int totalRows;
    private int totalCols;

    private String mouseAction = "select";   // what should the mouse do?

    private static final double LEFT = 10;  // position of image .
    private static final double TOP = 10;
    private static final double MARGIN = 5; // space between result and image

    //-------------------------------------------------------
    // Methods for the image operations
    //-------------------------------------------------------

    /**
     * function to set the total dimentions of the current immage used for rotation etc
     * */
    private void setTotals(){
        //variable to store the last index of both parts of 2d original array
        totalRows = rows(image) - 1;
        totalCols = cols(image) - 1;

    }

    /**
     * CORE
     * 
     * Make the image brighter or darker.
     *  The value is between -1.0 and 1.0
     *  Sets the fraction to move the color towards the min max
     */
    public void brightness(float value){
        this.checkResult();

        float[] colorComponents;
        float valueChck;
        float ratio;
        float red;
        float green;
        float blue;
        float alpha;
        Color newColor;

        //looping over every element in the original image
        for(int row = 0; row < rows(image); row++){

            for(int col = 0; col < cols(image); col++) {

                Color origColor = image[row][col];

                //getting the rgb colour coponents of the original image this will be in float form
                colorComponents = origColor.getColorComponents(null);

                //looping over the color components and modifying their value
                for(int i = 0; i < colorComponents.length; i++){

                    /*if the image is to be darkened then the changed colour value is darkened by the value multiplied by the colour this
                    works as if it was -1 to be black then it would be taking the whole value and so be 0 (black) */
                    if(value <= 0.0f) {

                        valueChck = colorComponents[i] + (colorComponents[i] * value);
                    }
                    else{

                        //cant divide by 0
                        if(colorComponents[i] != 0){
                            //ratio of the colour related to value aka how much to increase it
                            ratio = value / colorComponents[i];
                            //adding the increased value by the ratio to the colour value
                            valueChck = (colorComponents[i] + (colorComponents[i] * (ratio)));

                        }
                        else{
                            /*if the colour value is 0 set it to the value this works as it is effectivly doing the same as above but for 0 e.g if value is 1 pix
                            becomes 1 and so white*/
                            valueChck = value;
                        }



                    }

                    if(valueChck > 1.0f){
                        colorComponents[i] = 1.0f;
                    }
                    else if(valueChck < 0.0f){

                        colorComponents[i] = 0.0f;
                    }
                    else{

                        //if its not over or under the range set it to the value calculated
                        colorComponents[i] = valueChck;
                    }
                }

                //if it cant get the proper colour values then its an error
                if(colorComponents.length < 3){
                    UI.println("Error changing brightness");
                }
                else {

                    //setting modified colour components
                    red = colorComponents[0];
                    green = colorComponents[1];
                    blue = colorComponents[2];
                    //if it has alpha set it
                    if(colorComponents.length >= 4) {
                        alpha = colorComponents[3];

                        newColor = new Color(red, green, blue, alpha);
                    }
                    else{
                        //otherwise normal colour type
                        newColor = new Color(red,green,blue);
                    }

                    result[row][col] = newColor;
                }
            }
        }

    }

    /**
     * CORE
     * 
     * Flip the image horizontally (so around the vertical axis)
     */
    public void horizontalFlip(){
        this.checkResult();

        imageFlip(true);
    }

    /**
     * CORE
     * 
     * Flip the image vertically (so around the horizontal axis)
     */
    public void verticalFlip(){
        this.checkResult();

        imageFlip(false);
    }

    /**
     * method for the flipping of the image as horizontal and verticle flip is similar
     * takes parameter true to flip horizontaly other wise verticly
     * */
    private void imageFlip(boolean horizontal){

        //updating dimentions of current image
        setTotals();

        //looping over the current image array each element is looped over going down colomn by colomn
        for(int row = 0; row <= totalRows; row++){

            for(int col = 0; col <= totalCols; col++){

                if(horizontal){
                    //filling the new array with  the pixel from the same row but flipped the colomns so that the new image has the last element in the first index
                    result[row][col] = new Color(image[row][totalCols - col].getRGB());
                }
                else {
                    //filling the new array with the pixel from same colomn as original but the rows start with the last pixel as to flip in
                    result[row][col] = new Color(image[totalRows - row][col].getRGB());
                }

            }
        }
    }

    /**
     * CORE
     * 
     * Rotate the image 90 degrees clockwise
     */
    public void rotate90clockwise(){

        //array for rotation is cols by image instead
        Color[][] rotate90 = new Color[cols(image)][rows(image)];

        //looping over image
        for(int row = 0; row < rows(image); row++){

            for(int col = 0; col < cols(image); col++){

                //each col of the image is filled in the rotate array down the row but starting from the right side and moving left to rotate 90
                rotate90[col][rows(image) - 1 - row] = new Color(image[row][col].getRGB());


            }


        }
        result = copyImage(rotate90);

    }

    /**
     * CORE
     * 
     * Rotate the image 90 degrees anticlockwise
     */
    public void rotate90anticlockwise(){
        //calling roation with anticlockwise
       // rotation(-(Math.PI / 2),cols(image),rows(image));

        //array to hold rotated image is cols by rows instead
        Color[][] antiRotate90 = new Color[cols(image)][rows(image)];

        //looping over image
        for(int row = 0; row < rows(image); row++){

            for(int col = 0; col < cols(image); col++){

                //filling from bottom to top so each col from the image starts from the bottom row of the result upwards to rotate anti 90
                antiRotate90[cols(image) - 1 - col][row] = new Color(image[row][col].getRGB());


            }


        }
        //making the result a clone of the rotaion array
        result = copyImage(antiRotate90);

    }

    /**
     * method that will roatate the image angle degrees using a matrix
     * it takes angle and image cols and rows as parameters totall how much to rotate and what the center of rotation is
     * */
    private void rotation(double angle,int imageCols, int imageRows){

        //variables for which of the original images elements will be selected
        int x;
        int y;
        setTotals();
        //calculating the center of the array
        double centerX = (double)(imageCols / 2);
        double centerY = (double)(imageRows / 2);

        //looping over the image array
        for(int row = 0; row <= totalRows; row++){

            for(int col = 0; col <= totalCols; col++){

                /*setting the x and y values for the values to get from. this equation is to calculate going from a modified value back to the original so it will be using the 
                normal row and col position as the modified value and then going back to the original so will get the related orignal position for a roaion*/
                x = (int) (centerX + (col - centerX) * Math.cos(angle) + (row - centerY) * Math.sin(angle));
                y = (int) (centerY - (col - centerX) * Math.sin(angle) + (row - centerY) * Math.cos(angle));

                //making sure the x and y values are in range and wont cause error for out of range
                if((x >= 0 && x <= totalCols) && (y >= 0 && y <= totalRows)) {
                    //filling the result array in a normal way with the x and y indexs for the rotation
                    result[row][col] = new Color(image[y][x].getRGB());
                }
                else{
                    //elements that cant be rotated due to array size are turned white to go with background
                    result[row][col] = Color.WHITE;
                }
            }

        }

    }

    /** 
     * CORE
     *
     * Merges the current image and the toMerge image, if there is one.
     * Work out the rows and columns shared by the images
     * For each pixel value in the shared region, replace the current pixel value
     * by the average of the pixel value in current image and the corresponding
     * pixel value in the other image.
     */
    public void merge(float factor){

        this.checkResult();

        if (toMerge==null){
            UI.println("no image to merge with");
            return;
        }
        /*# YOUR CODE HERE */
        //updating image totals
        setTotals();
        UI.println("merging: "+factor);
        //setting true merge totals
        int toMergeRows = rows(toMerge) - 1;
        int toMergeCols = cols(toMerge) - 1;
        int rowsMerge;
        int colsMerge;

        //arrays for storing image components
        float[] imageComponets;
        float[] mergeComponets;
        float[] weightedComponets;

        //setting the range of overlapping pixels
        if (toMergeRows >= totalRows) {

            rowsMerge = totalRows;
        }
        else{

            rowsMerge = toMergeRows;
        }
        if(toMergeCols >= totalCols){

            colsMerge = totalCols;
        }
        else{

            colsMerge = toMergeCols;
        }

        //looping over whole of image as to fill all of result
        for(int row = 0; row <= totalRows; row++){

            for(int col = 0; col <= totalCols; col++){

                //if its in the merge range then can calc weighted pixel
                if(row <= rowsMerge && col <= colsMerge) {

                    //setting the component arrays to be filled with related pixel
                    imageComponets = image[row][col].getColorComponents(null);
                    mergeComponets = toMerge[row][col].getColorComponents(null);

                    //images must have same rgb and a or just rgb otherwise would get index out of range
                    if(mergeComponets.length == imageComponets.length) {

                        weightedComponets = new float[mergeComponets.length];
                        //performing weight calculation on each component
                        for (int index = 0; index < mergeComponets.length; index++){

                            weightedComponets[index] = factor * mergeComponets[index] + (1 - factor) * imageComponets[index];

                        }
                    }
                    else if (mergeComponets.length >= 3 && imageComponets.length >= 3){
                        //if they dont match just get the rgb values
                        weightedComponets = new float[3];
                        //performing weight calculation on each component
                        for (int index = 0; index < weightedComponets.length; index++){

                            weightedComponets[index] = factor * mergeComponets[index] + (1 - factor) * imageComponets[index];

                        }

                    }
                    else{

                        UI.println("Merge error");
                        return;
                    }

                    for(int i = 0; i < weightedComponets.length; i++){

                        if(weightedComponets[i] > 1.0f){

                            weightedComponets[i] = 1.0f;
                        }
                        else if (weightedComponets[i] < 0.0f){

                            weightedComponets [i] = 0.0f;
                        }
                    }


                    //if it contains alpha set pixel with alpha else set it with just rgb
                    if(weightedComponets.length >= 4) {
                        result[row][col] = new Color(weightedComponets[0], weightedComponets[1], weightedComponets[2],weightedComponets[3]);
                    }
                    else {

                        result[row][col] = new Color(weightedComponets[0], weightedComponets[1], weightedComponets[2]);
                    }
                }
                else{
                    //out of merge range so set normalyy
                    result[row][col] = image[row][col];
                }
            }

        }
    }

    /**
     * CORE
     *
     * Write the current image to a file
     */
    public  void saveImage() {

        setTotals();

        //creating a buffered image of the same size as the image colour array the .TYPE_INT_RGB means the buffered image will be interpreted in rgb form it takes the width then length first
        BufferedImage bi = new BufferedImage(cols(image),rows(image),BufferedImage.TYPE_INT_RGB);

        //looping over the image and setting its RGB values in the buffered image
        for(int col = 0; col <= totalCols; col++){

            for(int row = 0; row <= totalRows; row++){

                //buffered image takes width then height
                bi.setRGB(col,row,image[row][col].getRGB());
            }

        }
        //writing to file so try catch
        try {
            String saveFileName;
            //making sure a file is either selected or chosen
            do {
                saveFileName = UIFileChooser.save("file to save to");
            }while (saveFileName == null);

            //making sure .jpg is on the end
            if(!saveFileName.contains(".jpg")){

                saveFileName += ".jpg";
            }

            //creating file to save to
            File saveFile = new File(saveFileName);

            //using the imageIO class with static method to write the buffered image to the savefile as type jpeg
            ImageIO.write(bi, "jpg", saveFile);

        }catch (IOException e){
            UI.println("FILE Saving error: " + e);
        }
    }

    /**
     * COMPLETION
     *
     * Scales the currently selected region of the image (if there is one) to fill
     * the result image.
     * This is a combination scale, translate, and crop.
     * Return true if a region was selected, false otherwise
     */
    public boolean cropAndZoom(){
        this.checkResult();

        // requires that a region has been selected.
        /*# YOUR CODE HERE */

        //making sure a area is selected
        if(arrow.equals("select")){
            setTotals();

            //creating an array of the selected area
            Color[][] selectedArea = new Color[regionHeight][regionWidth];
            int selecRow = 0;
            int selecCol;

            //looping over the selected area in the image from top to bottom
            for(int imgRow = regionTop; imgRow < regionHeight + regionTop; imgRow++){

                //reseting col to 0 for each new row
                selecCol = 0;
                for(int imgCol = regionLeft; imgCol < regionWidth + regionLeft; imgCol++){

                    //filling the selected array with the colours from the image array
                    selectedArea[selecRow][selecCol] = new Color(image[imgRow][imgCol].getRGB());
                    selecCol++;
                }

                selecRow++;
            }

            //calculating how much the width needs increasing to fit image
            int widthSelectedIncrrease = 0;
            double widthMulti = (double)cols(image) / cols(selectedArea);

            //increasing the selected width until it fits into the width of the image and is a whole number
            while(((double)cols(image) % (cols(selectedArea) + widthSelectedIncrrease) != 0) && (widthMulti % 1 != 0)){

                widthSelectedIncrrease++;
                widthMulti = (double)cols(image) / (cols(selectedArea) + widthSelectedIncrrease);
            }

            //doing same here but with height
            int heightSelectedIncrrease = 0;
            double heightMulti = (double)rows(image) / rows(selectedArea);
            while (((double)rows(image) % (rows(selectedArea) + heightSelectedIncrrease) != 0) && (heightMulti % 1 != 0)){

                heightSelectedIncrrease++;
                heightMulti = (double)rows(image) / (rows(selectedArea) + heightSelectedIncrrease);

            }

            //making the selected area into buffered image to be resized
            BufferedImage selectedImage = new BufferedImage(cols(selectedArea),rows(selectedArea),BufferedImage.TYPE_INT_RGB);

            //filling the selectedImage with the rgb values from the selected area
            for(int row = 0; row < rows(selectedArea); row++){

                for(int col = 0; col < cols(selectedArea); col++){

                    selectedImage.setRGB(col,row,selectedArea[row][col].getRGB());

                }

            }

            //creating a scalled version from the buffered image this is using default scale
            Image scaledSelec = selectedImage.getScaledInstance(cols(selectedArea) + widthSelectedIncrrease,rows(selectedArea) + heightSelectedIncrrease, Image.SCALE_DEFAULT);

            //cant get the rgb values from image so creating afterScale buffered image to fill
            BufferedImage afterScale = new BufferedImage(cols(selectedArea) + widthSelectedIncrrease,rows(selectedArea) + heightSelectedIncrrease, BufferedImage.TYPE_INT_RGB) ;

            //using drawImage to draw the image object to the graphics of the buffered image after scaled up is drawn at 0 0 so top left of the buffered image and null for observer as not need for buffered image
            afterScale.getGraphics().drawImage(scaledSelec,0,0,null);

            //disposing graphic as finished setting it to buffered image in memory
            afterScale.getGraphics().dispose();

            //creating array to store the rgb colours of the buffered image
            Color[][] strechSelected = new Color[rows(selectedArea) + heightSelectedIncrrease][cols(selectedArea) + widthSelectedIncrrease];

            //filling that array
            for(int row = 0; row < rows(strechSelected); row++){

                for(int col = 0; col < cols(strechSelected); col++){

                    strechSelected[row][col] = new Color(afterScale.getRGB(col,row));

                }

            }

            //drawing the pixels from the streched array the amount initialy descovered in heightMulti and width times in order to resize to full image size
            //variables for the result index
            int resultCol;
            int resultRow = 0;
            //looping over every pixel in the strecSelected array
            for(int row = 0; row < rows(strechSelected); row++){
                //each new row the result array starts at colomn 0
                resultCol = 0;
                for(int col = 0; col < cols(strechSelected); col++){

                    //drawing the same pixel down multiple times to meet the heightMulti so it takes up whole image height when all pixels drawn
                    for(int heightAdd = 0; heightAdd < heightMulti; heightAdd++) {
                        //drawing that specific pixel acroos the widthMulti times so pixels apeare streched take up more room
                        for (int widthAdd = 0; widthAdd < widthMulti; widthAdd++) {

                            //drawing the pixel in the result array in the resultRow index set and has heightAdd added as will be moved across for extra pixels same with col
                            //and is the same pixel from the strechSelected array for each time the pixel is drawn multiple times
                            result[resultRow + heightAdd][resultCol + widthAdd] = new Color(strechSelected[row][col].getRGB());
                        }

                    }
                    //will need to move over widthMulti amount as was drawn multiple times
                    resultCol += widthMulti;

                }
                //next row so need to draw result pixel down by heightMulti as would have drew down that much for each pixel
                resultRow+= heightMulti;

            }

            return true;
        }
        else {
            //image not selected so return false
            return false;
        }
    }

    /** 
     * COMPLETION
     *
     * CONVOLVE  Matrix   
     *   Modify each pixel to make it a weighted average of itself and the pixels around it
     *   A simple blur will weight the pixel by 0.4, its horizontal and vertical neighbours by 0.1, 
     *   and the diagonal neighbours by 0.05.
     * Hint: It is easier to make a new image array of the same size as the image,
     *       then work out the weighted averages in the new array and then assign the new array to the image field.
     */

    public void convolve(float[][] weights){   
        /*# YOUR CODE HERE */
        this.checkResult();
        setTotals();

        //sizes of the matrix
        int matrixWidth = weights.length;
        int matrixHeight = weights[0].length;

        //looping over all pixels in image array
        for(int row =  0; row <= totalRows; row++){

            for(int col = 0; col <= totalCols; col++){

                //reseting rgba values
                float r = 0;
                float g = 0;
                float b = 0;
                float a = 0;
                boolean hasA = false;
                //variables for current pix and row being weighted
                int colPix;
                int rowPix;

                //mids of matrix
                int hd2 = (matrixHeight - 1)/2;
                int wd2 = (matrixWidth - 1)/2;

                //looping over from negative mid of matrix to positive and including so that all parts of the matrix get compared to relevent pixels
                for(int filtHeight = (hd2 * -1); filtHeight <= hd2; filtHeight++){

                    //adding the current row to filtHeight to get corosponding pixel for pixel weighting
                    rowPix = row + filtHeight;

                    //checking the pixel to be checked not out of bounds if it is jump back to top of loop
                    if(rowPix > totalRows){

                        continue;
                    }
                    if(rowPix < 0){

                        continue;
                    }

                    for(int filtWidth = (wd2 * -1); filtWidth <= wd2; filtWidth++){

                        //doing same but with colomns
                        colPix = col + filtWidth;

                        if(colPix > totalCols){

                            continue;
                        }
                        else if(colPix < 0){

                            continue;
                        }

                        //getting the image colour values from the pixel to be checked
                        float[] imgComponents = image[rowPix][colPix].getColorComponents(null);


                        //looping over all the colour values and applying the related weight
                        for(int i = 0; i < imgComponents.length; i++){

                            imgComponents[i] *= weights[filtHeight + hd2][filtWidth + wd2];
                        }

                        //increasing as its for all the surrounding pixels to get average
                        r += imgComponents[0];
                        g += imgComponents[1];
                        b += imgComponents[2];
                        if(imgComponents.length >= 4){

                            a += imgComponents[3];
                            hasA = true;
                        }
                    }

                }

                //checking weight is not out of bounds
                if(r > 1.0f)
                    r = 1.0f;
                else if (r < 0.0f)
                    r = 0.0f;

                if(g > 1.0f)
                    g = 1.0f;
                else if (g < 0.0f)
                    g = 0.0f;

                if(b > 1.0f)
                    b = 1.0f;
                else if(b < 0.0f)
                    b = 0.0f;


                if(hasA){

                    if(a > 1.0f)
                        a = 1.0f;
                    else if(a < 0.0f)
                        a = 0.0f;
                    result[row][col] = new Color(r,g,b,a);
                }
                else {
                    //setting the new colour to the related result array position
                    result[row][col] = new Color(r, g, b);
                }
            }
        }
    }

    /**
     * COMPLETION
     *
     * Rotate the image by the specified angle.
     * Rotates around the center of the image, or around the center
     * of the selected region if there is a selected region.
     */
    public void rotate(double angle){
        this.checkResult();
        /*# YOUR CODE HERE */
        setTotals();
        //setting the angle to radians as thats what the calculations using 
        angle = Math.toRadians(angle);
        
        //checking if the height and region has no selected area so when the selected rectangle is removed it will rotate normally
        if (regionHeight == 0 && regionWidth == 0) {
            //roating by the angle with the center of image for rotation
            rotation(angle, cols(image), rows(image));
        } else {
            
            //rotating by the angle with center of image as the center of selected area
            rotation(angle, regionWidth + regionLeft, regionHeight + regionTop);

        }

    }


/**
 * function to read convolution filter file and turn into 2d array
 * user sets the file to read then calls convolve with that filter
 * */
    public void  setConvolve(){

        int totRows;
        int totCols;
        try{

            //getting file to scan over
            String filterName;
            //file must be selected
            do {
                filterName = UIFileChooser.open("select a filter file");
            }while (filterName == null);

            //making sure file exists
            File filterFile = new File(filterName);
            if(!filterFile.exists()){

                UI.println("file doesnt exist");
                return;
            }

            Scanner fileScan = new Scanner(filterFile);

            //total row and col are first 2 numbers
            totRows = fileScan.nextInt();
            totCols = fileScan.nextInt();
            float[][] filter = new float[totRows][totCols];


            //adding all the values to the array
            for(int row = 0; row < totRows; row++){

                for (int col = 0; col < totCols; col++){

                    //if the next token is not a float ignore it
                    if(fileScan.hasNextFloat()) {
                        filter[row][col] = fileScan.nextFloat();
                    }
                    else{fileScan.next();}
                }


            }

            //call convolve on that filter given
            convolve(filter);
        }
        catch(IOException e){

            UI.println("file reading error: " + e);
        }


    }

    //-------------
    //  GUI methods  
    //-------------

    /** Respond to button presses */
    public void buttonSave(){
        if (this.image == null) {
            UI.printMessage("Nothing to save");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.saveImage();
        this.redisplay();
    }

    public void buttonCommit(){
        if (this.result == null) {
            UI.printMessage("Nothing to commit");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.image = copyImage(this.result); 
        this.displayResult = false;
        this.arrow = "left";
        this.action = "Commit";
        this.redisplay();
    }

    /** Respond to sliders changes */
    public void sliderBrightness(double num) {
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.brightness((float)num/100);
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Brightness " + (num/100);
        this.redisplay();
    }

    public void buttonHorizontalFlip(){
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.horizontalFlip(); 
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Horizontal Flip";
        this.redisplay();
    }

    public void buttonVerticalFlip(){
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.verticalFlip(); 
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Vertical Flip";
        this.redisplay();
    }

    public void buttonRotate90clockwise(){
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.rotate90clockwise();
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Rotate 90 Clockwise";
        this.redisplay();
    }

    public void buttonRotate90anticlockwise(){
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.rotate90anticlockwise();
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Rotate 90 Anticlockwise";
        this.redisplay();
    }

    public void buttonLoadMerge(){
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.toMerge = this.loadImage(UIFileChooser.open("Image to merge"));
        this.merge(0.5f);
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Load Merge";
        this.redisplay();
    }

    public void sliderMerge(double num) {
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.merge((float)num/100);
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Merge level " + (int)(num) + "%";
        this.redisplay();
    }

    public void buttonCropZoom(){
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        this.displayResult =  this.cropAndZoom(); 
        this.arrow = "right";
        this.action = "Crop&Zoom";
        this.redisplay();
    }

    public void buttonBlur(){
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action

        //blur matrix
        float[][] blurWeights = {
                {0.05f,0.1f,0.05f},
                {0.1f,0.4f,0.1f},
                {0.05f,0.1f,0.05f}
            };
        this.convolve(blurWeights);
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Blur";
        this.redisplay();
    }


    public  void generalConvolve(){

        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.mouseAction="select";  // reset the current mouse action
        setConvolve();
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Convolved";
        this.redisplay();



    }

    public void sliderRotate(double num) {
        if (this.image == null) {
            UI.printMessage("No image");
            return;
        }
        this.rotate(num);
        this.displayResult = true;
        this.arrow = "right";
        this.action = "Rotate "+num;
        this.redisplay();
    }

    /**
     * Respond to mouse events "pressed", "released"".
     * If mouseAction field is "select", then pressed and released set the region
     * (can be on either of the result or the image).
     * If mouseAction field is "pour", then released will pour the current paint
     *  at the point.
     */
    public void doMouse(String action, double x, double y) {
        int[] rowCol = getRowColAtMouse(x, y);
        int row = -1;
        int col = -1;
        if (rowCol!=null){
            row = rowCol[0];
            col = rowCol[1];
        }

        if (action.equals("pressed")){
            if (mouseAction=="select"){
                this.regionTop = row;
                this.regionLeft = col;
            }
        }
        else if (action.equals("released")){
            if (mouseAction=="select"){
                this.regionHeight = Math.abs(row-this.regionTop);
                this.regionWidth = Math.abs(col-this.regionLeft);
                this.regionTop = Math.min(row, this.regionTop);
                this.regionLeft = Math.min(col, this.regionLeft);
                this.arrow = "select";
                this.redisplay();
            }

        }
    }

    //-------------------------------------------------------
    // UTILITY METHODS: load, save, copy clear, check,  computing rows/cols
    //-------------------------------------------------------

    /**
     * Returns the number of rows in an image
     */
    public int rows(Color[][] array){return array.length;}

    /**
     * Returns the number of columns in an image
     */
    public int cols(Color[][] array){return array[0].length;}

    /**
     * Returns the row and column of the image that the point (x, y) is on.
     */
    private int[] getRowColAtMouse(double x, double y){
        if (this.image==null){UI.println("no image"); return null;}
        if (y<TOP || y >= TOP+this.rows(this.image)){return null;}
        if (x<LEFT || x>= LEFT+this.cols(this.image)){return null;}

        int row = (int)(y-TOP);
        int col = (int)(x-LEFT);
        return new int[]{row, col};
    }

    /**
     * Loads an image from a file into both the current image and the result image
     */
    public void load(){
        String fname = UIFileChooser.open();
        this.image = loadImage(fname);
        this.result = copyImage(this.image);
        this.displayResult = false;
        this.arrow = "select";
        this.redisplay();
    }

    /**
     * Load image from a file and return as a two-dimensional array of Color.
     */
    public Color[][] loadImage(String imageName) {
        Trace.println("loading " + imageName);
        if (imageName==null || !new File(imageName).exists()){ return null; }
        try {
            BufferedImage img = ImageIO.read(new File(imageName));
            int rows = img.getHeight();
            int cols = img.getWidth();
            Color[][] ans = new Color[rows][cols];
            for (int row = 0; row < rows; row++){
                for (int col = 0; col < cols; col++){                 
                    Color c = new Color(img.getRGB(col, row));
                    ans[row][col] = c;
                }
            }
            UI.printMessage("Loaded "+ imageName);
            return ans;
        } catch(IOException e){UI.println("Image reading failed: "+e);}
        return null;
    }

    /**
     * Ensures that the result image is the same size as the current image.
     * Makes a new result image array if not.
     */
    public void checkResult(){
        int rows = this.rows(this.image);
        int cols = this.cols(this.image);
        if (this.rows(this.result) != rows || this.cols(this.result) != cols){
            this.result = new Color[rows][cols];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    this.result[row][col]= Color.BLACK;
                }
            }
        }
    }

    /**
     * Set the result image to all black (needed for convolution}
     */
    public void clearResult(){
        for(int row =0; row<result.length;row++){
            for(int col=0;col< result[0].length;col++){
                result[row][col] = Color.BLACK;

            }
        }
    }

    /**
     * Make a copy of an image array
     */
    public Color[][] copyImage(Color[][] from){
        int rows = from.length;
        int cols = from[0].length;
        Color[][] to = new Color[rows][cols];
        for (int row = 0 ; row < rows ; row++){
            for (int col = 0; col<cols; col++) {
                to[row][col] = from[row][col];
            }
        }
        return to;
    }

    //=========================================================================
    boolean displayResult = false;
    String arrow = "none";
    String action;
    double arrowSize = 100;

    /** ReDisplay the images (image and result) each pixel as a square of size 1
     *  The original image is displayed on the left
     *  The result image (that results from any transformation) is displayed on the right
     *  Called after each button pressed.
     */
    public void redisplay(){
        if (this.image ==null) {
            UI.println("no image to display");
            return;
        }
        double imageRight = LEFT + this.cols(this.image);
        UI.clearGraphics();
        displayImage(this.image, LEFT);
        if (this.arrow.equals("right")) {
            UI.sleep(100);
            displayRightArrow(this.action, imageRight + MARGIN, TOP+this.regionHeight/2);
        }
        else if (this.arrow.equals("left")) {
            UI.sleep(100);
            displayLeftArrow(this.action, imageRight + MARGIN, TOP+this.regionHeight/2);
        }

        if (this.displayResult) {
            UI.sleep(100);
            displayImage(this.result, imageRight + MARGIN*2 + this.arrowSize);
        }

        if (this.regionLeft>-1){
            UI.setColor(Color.red);
            UI.drawRect(LEFT+this.regionLeft, TOP+this.regionTop,
                this.regionWidth, this.regionHeight);
        }
        UI.repaintGraphics();
    }

    public void displayImage(Color[][] img, double left){
        double y = TOP;
        for(int row=0; row<img.length; row++){
            double x = left;
            for(int col=0; col<img[row].length; col++){
                UI.setColor(img[row][col]);
                UI.fillRect(x, y, 1, 1);
                x++;
            }
            y++;
        }
    }

    public void displayRightArrow(String text, double left, double top) {
        UI.setColor(Color.green);
        UI.fillRect(left, top+this.arrowSize/3, this.arrowSize/2, this.arrowSize/3);
        double [] xPoints = {left+this.arrowSize/2, left+this.arrowSize/2, left+this.arrowSize};
        double [] yPoints = {top,top+this.arrowSize,top+this.arrowSize/2};
        UI.fillPolygon(xPoints,yPoints,3);
        UI.setColor(Color.black);
        UI.drawString(text, left+2, top+this.arrowSize/2);
    }

    public void displayLeftArrow(String text, double left, double top) {
        UI.setColor(Color.green);
        UI.fillRect(left+this.arrowSize/2, top+this.arrowSize/3, this.arrowSize/2, this.arrowSize/3);
        double [] xPoints = {left+this.arrowSize/2, left+this.arrowSize/2, left};
        double [] yPoints = {top,top+this.arrowSize,top+this.arrowSize/2};
        UI.fillPolygon(xPoints,yPoints,3);
        UI.setColor(Color.black);
        UI.drawString(text, left+this.arrowSize/2+2, top+this.arrowSize/2);
    }

    // Main
    public static void main(String[] arguments){
        ImageProcessor obj = new ImageProcessor();
        UI.setMouseListener(obj::doMouse);
        UI.addButton("Load", obj::load);
        UI.addButton("Save", obj::buttonSave);
        UI.addButton("Commit", obj::buttonCommit);
        UI.addSlider("Brightness", -100, 100, 0, obj::sliderBrightness);
        UI.addButton("Horizontal Flip", obj::buttonHorizontalFlip);
        UI.addButton("Vertical Flip", obj::buttonVerticalFlip);
        UI.addButton("Rotate 90 Clockwise", obj::buttonRotate90clockwise);
        UI.addButton("Rotate 90 Anticlockwise", obj::buttonRotate90anticlockwise);
        UI.addButton("Load Merge", obj::buttonLoadMerge);
        UI.addSlider("Merge level", 0, 100, 50, obj::sliderMerge);

        UI.addButton("Crop&Zoom", obj::buttonCropZoom);
        UI.addButton("Blur", obj::buttonBlur);
        UI.addSlider("Rotate", -180, 180, 0, obj::sliderRotate);


        UI.addButton("Convolve",obj::generalConvolve);

        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1400,600);
        UI.setDivider(0.15);
    }       

}
