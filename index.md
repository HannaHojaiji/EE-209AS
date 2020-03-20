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





### Prior Work

### References



### Welcome to GitHub Pages.
This automatic page generator is the easiest way to create beautiful pages for all of your projects. Author your page content here [using GitHub Flavored Markdown](https://guides.github.com/features/mastering-markdown/), select a template crafted by a designer, and publish. After your page is generated, you can check out the new `gh-pages` branch locally. If you’re using GitHub Desktop, simply sync your repository and you’ll see the new branch.

### Designer Templates
We’ve crafted some handsome templates for you to use. Go ahead and click 'Continue to layouts' to browse through them. You can easily go back to edit your page before publishing. After publishing your page, you can revisit the page generator and switch to another theme. Your Page content will be preserved.

### Creating pages manually
If you prefer to not use the automatic generator, push a branch named `gh-pages` to your repository to create a page manually. In addition to supporting regular HTML content, GitHub Pages support Jekyll, a simple, blog aware static site generator. Jekyll makes it easy to create site-wide headers and footers without having to copy them across every page. It also offers intelligent blog support and other advanced templating features.

### Authors and Contributors
You can @mention a GitHub username to generate a link to their profile. The resulting `<a>` element will link to the contributor’s GitHub Profile. For example: In 2007, Chris Wanstrath (@defunkt), PJ Hyett (@pjhyett), and Tom Preston-Werner (@mojombo) founded GitHub.

### Support or Contact
Having trouble with Pages? Check out our [documentation](https://help.github.com/pages) or [contact support](https://github.com/contact) and we’ll help you sort it out.
