# ECE 209AS Project Repository 
UCLA ECE 209AS "Security and Privacy for Embedded Systems, Cyber-Physical Systems, and Internet of Things" Project, Winter 2020


## Team members
Mark Chen, Hannaneh Hojaiji, Riyya Hari Iyer


## Project proposal

The ubiquity of IoT sensors and wearable devices can potentially cause users to accidentally wear sensors that are not paired with their own cellphone. This kind of situation can lead to unwanted sensor data collection by unauthorized personnel. 

![System flowchart](https://github.com/HannaHojaiji/EE-209AS/blob/master/System%20flowchart.png)


In this project, we propose the "same-body" authentication for a set of sensor networks (e.g. earables, wearables). 
This authenication process will comprise a initial-pairing phase and a periodic-verifying phase. By utilizing the loacality (sensors in user's possession or attached to user's body) and wireless (NFC, WiFi and Bluetooth) data communication, we will ensure reliable conenctivity and locality across the desired user's mobile phone and set of sensor networks.  

![same body sensor network](https://github.com/HannaHojaiji/EE-209AS/blob/master/same%20body%20sensor%20network.png)



The initial-pairing phase can be done through daily-based first-time authentication of the sensor network and the mobile device. This may be done by close-proximity authenication technique such as NFC. Once the mobile phone recognized a targeted sensor, we will use mobile app to record the participants and the timestamp of this initial-pairing instance.

The data collection will begin after the inital-pairing is done.

![established communication setup with smartwatch](https://github.com/HannaHojaiji/EE-209AS/blob/master/watch%20communication.png)

Then, throughout the day, periodic authentication for reliable localized connection is needed. To accomplish this, we will register unique user response signatures for the sensors in the sensor network. During an periodic authentication instance, we will first enable the sensor network to introduce human-recognizable event(s) (e.g., a short sound, a vibration, etc.) to the user. Once the user receive the event(s), the mobile phone app analyze the captured user response pattern to see if the sensor network is indeed in user's possession or is attached to user's body. 

The data communication between the sensor network and the mobile phone will be disabled if the captured user response pattern is not consistent with the previous collected user response signature. A re-authentication via initial-pairing is needed to re-enable the data communication if the user desire to continue sensor data collection. 

![established communication with the watch](https://github.com/HannaHojaiji/EE-209AS/blob/master/established%20communication.png)


## Materials needed
1) For the first stage: Collect an Android phone and a collection of sensors that includes a smartwatch and a earable. We will then set up stable wireless connection betweeen the Android phone and these sensors so that authenication and data communication can be achieved.

2) After setting up this connectioon, we will implement inital-pairing process with an Android app. For example, we can utilize a passive NFC tag as a local (and physical) key identifier and an Andorid app that help establish NFC authenication and WiFi/bluetooth connection.

3) For the periodic-verifying process, we will leverage the acuators inside the smartwatch and the earable to create 
human-recognizable event(s). Then, the android app will be updated to analyze user response pattern.

4) Optional: If a custom sensor module is needed, we can build one by using arduino, sensor chips, and wifi chips. 



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








