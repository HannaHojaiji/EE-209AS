# ECE 209AS Project Repository 
UCLA ECE 209AS "Security and Privacy for Embedded Systems, Cyber-Physical Systems, and Internet of Things" Project, Winter 2020

Project Website: https://hannahojaiji.github.io/HannaHojaiji209.github.io/

## Team members
Mark Chen, Hannaneh Hojaiji, Riyya Hari Iyer


<a name="table"></a>
## Table of Contents
* [Introduction](#introduction)
* [Project Proposal](#project-proposal)
* [Deliverables](#deliverables)
* [Threat Model](#threat-model)
* [Technical Approach](#technical-approach)
* [Timeline for the Project](#timeline-for-the-project)
* [Results and Evaluations](#results-and-evaluations)
* [Limitations](#limitations)
* [Demonstration](#demonstration)
* [References](#references)
* [Online Resources](#online-resources)


## Introduction
The seamlessing pairings of off-the-shelf Internet-of-Things (IoT) wearable sensors eanble user to read sensor data from multiple devices at once without too much difficulty. However, the lack of user attentions may lead to inaccurate sensor data collection and loss of devices by adversaries. To address this security vulnerabiility in body sensor network while maintaining the conveiences of IoT wearable sensors, a same-body authentication and verification system is required.

<a href="#table">Back to Table of Contents</a>

## Project Proposal
The goal of this project is to present a same-body authentication system that aims to secure and associate sensor readings of a user’s body sensor network to that particular user. In addition, this system provides actionable feedback and on-board abnormality detection when verifying the integrity of a body sensor network. It notifies the user about the lost/stolen node in the body sensor network before that node loses its bluetooth connection due to being out-of-range. With this condition met, a user will have greater chance to halt inaccurate sensor readings from the body sensor network and/or to recover his or her lost/stolen wearables before the adversary is gone.

<a href="#table">Back to Table of Contents</a>

## Deliverables
- An Android app, named SameBodyAuth, that authenticate and periodically verifies whether a three-device sensor network (smartphone, Motorola moto 360 watch, and Nokia eSense earable) is on the same body.  
- Data analysis plots of collected sensor data from some of these  devices’  accelerometers and gyroscopes. 
- Codes and scripts that authenticate phone and wearables, periodically check body sensor network integrity, record the sensor data,  and analyze the correlations among the sensor data.
- Video demo that illustrates the uses of our Android app in recognizing lost/stolen devices and notifying user to recover them.


<p align="center">
	<img src="https://hannahojaiji.github.io/HannaHojaiji209.github.io/Media/system-components.png" width="480"/>
	<br/>
	<strong>The three-device sensor network and the SameBodyAuth app</strong>
</p>


<a href="#table">Back to Table of Contents</a>


## Threat Model
- A user pairs up and wears the two wearables (eSense and watch) to perform personal sensor data collection.
- Collected data can be messed up and/or the wearables can be stolen by the following two attack scenarios:
  1. The user forget one of the wearables on an stationary object such as table. An adversary can then take away the wearable in the absence of the user. 
  2. The adversary directly grabs one of the wearables from the user. This attacker can also apply man-in-the-middle attack (MITM) to steal the device in stealth.

<a href="#table">Back to Table of Contents</a>

## Technical Approach
### Initial User Authentication
The SameBodyAuth app has an initial authentication module applied across the paired smartphone, eSense earable, and Moto 360 watch. The user will follow the following guidelines to let the app check if he or she is using all the threepaired devices:
- User generates a pin on the phone (NO visual display)
- The eSense speaks out the pin to user on the earable via text-to-speech
- The user receives the pin from eSense
- The user then types the pin on watch then send it to the phone. The user is able to do this throught the keyboard implemented at the    watch interface
- The phone verfifies whether  the received typed pin is the same as the one that generated earlier. 
- Once the verification is successful and the two pins turn out to be the same, app branches to the next activity where data collection takes place

This cyclic authentication ensures that the same person is using the sensor arrays from the three paired devices. In other words, the user will know if he or she wear the correct wearable(s) before using the SamebodyAuth app on his or her phone ot collect sensor data.

<p align="center">
	<img src="https://hannahojaiji.github.io/HannaHojaiji209.github.io/Media/Initial-User-Authentication.png" height="640"/>
	<br/>
	<strong>Components of the SameBodyAuth app's cyclic authentication activity</strong>
</p>


### Periodic Same-body Verification
After the user successfully authenticated the three paired devices, the SameBodyAuth app then starts its periodic verifying module. The system will perform the following steps to tell whether any of the paired wearables on the user's body is lost or stolen:

- The phone collect contextual modalities (accelerations, angular velocities, and heart rate) in time series from the two wearables 
- The app extracts signatures/features (e.g. peak values) from these modalities for same-body verification
- The app performs data analysis on these obtained features/signatures by windowing the collected sensor data 
- The app applies decision trees on the results of the feature/signature analysis to check if a wearable is still on the same user’s body (i.e., correlation across multiple modalities and/or detection of data gaps)  
- Based on the resulting decisions, disable data communication of a wearable if it is said to be detached from user’s body (either left behind or stolen in our threat model).
- In addition to disabiling data communcation, the app will also notify the user to retrieve the lost/stolen wearable by using android toast notification

<p align="center">
	<img src="https://hannahojaiji.github.io/HannaHojaiji209.github.io/Media/Decision_Tree.png" width="480"/>
	<br/>
	<strong>High-level diagram of the same-body checking mechanism on the SameBodyAuth app</strong>
</p>


<a href="#table">Back to Table of Contents</a>
 

## Timeline for the Project
Week 3-4: Develop an android application module to generate initial authentication code that ensure the same user is using the paired smartphone and wearables (watch and earable). Program these phone, watch and earable to collect sensor data and establish a connection. 

Week 5-6: Develop an android application  module to periodically verify whether paired devices are on the same user's body. Present the midterm results. Check timing efficiency and improve the design of the perioidic verififcation module.

Week 7-8: Integrate these two modules, ensure data communication, and analyze cllected sensor data. In addition, modify and amplify the security checks between the devices. 

Week 9-10: Finalize reliability tests. Create a report, and integrate a website. Take demos. 

<a href="#table">Back to Table of Contents</a>


## Results and Evaluations
The SameBodyAuth app has been used to collect sensor data from serveral cases of human behaviors that are likely involved in the two mentioned attack scenarios (i.e., left on table and grab by adversary).

### Cases of Human Behaviors
We conduct observations on some basic human behaviors that are likely to occur in the proposed threat model. Accelerometer, gyroscope, and heart rate sensor data are collected for understanding how acceleration, angular velocity, and heart rate values change in response to these human behaviors.

#### Case # and Observed Behaviors
    0         Sitting
    1         Walking
    2         Running
    3         Put earable in from table
    4         Take earabale out of ear
    
With the insights on possible value changes in acceleration, angular velocity, and heart rates, we then record sensor data on the two proposed attack scenarios.

#### Case #  and Observed Behaviors
    5         Put earphone on table and get up
    6         Earphone stolen and adversary walks at the same pace as user
    7         Earphone stolen and adversary runs away



### Collected Sensor Profiles

<p align="center">
	<img src="https://hannahojaiji.github.io/HannaHojaiji209.github.io/Media/acc-profile.png" width="860"/>
	<br/>
	<strong>Acceleration profile from smartphone and eSense earable via SameBodyAuth app</strong> 
	<br/>
	(Left: sitting, Middle: running, Right: Walking)
	<br/>
	(Blue: smartphone, Orange: eSense earable)
</p>


<p align="center">
	<img src="https://hannahojaiji.github.io/HannaHojaiji209.github.io/Media/gyro-profile.png" width="860"/>
	<br/>
	<strong>Angular velocity profile from smartphone and eSense earable via SameBodyAuth app</strong>
	<br/>
	(Left: Grabbed by running adversary, Middle: Grabbed by same-pace adversary, Right: Left by the user)
	<br/>
	(Blue: smartphone, Orange: eSense earable)
</p>


### Key Findings
- Peak detection on collected sensor data gives good insights on user' actions/motions
- Correlating multiple modalities (e.g., acceleration and angular velocities) provides better
decisions on whether a wearable is detached from the user’s body
- Fine control on sesnosr data collection can be done through registering/unregistering sensor listeners
and connect/disconnect bluetooth devices inside the app


### Metrics of Success
- The app can properly collect desired sensor data on the smartphone, Moto 360 watch, and eSense Earable✔
- Implementation of decision trees based on sensor features/signatures to detect adversarial events ✔
- Quick notification/toast for user to recognize detachment of a wearable beore bluetooth connection is lost due to out-of-range ✔

<a href="#table">Back to Table of Contents</a>

 
## Limitations
The SameBodyAuth app provides the ground work for same-body authentication accross mutliple paired devices. Given that a major proportion of time is spent on establishing authentication and verfication mechanisms, the app ends up with the following weaknesses:

1. Need to collect more sensor data from different individuals to make the same-baody checking mechanism (i.e., the decision trees) mroe robust.
2. Wearable sensors’ accuracies often dictates the outcomes of adversarial detection.
   - Phone and wearables' accelerometers and gyroscopes are okay, but watch's heart rate sensor is not.
   - Pedometer (step counting) based verification ends up not working due to drifting in eSense earable's readings.
3. Must concatenate all the sensor values collected from the watch in a string to ensure successful data transmission.
   - The current design relies on a single phone-watch communication channel
4. Readings of sensors that are sampling at different rates may not be collected at the same time.
   - Need a way to synchronize these sensors for good timestamps  
5. Updates in Android packages and APIs cause unnecessary overheads when incorporating more sensors for adversarial detection
   - Frequent maintenance on the app’s source code is required to ensure usability
   
<a href="#table">Back to Table of Contents</a>


## Demonstration

The following link is a presentation detailing our threat model exploration, brainstroming, and intial implementation of the SameBodyAuth app

<a href="https://www.thesitewizard.com/" rel="noopener noreferrer" target="_blank">https://github.com/HannaHojaiji/HannaHojaiji209.github.io/blob/master/ECE209AS-CPS_IoT%20Midterm-Hojaiji%20Chen%20Iyer.pdf</a>


### Final Presentation

The following link is a presentation detailing our final implementation of the SameBodyAuth app and data analysis from collected sensor readings

<a href="https://www.thesitewizard.com/" rel="noopener noreferrer" target="_blank">
https://github.com/HannaHojaiji/HannaHojaiji209.github.io/blob/master/ECE209AS-CPS_IoT%20Final-Hojaiji%20Chen%20Iyer.pdf</a>
	
### Video Demo

Here's the link to our video that demonstrates the uses of the SameBodyAuth app.

<a href="https://youtu.be/Vbj39Gpa_f0" target="_blank" rel="noopener noreferrer"> Demo </a> 

<a href="#table">Back to Table of Contents</a>


## References
[1] Al Ameen, Moshaddique, Jingwei Liu, and Kyungsup Kwak. "Security and privacy issues in wireless sensor networks for healthcare applications." Journal of medical systems 36.1 (2012): 93-101.

[2] Stajano, Frank, et al., eds. Security and Privacy in Ad-hoc and Sensor Networks: 4th European Workshop, ESAS 2007, Cambridge, UK, July 2-3, 2007, Proceedings. Vol. 4572. Springer Science & Business Media, 2007.

[3] Huang, X., Wang, Q., Bangdao, C., Markham, A., Jäntti, R., & Roscoe, A. W. (2011, October). Body sensor network key distribution using human interactive channels. In Proceedings of the 4th International Symposium on Applied Sciences in Biomedical and Communication Technologies (pp. 1-5).

[4] Schürmann, D., Brüsch, A., Sigg, S., & Wolf, L. (2017, March). BANDANA—Body area network device-to-device authentication using natural gAit. In 2017 IEEE International Conference on Pervasive Computing and Communications (PerCom) (pp. 190-196). IEEE.

[5] Wang, F., Li, Z., & Han, J. (2019). Continuous user authentication by contactless wireless sensing. IEEE Internet of Things Journal, 6(5), 8323-8331.

[6] Shi, C., Liu, J., Liu, H., & Chen, Y. (2017, July). Smart user authentication through actuation of daily activities leveraging WiFi-enabled IoT. In Proceedings of the 18th ACM International Symposium on Mobile Ad Hoc Networking and Computing (pp. 1-10).

[7] Cornelius, C. T., & Kotz, D. F. (2012). Recognizing whether sensors are on the same body. Pervasive and Mobile Computing, 8(6), 822-836. 

[8] M. Shahzad and M. P. Singh, "Continuous Authentication and Authorization for the Internet of Things," in IEEE Internet Computing, vol. 21, no. 2, pp. 86-90, Mar.-Apr. 2017.

[9] Lin, S., et al. (2019). Natural Perspiration Sampling and in Situ Electrochemical Analysis with Hydrogel Micropatches for User-Identifiable and Wireless Chemo/Biosensing. ACS sensors.


<a href="#table">Back to Table of Contents</a>


## Online Resources

### Nokia eSense earable
- eSense Earables website: <br>
  https://www.esense.io/

- EarComp 2019 Document: <br>
  https://www.esense.io/earcomp/EarComp-PreACM.pdf
  
- Basic application for eSense earables using Android studio to log IMU data: <br>
  https://github.com/nesl/eSenseTester


### Android smartphone applications
- Random Number Generator for first time authentication: <br>
  https://www.youtube.com/watch?v=ID4YJHoXbEw

- Saving data(accelerometer, gyroscope) as a Text File: <br>
  https://www.youtube.com/watch?v=BnYruBLqdmM

- senslog (logging phone sensor data to phone as text file): <br>
  https://github.com/tyrex-team/senslogs
  
- Simple Pedometer for smartphones: <br>
  https://github.com/google/simple-pedometer
  
- Android phone and watch bidirectional message exchange: <br>
  https://github.com/jeffreyliu8/Android-Wearable-Send-Message-bi-directional
  
- Read heartbeat rate from an Android watch: <br>
  https://github.com/macsj200/BeatWatch
  
  
### Motorola Moto 360 Watch


**NOTE: RESOURCE LINKING FAILS WHEN COMPILE SDK VERSIONS OF MOBILE AND WEAR DO NOT MATCH SO MAKE SURE THEY DO**


## Steps to program the watch, track the sensor data, record, store and anlyze the data 

###### 1. To use/pair the watch first you need to reset the watch, pair with your phone using the "Wear OS" app from Google play. 
###### 2. To program the watch on CMD go to the platform-tools where you have the adb debugging. The run the following two commands

###### 3. To get the wear app started, you need to set the following dependencies:
                    dependencies {
                      implementation 'androidx.wear:wear:1.0.0'
                      ~~implementation 'com.google.android.support:wearable:2.5.0'~~
                      ~~compileOnly 'com.google.android.wearable:wearable:2.5.0'~~
                    }
                    Make sure your sdk version is 28 for any font variations, 
                    In main gradle: classpath 'com.android.tools.build:gradle:3.5.3'
                    In gradle wrapper: distributionUrl=https\://services.gradle.org/distributions/gradle-5.4.1-all.zip
                    Get the latest versions of the gradele and plugins to be able to run this code. 
###### 4. Implement the ADB debugging over BT on watch. Download the ADB debugging toolset zipped in this repo and follow these tutorials to program the watch through Android Studio. 
                    (1) https://developer.android.com/studio/releases/platform-tools.html
                    (2) https://www.youtube.com/watch?v=v6_mtCAOops
                    (3) https://developer.android.com/training/wearables/apps/debugging

## Debugging:
1.If you are not able toconnect to watch trough the server try the following:
To debug the adb server https://androidforums.com/threads/adb-shell-error-more-than-one-device-and-emulator.48572/

2. The gradle run issues for runproguard:
https://stackoverflow.com/questions/27078075/gradle-dsl-method-not-found-runproguard

3. “Default activity not found”: https://stackoverflow.com/questions/27970210/default-activity-not-found-for-a-wearable-app-created-with-android-studio-temp

4.Error: resource android:attr/fontVariationSettings not found: compileSdkVersion 28

5. After upgrade android version getting “Duplicate class android.support.v4.app.INotificationSideChannel”: Put these flags in your gradle.properties
                        android.enableJetifier=true
                        android.useAndroidX=true
    https://stackoverflow.com/questions/55810694/after-upgrade-android-version-getting-duplicate-class-android-support-v4-app-in
    For gradle version compatibility in your own set up, make sure you adjust the parameters according to this table
![System flowchart](https://github.com/HannaHojaiji/EE-209AS/blob/master/Media/Compatibility%20Chart.png)
        [https://developer.android.com/studio/releases/gradle-plugin#updating-gradle]

6. Solving gradle problems and versions: https://stackoverflow.com/questions/44546849/unsupported-method-baseconfig-getapplicationidsuffix
          
               After updating to 3.3 and Maven repositories. change the following codes in 
                    wear dependencies build.gradle
                        compile 'com.google.android.support:wearable:2.5.0'
                        compile 'com.google.android.gms:play-services-wearable:17.0.0'
               shared dependencies build.gradle    
                        compile fileTree(include: ['*.jar'], dir: 'libs')
                        compile 'com.android.support:appcompat-v7:22.2.1'
                        compile 'com.google.android.gms:play-services-wearable:17.0.0'
                        compile 'com.google.android.support:wearable:2.5.0'
                mobile dependencies build.gradle        
                        compile fileTree(include: ['*.jar'], dir: 'libs')
                        wearApp project(':wear')
                        compile 'com.android.support:appcompat-v7:21.0.3'
                        compile 'com.google.android.gms:play-services:12.0.1'
                        compile project(':shared')
                After upgrade android version Put these flags in your gradle.properties
                        android.enableJetifier=true
                        android.useAndroidX=true

              After that update the commands (android x, etc.)
              Update the mimSDK to 14 from 9 by pasting this whole thing
                        android {
                        compileSdkVersion 28
                        buildToolsVersion '28.0.3'

                        defaultConfig {
                            minSdkVersion 21
                            targetSdkVersion 28
                            versionCode 1
                            versionName "1.0"
                        }
              Import @ nullable (inputstreamconnection.java) and comment out the android version of Nullable and keep the AndroidX version
              * Data collector app
              Then 4 above make sure the plugin (in buildgradle version) and gradle version in properties match the table
              Then 2
              change buildToolsVersion '25.0.0'
              android.useAndroidX=true
              android.enableJetifier=true
              Then change the imports as you install
              if there's no watch face: https://codelabs.developers.google.com/codelabs/watchface/index.html#0
7. Setting up AndroidX for wear programming:https://stackoverflow.com/questions/55756647/duplicate-classes-from-androidx-and-com-android-support

8. Small update for wearable depedencies of the gradle of android wear: https://github.com/android/wear-os-samples/blob/master/WearDrawers/Wearable/build.gradle


9. Migrate Wear apps to GoogleApi: https://developer.android.com/training/wearables/data-layer/migrate-to-googleapi

## Other resources used to program the watch and utilize permission, data collection services, etc. 
1. Making watch face circular: https://developer.android.com/training/articles/wear-permissions
2. Velocity calcualtion based on the GPS and IMU: https://github.com/android/wear-os-samples/tree/master/SpeedTracker
**Note for this project please install the 3.4.1 version of Android from this version as the new beta Android Studio version updates deprecate the previous gradle versions https://developer.android.com/studio/archive

3. Keyboard for integration: https://github.com/idoideas/Wear-Keyboard
4. Procedure to integrate communication of the watch and the phone: 
          https://forum.xda-developers.com/android/software/android-data-transferring-tutorial-t3182153
          https://medium.com/@manuelvicnt/android-wear-accessing-the-data-layer-api-d64fd55982e3

5. Continuous data syncing with watch: https://developer.android.com/training/wearables/data-layer/accessing.html#java

