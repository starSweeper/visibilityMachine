# Python Script to cluster, sort, and analyze new images so that they can be labeled by the labeling interface

import statistics
import cv2
import numpy as np
from sklearn.cluster import KMeans
import os

np.set_printoptions(threshold=np.nan)


class NewImages:
    image_total = 0

    def __init__(self, file_path, image_from_data, cluster_num):
        self.attribute_list = image_from_data
        self.f_path = file_path
        self.f_name = file_path.split("\\")[-1]
        self.i_data = image_data
        self.cluster = cluster_num
        self.month = int(str(self.attribute_list[-1])[:-4])
        # If month is invalid, set it to January and let the problem be fixed during labeling
        if self.month > 12 or self.month == 0:
            self.month = 1

        NewImages.image_total += 1


# Create a list of files in folder
def new_images(folder, file_paths):
    if os.path.isdir(folder):
        for my_image in os.listdir(folder):
            if my_image.split(".")[1:] == ['png'] or my_image.split(".")[1:] == ['jpg']:
                file_paths.append(os.path.abspath(folder) + '\\' + my_image)


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

    # For every pixel
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
    color_count = [red_count, blue_count, green_count, yellow_count, pink_count, gray_count, black_count, white_count]

    return color_count


# Process images as data (without normalization)
def process_image_as_non_normalized_data_(file):
    if file.split(".")[1:] == ['png'] or file.split(".")[1:] == ['jpg']:  # Only process png and jpg files
        # photo_data = counting_colors("newData/" + file)
        photo_data = counting_colors(file)
        file = file.replace(".", "_")  # Replace '.' with '_'
        nickname, temp, humidity, dew_point, time_of_photo, date, extension = file.split("_")  # Parse file name
        if len(date) > 6:
            print("Date too long in " + file)
            date = date[-5:]

        # Replace any 'p' character found in the temperature, humidity, or dew point with a '.'
        temp = temp.replace("p", ".")
        humidity = humidity.replace("p", ".")
        dew_point = dew_point.replace("p", ".")

        # Add normalized data to the image data list
        photo_data.append(int(float(temp)))
        photo_data.append(int(float(humidity)))
        photo_data.append(int(float(dew_point)))
        photo_data.append(int(time_of_photo))
        photo_data.append(int(date))

        return photo_data


# Cluster data using k-means clustering
def cluster(np_data_array):
    k_means = KMeans(n_clusters=8).fit(np_data_array)
    return k_means.labels_


def get_averages(obj_list, index):
    total = 0
    for o in obj_list:
        total += o.attribute_list[index]
    return str(int(float(total/len(obj_list))))


