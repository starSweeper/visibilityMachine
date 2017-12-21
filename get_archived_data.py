from PIL import Image
from PIL.ExifTags import TAGS
from urllib.request import urlretrieve
import urllib.request
import json
import time


def pull_image(day, month, year):
    # Get weather data (CAN ONLY DO THIS 500 TIMES A DAY!!!!!!)
    json_from_site = urllib.request.urlopen('http://api.wunderground.com/api/fd446ebeccba6358/history_' +
                                            year + month + day + '/q/OR/Medford.json')
    json_string = json_from_site.read()
    parsed_json = json.loads(json_string)

    temperature = humidity = dew_point = '0'

    for entry in parsed_json['history']['observations']:
        if entry['date']['hour'] == '13':
            temperature = str(entry['tempi'].replace('.', 'p'))
            humidity = str(entry['hum'].replace('.', 'p'))
            dew_point = str(entry['dewpti'].replace('.', 'p'))
            break

    if temperature == '0' and humidity == '0' and dew_point == '0':
        print("Weather information not found for " + month + '/' + day + '/' + year + " (I call shenanigans!)")
    else:
        image_title = 'newData/medford_' + temperature + '_' + humidity + '_' + dew_point + \
                      '_' + '1358' + '_' + month + day + year[-2:] + '.jpg'

        image_url = 'http://swo.odf.state.or.us/~camarc/' + month + day + year + '.jpg'
        try:
            urlretrieve(image_url, image_title)
            print("Pulled image from (" + month + "/" + day + "/" + year + "). Now time for a quick nap...")
        except Exception as e:
            print("Ran into a bit of an issue.")
            print(e)
            print("Was attempting to retrieve this URL: " + image_url)
            print("Pausing for a bit before I try a different one...")
            time.sleep(60)


def get_images_from_archive(limit):
    archive_date_list = tuple(open("medfordArchiveDates.txt", "r"))

    for i in range(limit):
        image_date = archive_date_list[i].split()
        pull_image(image_date[1], image_date[0], image_date[2])
        time.sleep(8)  # Sleep for 8 seconds to avoid hitting the 10 calls a minute limit


get_images_from_archive(465)
