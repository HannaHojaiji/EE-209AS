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
* [Periodic Authentication] (#periodic-authentication)
* [Success Metrics] (#success-metrics)
* [Limitations]
* [Grounds for Future Work]
* [Midterm Presentation]
* [Final Presentation]
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
- The eSense speaks out the pin to user on the earable via text-to-speech
- The user receives the pin from eSense
- The user then types the pin on watch then send it to the phone. The user is able to do this throught the keyboard implemented at the    watch interface
- The phone verfifies whether  the received typed pin is the same as the one that generated earlier. 
- Once the verification is successful and the two pins turn out to be the same, app branches to the next activity where data collection takes place

This cyclic authentication through all devices ensures that the same person is using the sensor arrays from the three paired devices. In other words, the user will know if he or she wear the correct wearable(s) when using the SamebodyAuth app on his or phone for sensor data collection.


#### Perioidc Same-body Verification



### Prior Work
There has been research in the domain of context sensing and ensuring that the devices are on the same person. WiFi-enabled authentication is an area of the research work in this domain. For example, Wang, Li, and Han’s publication presents BodyPIN, a light-weight and robust technique that performs user authentication through quantifying human body effects on bypassing Wi-Fi signals from commodity Wi-Fi devices [5]. They devised a mechanism where a computer receives a biometric input. It receives this input once in a period of 30 seconds, where the input reception lasts for 4 seconds. These inputs are then added to a cluster and then a  bayes' range is formed. Every incoming input is compared with the cluster to see if there's a mismatch or not. In case of a mismatch the computer denies access.

On the other hand, Shi, Liu and Chen’s work discusses about extracting Channel State Information (CSI) measurements of IoT devices' WI-FI signals to identify human physiological and behavioral characteristics and applying a deep learning algorithm on these characterisitcs to identify individual users [6]. They extract the features from the WiFi signals of IoT devices like smart refrigerator to gain an insight about the user movements and then send it through a deep learning classifier.

Given this enlightment of leveraging human body effects on data and signals and the fact of several sensor types existed among smartphones and wearables, we decide to apply feature/signature extraction and analysis on the avaialble sensor types across the three devices (smartphone, Moto 360 watch, and eSense earable). Among all these devices, inertial measurement unit (IMU) sensors are mostly available. In particular, accelerometer and gyroscope sensors can provide steady readings that enable feature/signature extraction for this project. Cornelius and Kotz’s[7] research, for example is one such that talks about the reliability and economical cost of accelerometer. Their work is primarily for application in medical fields and to counter the problem of one family member picking up the equipment of the other.

### Taking things forward

To summarize, we built our systems upon these methods and ideas and choosing the best approaches utilizing the array of sensor signatures introduced. Accelerometer data, being a reliable one, is one thing that we picked up. In addition to this, we added many other modalities like gyroscopic data, velocity, displacement and heart rate sensing.

Furthermore, we also devised and presented an cyclic authentication method that utilize wireless data transfer among the three devices to see if the same user is operating them at the same time. 

### Initial authentication
We setup the initial authentication mechanism that takes place before any data collection. The way it's implemented(pin heard on the earable, pin typed through the watch and verified at the phone) ensures that the authentication would succeed only when all three devices are with the same person. It's only when this authentication succeeds that data collection takes place. Our application coding ensures this.

### Periodic Authentication

The periodic authentication works this way:

It collects the accelerometer data, gyroscope from the three devices and heart rate sensing from the phone as well as the watch. Velocity and displacement potential are derived from the accelerometer values.
In implementing our approach, we collected data and did a data analysis using Python to extract correlation between the data from different devices. 
We constructed a decision tree by studying this data and implemented it.

Thus the decision tree was implemented and the app checks the decision tree to check whether the device lies on the same person or not and does this periodically. Upon sensing mismatch, the app snaps the bluetooth connection before the devices can get out of range and notifies the user via a "toast". Toast, over here, refers to a shot-term message that appears on the bottom of the phone screen. Please note that our project is based on the underlying assumption that the phone always lie with the user.

Keeping in mind the various scenarios that can possibly take place, we have collected and analysed data by keeping the following cases in mind.

## Case#      Description

    0         Sitting
    1         Walking
    2         Running
    3         Put earable in from table
    4         Take earabale out of ear
    5         Put earphone on table and get up
    6         Earphone stolen and adversary walks at the same pace as user
    7         Earphone stolen and adversary runs away
    
 
  
 <a href="#table">Back to Table of Contents</a>
 
 
 ### Success Metrics
 
 1. Properly collect wearables’ sensor data through wireless transmission 
 2. Implementation of decision trees based on sensor signatures to detect adversarial events 
 3. Quick notification/toast to user about the detachment of a wearable 
 
 <a href="#table">Back to Table of Contents</a>
 
 
 
 ### Limitations
 
1. Need to collect more data samples from more individuals to refine the decision trees.
2. Wearable sensors’ sensing accuracies affect the outcome of adversarial detection.
   a. Accelerometer and gyroscope are okay, but heart rate sensor is not
   b. Pedometer (step counting) ends up not working due to drifting in eSense.
3. Must concatenate the sensor values collected from the watch in a package to synchronize better and more smoothly.
4. Updates in Android packages and APIs cause unnecessary overheads when incorporating more sensors for adversarial detection
   - Frequent maintenance on the app’s source code is required to ensure usability
   
   <a href="#table">Back to Table of Contents</a>
   
   
### Grounds for Future Work

1. Train and use a machine learning algorithm/model that is resilient 
2. User-specific behavioural signatures.
3. Add more sensor types and proper authentication/verification configurations to the system for enabling personalization on same-body sensor network.
4. Search or develop more accurate sensing devices to improve the reliability of the system  
5. Apply this methods to healthcare platforms as was mentioned in Lin et al. paper[9]

<a href="#table">Back to Table of Contents</a>

### Midterm Presentation

Here's a presentation detailing our work and progress upto the midterm

<a href="https://www.thesitewizard.com/" rel="noopener noreferrer" target="_blank">https://github.com/HannaHojaiji/HannaHojaiji209.github.io/blob/master/ECE209AS-CPS_IoT%20Midterm-Hojaiji%20Chen%20Iyer.pdf</a>

<a href="#table">Back to Table of Contents</a>


### Final Presentation

Here's our final presentation

https://github.com/HannaHojaiji/HannaHojaiji209.github.io/blob/master/ECE209AS-CPS_IoT%20Final-Hojaiji%20Chen%20Iyer.pdf

<a href="#table">Back to Table of Contents</a>




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