# Get stats for each cluster
def get_cluster_stats(i_objects):

    cluster_group = []  # List containing each cluster as a list of NewImages object

    # For each cluster add an empty list to cluster_group
    for j in range(8):
        cluster_group.append([])

    # Add each object to the cluster group it belongs in
    for obj in i_objects:
        cluster_group[obj.cluster].append(obj)

    #  Generate JSON string
    json_string = "{\"clusters\": ["  # String will eventually be output to a JSON file
    for j in range(8):
        json_string += "\n\t{\n\t\t\"clusterNum\": " + str(j) + \
                       ",\n\t\t\"clusterTotal\": " + str(len(cluster_group[j])) + \
                       ",\n\t\t\"redAvg\": " + str(get_averages(cluster_group[j], 0)) + \
                       ",\n\t\t\"blueAvg\": " + str(get_averages(cluster_group[j], 1)) + \
                       ",\n\t\t\"greenAvg\": " + str(get_averages(cluster_group[j], 2)) + \
                       ",\n\t\t\"yellowAvg\": " + str(get_averages(cluster_group[j], 3)) + \
                       ",\n\t\t\"pinkAvg\": " + str(get_averages(cluster_group[j], 4)) + \
                       ",\n\t\t\"grayAvg\": " + str(get_averages(cluster_group[j], 5)) + \
                       ",\n\t\t\"blackAvg\": " + str(get_averages(cluster_group[j], 6)) + \
                       ",\n\t\t\"whiteAvg\": " + str(get_averages(cluster_group[j], 7)) + \
                       ",\n\t\t\"tempAvg\": " + str(get_averages(cluster_group[j], 8)) + \
                       ",\n\t\t\"humidityAvg\": " + str(get_averages(cluster_group[j], 9)) + \
                       ",\n\t\t\"dewPointAvg\": " + str(get_averages(cluster_group[j], 10)) + \
                       ",\n\t\t\"timeAvg\": " + str(get_averages(cluster_group[j], 11)) + \
                       ",\n\t\t\"dateAvg\": " + str(get_averages(cluster_group[j], 12)) + \
                       ",\n\t\t\"images\":\n\t\t\t[\n\t\t\t\t"
        for count in range(len(cluster_group[j])):
            json_string += "{\n\t\t\t\t\t\"imageID\": " + str(count) + \
                           ",\n\t\t\t\t\t\"imagePath\": \"" + cluster_group[j][count].f_path.replace("\\", "\\\\") + \
                           "\"" + \
                           ",\n\t\t\t\t\t\"imageTitle\": \"" + cluster_group[j][count].f_name + "\"" + \
                           ",\n\t\t\t\t\t\"redCount\": " + str(cluster_group[j][count].attribute_list[0]) + \
                           ",\n\t\t\t\t\t\"blueCount\": " + str(cluster_group[j][count].attribute_list[1]) + \
                           ",\n\t\t\t\t\t\"greenCount\": " + str(cluster_group[j][count].attribute_list[2]) + \
                           ",\n\t\t\t\t\t\"yellowCount\": " + str(cluster_group[j][count].attribute_list[3]) + \
                           ",\n\t\t\t\t\t\"pinkCount\": " + str(cluster_group[j][count].attribute_list[4]) + \
                           ",\n\t\t\t\t\t\"grayCount\": " + str(cluster_group[j][count].attribute_list[5]) + \
                           ",\n\t\t\t\t\t\"blackCount\": " + str(cluster_group[j][count].attribute_list[6]) + \
                           ",\n\t\t\t\t\t\"whiteCount\": " + str(cluster_group[j][count].attribute_list[7]) + \
                           ",\n\t\t\t\t\t\"tempInF\": " + str(cluster_group[j][count].attribute_list[8]) + \
                           ",\n\t\t\t\t\t\"humidity\": " + str(cluster_group[j][count].attribute_list[9]) + \
                           ",\n\t\t\t\t\t\"dewPointInF\": " + str(cluster_group[j][count].attribute_list[10]) + \
                           ",\n\t\t\t\t\t\"time\": \"" + str(cluster_group[j][count].attribute_list[11]) + "\"" + \
                           ",\n\t\t\t\t\t\"date\": \"" + str(cluster_group[j][count].attribute_list[12]) + "\""+ \
                           "\n\t\t\t\t},\n\t\t\t\t"
        json_string += "\b]\n\t},"
    json_string += "\n]}"

    return json_string


# Get stats for each month
def get_month_stats(i_objects):
    month_group = []
    new_string = ""

    for j in range(12):
        month_group.append([])

    try:
        for obj in i_objects:
            month_group[obj.month - 1].append(obj)
    except IndexError:
            print("\nError processing this image: " + obj.f_name)

    for j in range(12):
        if month_group[j] != []:
            new_string += "Month " + str(j + 1) + " "
            for count in range(len(month_group[j][0].attribute_list)):
                new_string += (get_averages(month_group[j], count) + " ")
            new_string += "\n"

    return new_string


all_images = []
all_data = []
new_images("newData1", all_images)
progress = ""
image_objects = []

for i, image in enumerate(all_images):
    image_data = process_image_as_non_normalized_data_(image)
    all_data.append(image_data)
    progress = str(i+1) + "/" + str(len(all_images)) + " Images processed"
    print('\r' + progress, end='')

clusters_from_k_means = cluster(np.asarray(all_data, np.int32))
clusters = []

for c in np.nditer(clusters_from_k_means):
    clusters.append(int(c))

for i in range(len(clusters)):
    image_objects.append(NewImages(all_images[i], all_data[i], clusters[i]))
    # Write image data to file\VVBv z

cluster_json = open("clusters.json", "w")
cluster_json.write(get_cluster_stats(image_objects))
cluster_json.close()

month_info = open("new_image_month_stats.txt", "w")
month_info.write(get_month_stats(image_objects))
month_info.close()
