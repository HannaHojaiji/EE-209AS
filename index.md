---
title: Same-body Sensor Network Security
--- 

<a name="table"></a>
### Table of Contents
* [Introduction](#introduction)
* [Objectives](#objectives)
* [Deliverables](#deliverables)
* [Threat Model](#threat-model)
* [Technical Approach](#technical-approach)
* [Prior Work](#prior-work)
* [References](#references)

### Introduction
The seamlessing pairings of off-the-shelf wearable sensors may lead to inaccurate sensor data collection and loss of devices due to the lack of user attentions.
<br></br>

<a href="#table">Back to Table of Contents</a>

### Objectives
Our authentication system aims to secure and associate sensor readings of a user’s body sensor network to that particular user.In addition, the system provides actionable feedback and on-board abnormality detection when verifying the integrity of a body sensor network. It notifies the user about the lost/stolen node in the body sensor network before that node loses its bluetooth connection.

<a href="#table">Back to Table of Contents</a>

### Deliverables
- An Android app, named SameBodyAuth, that authenticate and periodically verifies whether a three-device sensor network (smartphone, Motorola moto 360 watch, and Nokia eSense earable) is on the same body.  
- Data analysis plots of collected sensor data from some of these  devices’  accelerometers and gyroscopes. 
- Codes and scripts that authenticate phone and wearables, periodically check body sensor network integrity, record the sensor data,  and analyze the correlations among the sensor data.
- Video demo that illustrates the uses of our Android app in recognizing lost/stolen devices and notifying user to recover them.

<a href="#table">Back to Table of Contents</a>


### Threat Model
- A user pairs up and wears the two wearables (eSense and watch) to perform personal sensor data collection.

- Collected data can be messed up and/or the wearables can be stolen by the following two attack scenarios:
  1. The user forget one of the wearables on an stationary object such as table. An adversary can then take away the wearable in the absence of the user. 
  2. The adversary directly grabs one of the wearables from the user. This attacker can also apply man-in-the-middle attack (MITM) to steal the device in stealth.

<a href="#table">Back to Table of Contents</a>


### Technical Approach
In this three-device body sensor network, we implement two same-body checking mechanisms. The first same-body checking mechannism works on correlating accelerometer and gyroscope readings and is applied on the eSense earable. The second same-body checking mechanism works on detecting gaps in the heart rate sensing of watch and is applied on the moto 360 watch. Moreover, we devise a cyclic authentication method that operates across all the paired devices. This cyclic authentication method ensures that the same user is indeed using the three devices at the same time.

For evaluating the presented threat model, we first ensure proper device placements and pairings on a selected user before he or she initiating the cyclic authentication activity of the SameBodyAuth app. Once the user sucessfully passed the cyclic authentication, the app transits into the periodic verifying activity and starts collecting sensor data from all paired devices. If any of the above two same-body checking mechanisms senses an abonormality in the derived signatures of the collected sensor data, then the app will terminate data acquisition from the affected wearable and notify the user the associated attack scenario. This resulting notification/warning from will pop up on the phone screen in a timely fashion so that the user can recover the stolen/lost wearable before the bluetooth connection is lost.    


#### Initial User Authentication
The SameBodyAuth app has an initial authentication module applied across the paired smartphone, eSense earable, and Moto 360 watch. The user will follow the following guidelines to let the app check if he or she is using all the threepaired devices:
- User generates a pin on the phone (NO visual display)
- The eSense speaks out the pin to user via text-to-speech
- The user receives the pin from eSense
- The user then types the pin on watch then send it to the phone
- The phone enables sensor data collection upon confirming the received typed pin is the same as the one that generated earlier. 

This cyclic authentication through all devices ensures that the same person is using the sensor arrays from the three paired devices. In other words, the user will know if he or she wear the correct wearable(s) when using the SamebodyAuth app on his or phone for sensor data collection.


#### Perioidc Same-body Verification



### Prior Work
There has been research in the domain of context sensing and ensuring that the devices are on the same person. WiFi-enabled authentication is an area of the research work in this domain. For example, Wang, Li, and Han’s publication presents BodyPIN, a light-weight and robust technique that performs user authentication through quantifying human body effects on bypassing Wi-Fi signals from commodity Wi-Fi devices [5]. On the other hand, Shi, Liu and Chen’s work discusses about extracting Channel State Information (CSI) measurements of IoT devices' WI-FI signals to identify human physiological and behavioral characteristics and applying a deep learning algorithm on these characterisitcs to identify individual users [6].

Given this enlightment of leveraging human body effects on data and signals and the fact of several sensor types existed among smartphones and wearables, we decide to apply feature/signature extraction and analysis on the avaialble sensor types across the three devices (smartphone, Moto 360 watch, and eSense earable). Among all these devices, inertial measurement unit (IMU) sensors are mostly available. In particular, accelerometer and gyroscope sensors can provide steady readings that enable feature/signature extraction for this project. Cornelius and Kotz’s[7] research talks about the reliability and economical cost of accelerometer. 

To summarizze, we built our systems upon these methods and ideas and choosing the best approaches utilizing the array of sensor signatures introduced. Yet, we also devised and presented an cyclic authentication method that utilize wireless data transfer among the three devices to see if the same user is operating them at the same time.


### References
[1] Al Ameen, Moshaddique, Jingwei Liu, and Kyungsup Kwak. "Security and privacy issues in wireless sensor networks for healthcare applications." Journal of medical systems 36.1 (2012): 93-101.

[2] Stajano, Frank, et al., eds. Security and Privacy in Ad-hoc and Sensor Networks: 4th European Workshop, ESAS 2007, Cambridge, UK, July 2-3, 2007, Proceedings. Vol. 4572. Springer Science & Business Media, 2007.

[3] Huang, X., Wang, Q., Bangdao, C., Markham, A., Jäntti, R., & Roscoe, A. W. (2011, October). Body sensor network key distribution using human interactive channels. In Proceedings of the 4th International Symposium on Applied Sciences in Biomedical and Communication Technologies (pp. 1-5).

[4] Schürmann, D., Brüsch, A., Sigg, S., & Wolf, L. (2017, March). BANDANA—Body area network device-to-device authentication using natural gAit. In 2017 IEEE International Conference on Pervasive Computing and Communications (PerCom) (pp. 190-196). IEEE.

[5] Wang, F., Li, Z., & Han, J. (2019). Continuous user authentication by contactless wireless sensing. IEEE Internet of Things Journal, 6(5), 8323-8331.

[6] Shi, C., Liu, J., Liu, H., & Chen, Y. (2017, July). Smart user authentication through actuation of daily activities leveraging WiFi-enabled IoT. In Proceedings of the 18th ACM International Symposium on Mobile Ad Hoc Networking and Computing (pp. 1-10).

[7] Cornelius, C. T., & Kotz, D. F. (2012). Recognizing whether sensors are on the same body. Pervasive and Mobile Computing, 8(6), 822-836. 

[8] https://www.csc2.ncsu.edu/faculty/mpsingh/papers/columns/IC-21-02-Auth-NWI-17.pdf

[9] Lin, S., et al. (2019). Natural Perspiration Sampling and in Situ Electrochemical Analysis with Hydrogel Micropatches for User-Identifiable and Wireless Chemo/Biosensing. ACS sensors.

