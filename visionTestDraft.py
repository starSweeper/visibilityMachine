# Vision Machine Draft
# Developed by Amanda Joy Panell in collaboration with Emily Kelly and Jon Fenn

import statistics  # For statistics.median()
import cv2  # OpenCV
import numpy as np  # For SVM
import os  # For working with files
import datetime  # For time and date stamps
import sys  # For system exit


# Tells you which color group a pixel belongs in. The choices are Red, Blue, Green, Yellow, Pink, Gray, Black, and White
def color_classifier(red, green, blue):
    median = statistics.median([red, blue, green])

    # If all colors are within 10 of each other
    if(median - 10 <= blue <= median + 10) and (median - 10 <= green <= median + 10)\
            and (median - 10 <= red <= median + 10):
        if median <= 50:
            return "black"
        elif median >= 250:
            return "white"
        else:
            return "gray"
    # If blue and green are within 10 of each other and are the top two numbers
    elif (max([red, blue, green]) == blue or max([red, blue, green]) == green) and \
            (green - 10 <= blue <= green + 10):
        if blue >= 30 and green >= 30:
            return "blue"
        else:
            return "black"
    # If red and blue are within 10 of each other and are the top two numbers
    elif (max([red, blue, green]) == red or max([red, blue, green]) == blue) and \
            (red - 10 <= blue <= red + 10):
        if red >= 40 and blue >= 40:
            return "pink"
        else:
            return "black"
    # If red and green are within 10 of each other and are the top two numbers
    elif (max([red, blue, green]) == red or max([red, blue, green]) == green) and \
            (red - 10 <= green <= red + 10):
        if red >= 80 and green >= 80:
            return "yellow"
        else:
            return "black"
    # If red is highest
    elif max([red, blue, green]) == red:
        if min([blue, green]) <= (red - 20):
            return "red"
        else:
            return "gray"
    # If green is highest
    elif max([red, blue, green]) == green:
        if min([red, blue, green]) <= (green - 20):
            return "green"
        else:
            return "gray"
    # If blue is highest
    elif max([red, blue, green]) == blue:
        if min([red, blue, green]) <= (blue - 20):
            return "blue"
        else:
            return "gray"


# Counts how many pixels of colors we are interested in are in each photo
def counting_colors(photo_path):
    red_count = 0
    blue_count = 0
    green_count = 0
    yellow_count = 0
    pink_count = 0
    gray_count = 0
    black_count = 0
    white_count = 0

    photo = cv2.imread(photo_path, 1)  # Read image

    # Resize image if too big or to small. (For consistency between images)
    if photo.size != 230400:
        photo = cv2.resize(photo, (240, 320))

    # Get the height and width (in pixels) of the image
    height, width = photo.shape[:2]

    # For ever pixel
    for x in range(height):
        for y in range(width):
            color_found = color_classifier(photo[x, y, 2], photo[x, y, 1], photo[x, y, 0])  # Label pixel as a color

            if color_found == "red":
                red_count = red_count + 1
            elif color_found == "blue":
                blue_count = blue_count + 1
            elif color_found == "green":
                green_count = green_count + 1
            elif color_found == "yellow":
                yellow_count = yellow_count + 1
            elif color_found == "pink":
                pink_count = pink_count + 1
            elif color_found == "gray":
                gray_count = gray_count + 1
            elif color_found == "black":
                black_count = black_count + 1
            elif color_found == "white":
                white_count = white_count + 1

    # Create list of normalized color counts
    color_count = [min_max_normalization(red_count), min_max_normalization(blue_count),
                   min_max_normalization(green_count), min_max_normalization(yellow_count),
                   min_max_normalization(pink_count), min_max_normalization(gray_count),
                   min_max_normalization(black_count), min_max_normalization(white_count)]

    return color_count


# Gets list of data from each image
def process_image(file):
    if file.split(".")[1:] == ['png'] or file.split(".")[1:] == ['jpg']:  # Only process png and jpg files

        photo_data = counting_colors(file)  # Create data list and add to it a count of colors found in each image
        file = file.replace(".", "_")  # Replace '.' with '_'
        nickname, temp, humidity, dew_point, time_of_photo, date, extension = file.split("_")  # Parse file name

        # Replace any 'p' character found in the temperature, humidity, or dew point with a '.'
        temp = temp.replace("p", ".")
        humidity = humidity.replace("p", ".")
        dew_point = dew_point.replace("p", ".")

        # Add normalized data to the image data list
        photo_data.append(min_max_normalization(float(temp)))
        photo_data.append(min_max_normalization(float(humidity)))
        photo_data.append(min_max_normalization(float(dew_point)))
        photo_data.append(min_max_normalization(int(time_of_photo)))
        photo_data.append(min_max_normalization(int(date)))

        return photo_data


