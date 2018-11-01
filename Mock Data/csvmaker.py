import csv
import random
import datetime

for i in range(1,100):

    filename="./BPdata/bp"+str(i)+".csv"
    with open (str(filename), 'w', newline='') as csvfile:
        filewriter = csv.writer(csvfile, delimiter=',')
        for x in range(10):
            systolic = random.randint(70,190)
            diastolic = random.randint(40,100)
            fullstring = str(systolic)+ "/" + str(diastolic)
            time = datetime.
            print(fullstring)
            filewriter.writerow([time, fullstring])