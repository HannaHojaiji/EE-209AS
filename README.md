# ECE 209AS Project Repository 
UCLA ECE 209AS "Security and Privacy for Embedded Systems, Cyber-Physical Systems, and Internet of Things" Project, Winter 2020


## Team members
Mark Chen, Hannaneh Hojaiji, Riyya Hari Iyer


## Project proposal

In this project, we propose the "same-body" authentication for a set of sensor networks (e.g. earables, wearables). 
This authenication process will comprise a initial-pairing phase and a periodic-verifying phase. By utilizing the loacality (sensors in user's possession or attached to user's body) and wireless (NFC, WiFi and Bluetooth) data communication, we will ensure reliable conenctivity and locality across the desired user's mobile phone and set of sensor networks.  

![System flowchart](https://github.com/HannaHojaiji/EE-209AS/blob/master/System%20flowchart.png)


The initial-pairing phase can be done through daily-based first-time authentication of the sensor network and the mobile device. Once the mobile phone recognized a targeted sensor, we will use mobile app to record the participants and the timestamp of this initial-pairing instance.

![established communication setup with smartwatch](https://github.com/HannaHojaiji/EE-209AS/blob/master/watch%20communication.png)


Then, throughout the day, periodic authentication for localized connection is needed. 
This was an IP address of the shared WiFi connection and a secret key is sent over short distance communication, which ensures that the sensor network is locally activated on the same individual user.
The data communication will be encrypted to ensure security of the pins shared between the sensor network and the smartphone. 
![established communication with the watch](https://github.com/HannaHojaiji/EE-209AS/blob/master/established%20communication.png)

To verify whether the set of sensor networks is indeed located on a target indiviual's body, we will explore and utilize tools and methods that create additonal shared information (e.g., vibration patterns, sound patterns, etc.) and test the existence of these shared information among the mobile device and the set of sensor networks. 


## Materials needed
1) For the first stage: two Android phones or an android phone and a smartwatch (one as a receiver and master device, the other as a passive sensor array with NFC connection)
2) After setting up this connection, we can utilize a passive NFC tag as a local (and physical) key identifier.
3) If a simple sensor system is needed, we can build one by using arduino, sensor module, and wifi module. 

## Timeline for the project
Week 3-4: Develop the android application to generate the NFC code and authentication. Program phone/watch to colelct sensor data and establish connection. 

Week 5-6: Develop periodic local key and wifi (IP address) sharing to keep aquiring the data. Present the midterm results. Check the power efficiency profile and improve the results.

Week 7-8: Implement the key encryption for the data communication and physical checks. Modify and amplify the security checks between the devices. 

Week 9: Finalize reliability tests. Create report, and integrate website. Take demos. 

## References
###### [1] Zhang, Jiansong, et al. "Proximity based IoT device authentication." IEEE INFOCOM 2017-IEEE Conference on Computer Communications. IEEE, 2017.
###### [2] Cornelius, Cory T., and David F. Kotz. "Recognizing whether sensors are on the same body." Pervasive and Mobile Computing 8.6 (2012): 822-836.
###### [3] Han, Jun, et al. "Do you feel what I hear? Enabling autonomous IoT device pairing using different sensor types." 2018 IEEE Symposium on Security and Privacy (SP). IEEE, 2018.
###### [4] Anand, S. Abhishek, and Nitesh Saxena. "Noisy Vibrational Pairing of IoT Devices." IEEE Transactions on Dependable and Secure Computing 16.3 (2018): 530-545.








