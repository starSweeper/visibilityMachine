from PIL import Image
from PIL.ExifTags import TAGS
from urllib.request import urlretrieve
import urllib.request
import json


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

        image_url = 'http://swo.odf.state.or.us/~camarc/' + day + month + year + '.jpg'
        try:
            urlretrieve(image_url, image_title)
        except:
            pull_image(day, month, year)



def get_images_from_archive(limit):
    archive_date_list = tuple(open("medfordArchiveDates.txt", "r"))

    for i in range(limit):
        image_date = archive_date_list[i].split()
        pull_image(image_date[1], image_date[0], image_date[2])


get_images_from_archive(450)
