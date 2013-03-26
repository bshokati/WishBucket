<img src="https://raw.github.com/bshokati/WishBucket/master/Images/wb.jpg" width="100px">

WishBucket
==========

WishBucket is an Android app that helps organize your bucket list dreams, allows you
to share them with friends, gives you suggestions for new dreams, and 
provides Groupon deals to help you achieve them.

##Technical Features
You may want to use some of the features of Wish Bucket for your own projects. 
<br>You can find examples of the following within this app:
+ SQLite Tables
+ Content Loaders
+ List View with images/text
+ Expandable List Views
+ Custom Adapters for lists
+ Custom Radio Buttons and Checkboxes
+ Accessing Android Photos Gallery
+ GET calls to Groupon
+ GPS location for deals
+ AsyncTask calls for network access
<br>... and many more!

##Source Code
The package was developed using Eclipse Juno and resides within the "development"
directory. You may import the package from within Eclipse as a project if you'd like.
<br> 
To use the Facebook features, you must add your own unique app id as the value for the 
"app_id" string in WishBucket/development/WishBucket/res/values/strings.xml. 
If you don't have an app id, you can request one by creating a Facebook app from the
[Facebook Android Developers page](https://developers.facebook.com/docs/getting-started/facebook-sdk-for-android/3.0/).
<br>
To use the Groupon feature, you must request a key from the
[Groupon API](http://www.groupon.com/pages/api) and place it into the "client_id" field in
the doBackground() method of the GetDealsTask inner class in
 development/WishBucket/src/com/example/wishbucket/DealsFragment.java.

##Quick Install
Currently Wish Bucket only targets Android 4.0.3 and above.
Was developed and optimized for the Samsung Galaxy Nexus.
Download the apk from the "Release" directory from your android phone.

## History
WishBucket started as a team project for UC San Diego's Computer Science 190 course
(Android Mobile Applications) under Prof. Greg Hoover. It was decided to make the app
an open source project in order to contribute to the android app development community.


## Team Members
[Ben Shokati](www.linkedin.com/in/bshokati0software1engineer) - Software Developer<br>
[Nima Hashemi](www.linkedin.com/pub/nima-hashemi/42/b98/75b) - Program Manager<br>
[Shazzy Gustafson](www.linkedin.com/pub/shazzy-gustafson/66/547/994) - User Interface Designer