# Process a folder full of images
def process_image_folder(f_path, test_list, label_list, cat_path=""):

    # Process each image in folder
    for file in os.listdir(f_path + cat_path):
        photo_data = process_image(f_path + cat_path + "/" + file)

        if cat_path != "" and photo_data is not None:
            if cat_path == "/clear" or cat_path == "/good":
                label_list.append(0)
            elif cat_path == "/foggy" or cat_path == "/bad":
                label_list.append(1)
            elif cat_path == "/smoky":
                label_list.append(2)

            test_list.append(photo_data)  # Add each list to full list of image data


# Makes sure folder meets the proper requirements before it can be processed
def process_test_data(folder_path):
    all_testing_data = []
    labels = []

    if os.path.isdir(folder_path):
        if os.path.isdir(folder_path + "/clear") and os.path.isdir(folder_path + "/foggy") \
                and os.path.isdir(folder_path + "/smoky"):
            print("Attempting to extract data from images... Please be patient!")
            process_image_folder(folder_path, all_testing_data, labels, "/clear")
            process_image_folder(folder_path, all_testing_data, labels, "/foggy")
            process_image_folder(folder_path, all_testing_data, labels, "/smoky")
            print("Data extraction complete.")
            return all_testing_data, labels
        elif os.path.isdir(folder_path + "/good" and os.path.isdir(folder_path + "/bad")):
            print("Attempting to extract data from images... Please be patient!")
            process_image_folder(folder_path, all_testing_data, labels, "/good")
            process_image_folder(folder_path, all_testing_data, labels, "/bad")
        else:
            print("ERROR! Not all required folders were found! Folder names are case sensitive!")
    else:
        print("ERROR! Could not find folder!")


# NOT COMPLETE! Will be the function that the Raspberry Pi will use to classify new images when in the field.
def classify_image_from_camera():
    os.popen("raspistill -t 2000 -o -n latestImage.png")  # Take image with pi camera

    # ENTER CODE HERE TO GET TEMP AND HUMIDITY FROM PI

    data_from_image = counting_colors("latestImage.png")  # Count the colors found in image
    # data_from_image.append(float(ENTER CODE TO GET TEMP))
    # data_from_image.append(float(ENTER CODE TO GET HUMIDITY))
    # data_from_image.append(float(ENTER CODE TO GET DEW POINT))
    data_from_image.append(datetime.datetime.now().strftime("%H%M"))  # Append time in 24 hour format with no colin
    data_from_image.append(datetime.datetime.now().strftime("%m%d%y"))  # Append date in MMDDYY format

    return data_from_image


# Train the SVM
def train_svm(training_data, labels, file_name='svm_data.dat'):
    svm = cv2.ml.SVM_create()
    print("Training SVM...")

    # Set parameters
    svm.setKernel(cv2.ml.SVM_RBF)
    svm.setType(cv2.ml.SVM_C_SVC)
    svm.setC(10)
    svm.setGamma(115)

    svm.train(np.array(training_data, np.float32), cv2.ml.ROW_SAMPLE, np.array(labels, np.int32))
    svm.save(file_name)
    print("Training complete.\n")


# Classify a single image (based on its data after being processed)
def predict_using_svm(image_data, file_name='svm_data.dat'):
    data_list = [image_data]
    svm = cv2.ml.SVM_load(file_name)
    return int(svm.predict(np.asarray(data_list, np.float32))[1])  # Returns the classification. Will be an int.


# Normalize data using min-max normalization
def min_max_normalization(value):
    return ((value - 0) / (500000 - 0)) * (1 - (-1)) + (-1)


# Test the accuracy of the SVM
def test_accuracy(folder, file_name='svm_data.dat'):
    testing_data = []
    labels = []
    result = []

    if os.path.isdir(folder):
        if os.path.isdir(folder + "/clear") and os.path.isdir(folder + "/foggy") and os.path.isdir(
                        folder + "/smoky"):
            print("Preparing test data... This may take a while")
            process_image_folder(folder, testing_data, labels, "/clear")
            process_image_folder(folder, testing_data, labels, "/foggy")
            process_image_folder(folder, testing_data, labels, "/smoky")
            print("Data extraction complete.")

            svm = cv2.ml.SVM_load(file_name)
            for i in testing_data:
                temp_list = [i]
                result.append(int(svm.predict(np.asarray(temp_list, np.float32))[1]))
        else:
            print(
                "ERROR! Test data not complete! Folders needed: clear, foggy, smoky. This is case sensitive!")
    else:
        print("ERROR! Could not find test data folder!")

    mask = []
    print("Labels:  " + str(labels))
    print("Results: " + str(result))

    for i in range(len(labels)):
        if labels[i] != result[i]:
            mask.append(0)
        else:
            mask.append(1)

    print("Mask:    " + str(mask))
    correct = np.count_nonzero(mask)
    return correct*100.0/len(result)


