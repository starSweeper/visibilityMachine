import requests
from bs4 import BeautifulSoup
import re
from datetime import datetime
from urllib.request import urlretrieve
import time


# This function copied from http://pydoc.net/weather/0.9.1/weather.units.temp/
# And then translated from C++ to Python, with code to convert between celsius and fahrenheit hard coded
# And code added to reformat result
# Temperature conversion from:
#  http://www.pythonforbeginners.com/code-snippets-source-code/python-code-celsius-and-fahrenheit-converter
# ASSUMES FAHRENHEIT!!!!!!!
def calc_dew_point(temp_in_f, hum):

    c = (float(temp_in_f) - 32) * 5.0/9.0
    x = 1 - 0.01 * float(hum)

    dew_point = (14.55 + 0.114 * c) * x
    dew_point = dew_point + ((2.5 + 0.007 * c) * x) ** 3
    dew_point = dew_point + (15.9 + 0.117 * c) * x ** 14
    dew_point = c - dew_point
    dew_point = 9.0/5.0 * dew_point + 32
    dew_point = round(dew_point, 2)
    dew_point = str(dew_point).replace('.', 'p')

    return dew_point


# Takes a number that is supposed to be 2 digits and adds a leading zero if it is not
def add_lead_zero(string_to_add_to):
    if len(string_to_add_to) != 2:
        string_to_add_to = '0' + string_to_add_to

    return string_to_add_to


# Pulls newest web cam photo and weather data from website
def get_photo():
    # Prepare "soup"
    req = requests.get('http://swo.odf.state.or.us/')  # Medford cam
    soup = BeautifulSoup(req.text, 'html.parser')

    # Parse data from website
    soup_as_string = str(soup.find('body').find_all('b')[1])
    soup_as_string = soup_as_string.replace('\n', '')
    soup_as_string = re.sub('<[^>]+>', '', soup_as_string)
    soup_as_string = re.sub('[^0-9^.]', ' ', soup_as_string)
    extracted_data = soup_as_string.split()

    temperature = extracted_data[0].replace('.', 'p')
    humidity = extracted_data[1].replace('.', 'p')
    calculated_dew_point = calc_dew_point(extracted_data[0], extracted_data[1])
    time_captured = add_lead_zero(extracted_data[3]) + add_lead_zero(extracted_data[4])
    date = add_lead_zero(extracted_data[5]) + add_lead_zero(extracted_data[6]) + add_lead_zero(extracted_data[7])

    # Pull most recent image from site
    image_path = 'newData/medford_' + temperature + '_' + humidity + '_' + calculated_dew_point +\
                 '_' + time_captured + '_' + date + '.jpg'
    urlretrieve('http://swo.odf.state.or.us/~webcam/webcam.jpg', image_path)

    print('Image retrieved at ' + str(datetime.now()))


def is_it_ready_yet():
    minute = datetime.now().minute
    if minute == 0:
        print("At last, my moment has arrived... retrieving image!")
        get_photo()
        print("1 hour remaining until next image is pulled.")
        time.sleep(3600)
        is_it_ready_yet()
    elif minute == 1:
        print("Its a little later than I would ideally like to be doing this, but retrieving image!")
        get_photo()
        print("A little less than an hour remaining until next image is pulled.")
        time.sleep(3540)
        is_it_ready_yet()
    elif minute > 1:
        print("Not ready yet :/ " + str(60 - minute) + " minutes remaining until I can check again.")
        time.sleep((60 - minute) * 60)
        is_it_ready_yet()


def can_i_haz_photo_pls():
    get_photo()
    is_it_ready_yet()


can_i_haz_photo_pls()
