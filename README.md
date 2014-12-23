# Anypic-Android

This is my custom version of the [Anypic](https://anypic.org) application, fully powered by [Parse](https://parse.com). Anypic is the easiest way to share photos with your friends. Get the app and share your fun photos with the world. 

Here is the [iOS tutorial](https://parse.com/tutorials/anypic) from which this version is derived. It is fully integrated with Facebook for user authentication.
 
## Initial Setup

Make sure you have the Android SDK installed on your system. The version of the Eclipse IDE with ADT (Android Developer Tools) built-in can be found [here](http://developer.android.com/sdk/index.html)

## How to Run

1) Clone the repository and open the project in Eclipse 

* (For Android Studio set up, you're on your own) 

2) Create your Anypic App on [Parse](https://parse.com/apps).

3) Add your Parse application ID and client key in `AnypicApplication.java`
```java

Parse.initialize(this, "<APP_ID>", "<CLIENT_KEY>");
```

4) Set up the Facebook SDK

* follow the [Facebook User guide](https://www.parse.com/docs/android_guide#fbusers) in Parse's Android documentation. 
* You will have to create a Facebook application for Anypic, and then you will have to put the Facebook Application ID into `AnypicApplication.java`

```java
ParseFacebookUtils.initialize("YOUR FACEBOOK APP ID");
```
* **Make sure that the `facebooksdk.jar` file is [added as an Android Dependency](http://stackoverflow.com/questions/20355971/how-do-i-add-a-new-library-to-android-dependencies-using-eclipse-adt) to your project**.

5) Build the project and run