# A (hopefully) easy to use interface. Main menu.
def user_interface():
    print("Welcome to visionMachine!\nWhat would you like to do?\n")
    print("1. Initiate vision test. (Requires a single image.)")
    print("2. Train clear/foggy/smoky SVM. (Requires a folder of training images.)")
    print("3. Test clear/foggy/smoky SVM. (Requires a folder of test images.)")
    print("4. Classify an individual photo using clear/foggy/smoky SVM. (Requires a single image.)")
    print("5. Train good visibility/bad visibility SVM. (Requires a folder of training images.)")
    print("6. Test good visibility/bad visibility SVM. (Requires a folder of training images.)")
    print("7. Classify an individual photo using good visibility/bad visibility SVM. (Requires a single image.)")
    print("8. Exit.\n")

    response = int(input("Please enter your selection: "))

    if response == 1:
        print("One day...")
    elif response == 2:
        if input("Enter 'y' to use default folder (visibilityMachine/trainingData) "
                 "or any other character to enter your own: ").lower() == "y":
            training_list, labels_list = process_test_data("trainingData")  # Process training data
        else:
            user_folder = input("Enter test image folder. Folder MUST have a 'clear' sub-folder, "
                                "a 'foggy' sub-folder, and a 'smoky' sub-folder: ")
            training_list, labels_list = process_test_data(user_folder)  # Process training data
        print("\n")
        train_svm(training_list, labels_list)  # Train SVM (requires processed training data)
        user_interface()
    elif response == 3:
        if input("Enter 'y' to use default folder (visibilityMachine/trainingData) "
                 "or any other character to enter your own: ").lower() == "y":
            print("\n")
            print("SVM was " + str(round(test_accuracy("testData"), 2)) + "% accurate.\n")  # Test SVM
            user_interface()
        else:
            user_folder = input("Enter test image folder. Folder MUST have a 'clear' sub-folder, "
                                "a 'foggy' sub-folder, and a 'smoky' sub-folder: ")
            print("\n")
            print("SVM was " + str(round(test_accuracy(user_folder), 2)) + "% accurate.\n")  # Test SVM
            user_interface()
    elif response == 4:
        prediction = predict_using_svm(process_image(input("Please enter image path: ")))
        print("\n")
        if prediction == 0:
            print("Image classified as clear.\n")
        elif prediction == 1:
            print("Image classified as foggy.\n")
        elif prediction == 2:
            print("Image classified as smoky.\n")
        user_interface()
    elif response == 5:
        if input("Enter 'y' to use default folder (visibilityMachine/trainingData) "
                 "or any other character to enter your own: ").lower() == "y":
            training_list, labels_list = process_test_data("trainingData")  # Process training data
        else:
            user_folder = input("Enter test image folder. Folder MUST have a 'good' sub-folder, "
                                "and a 'bad' sub-folder: ")
            training_list, labels_list = process_test_data(user_folder)  # Process training data

        print("\n")
        train_svm(training_list, labels_list, "goodOrBad_svm.dat")  # Train SVM (requires processed training data)
        user_interface()
    elif response == 6:
        if input("Enter 'y' to use default folder (visibilityMachine/trainingData) "
                 "or any other character to enter your own: ").lower() == "y":
            print("\n")
            dprint("SVM was " + str(round(test_accuracy("testData", "goodOrBad_svm.dat"), 2)) + "% accurate.\n")
            user_interface()
        else:
            user_folder = input("Enter test image folder. Folder MUST have a 'good' sub-folder, "
                                "and a 'bad' sub-folder: ")
            print("\n")
            print("SVM was " + str(round(test_accuracy(user_folder, "goodOrBad_svm.dat"), 2)) + "% accurate.\n")
            user_interface()
    elif response == 7:
        prediction = predict_using_svm(process_image(input("Please enter image path: ")), "goodOrBad_svm.dat")
        print("\n")
        if prediction == 0:
            print("Image classified as good visibility.\n")
        elif prediction == 1:
            print("Image classified as bad visibility.\n")
        user_interface()
    elif response == 8:
        print("\nAs if you didn't know how to use the exit button. Lazy.")
        sys.exit(0)
    else:
        print("That was not a valid selection. Please try again!\n")
        user_interface()


user_interface()
