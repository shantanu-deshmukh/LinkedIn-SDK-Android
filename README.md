> This library is deprecated. Authentication via WebViews is no longer supported by Android. Please see [this](https://support.google.com/faqs/answer/12284343) for more details. 

# LinkedIn-SDK-Android

A lightweight android library to implement Login with LinkedIn in your Android app.


Table of contents
=================
- [Why this UnOfficial SDK?](#why-this-unofficial-sdk-)
- [Adding the SDK to your Project](#adding-the-sdk-to-your-project)
  * [Method 1: Using JCenter](#method-1--using-jcenter)
  * [Method 2: Using Jitpack](#method-2--using-jitpack)
- [Usage](#usage)
  * [Authenticating](#authenticating)
  * [Retrieving updated profile](#retrieving-updated-profile)
    + [LinkedInUser Class](#linkedinuser-class)
- [Security](#security)
- [Contributing](#contributing)
- [Support](#support)


Why this UnOfficial SDK?
========================
* Existing SDKs have been discontinued. [Read More >>](https://engineering.linkedin.com/blog/2018/12/developer-program-updates)
* [Official docs](https://developer.linkedin.com/docs/android-sdk-auth) on developer.linkedin.com are outdated. 
* Weird JSON returned by the new APIs. See following example: 
```json
{
    "handle": "urn:li:emailAddress:3775708763",
    "handle~": {
        "emailAddress": "hsimpson@linkedin.com"
    }
}
```
Variable names in JAVA cannot have `~`, so you cannot use dynamic JSON parsers OR networking libraries like Retrofit.


Adding the SDK to your Project
===============================
Method 1: Using JCenter
-----------------------
Just add the dependency to your app level `build.gradle` file

```gradle
dependencies {
    implementation 'com.shantanudeshmukh:linkedinsdk:1.0.0'
}
```

> If you are getting `Failed to resolve` ERROR, make sure that Jcenter repository is added to your project level `build.gradle` file. This is done by default in recent versions of Android Studio.

Method 2: Using Jitpack
-----------------------
1. Make sure the Jitpack repository is present in your project level `build.gradle` file

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency to your app level `build.gradle` file

```gradle
dependencies {
    implementation 'com.github.shantanu-deshmukh:LinkedIn-SDK-Android:1:0'
}
```

Usage
=====

Authenticating
--------------

1. Add internet permission to your `AndroidManifest.xml` file if it's not already added.

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

2. Initiate Login Request. (You might want to do this on click of a login button)
```Java
LinkedInBuilder.getInstance(MainActivity.this)
        .setClientID("<YOUR_CLIENT_ID_HERE>")
        .setClientSecret("<YOUR_CLIENT_SECRET_HERE>")
        .setRedirectURI("<YOUR_REDIRECT_URL_HERE>")
        .authenticate(LINKEDIN_REQUEST_CODE);
```
> You can download the official Sign In with LinkedIn button images from [here](https://content.linkedin.com/content/dam/developer/branding/signin_with_linkedin-buttons.zip)

3. Handling Result: the sdk returns `LinkedInUser` object which contains the result data.

```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LINKEDIN_REQUEST_CODE && data != null) {
            if (resultCode == RESULT_OK) {
                //Successfully signed in
                LinkedInUser user = data.getParcelableExtra("social_login");

                //acessing user info
                Log.i("LinkedInLogin", user.getFirstName());

            } else {

                if (data.getIntExtra("err_code", 0) == LinkedInBuilder.ERROR_USER_DENIED) {
                    //Handle : user denied access to account

                } else if (data.getIntExtra("err_code", 0) == LinkedInBuilder.ERROR_FAILED) {
                    
                    //Handle : Error in API : see logcat output for details
                    Log.e("LINKEDIN ERROR", data.getStringExtra("err_message"));
                }
            }
        }

    }

```

Retrieving updated profile
-------------------------------
```Java
LinkedInBuilder.retrieveBasicProfile(linkedInUser.getAccessToken(), linkedInUser.getAccessTokenExpiry(), new OnBasicProfileListener() {
                    @Override
                    public void onDataRetrievalStart() {
                        //show progress bar if required
                    }

                    @Override
                    public void onDataSuccess(LinkedInUser linkedInUser) {
                        //handle retrived data
                    }

                    @Override
                    public void onDataFailed(int errCode, String errMessage) {
                        //handle error
                    }
                });

```


### LinkedInUser Class
|  Return       |  Method          | Description |
| ------------- |:-------------:|:-------------:|
| String    | `getId()` | Returns LinkedIn user ID |
| String    | `getEmail()`      | Returns users email (May return `null`)  |
| String    | `getFirstName()`      | Returns first name of the user|
| String    | `getLastName()`      | Returns last name of the user|
| String    | `getProfileUrl()`      | Returns profile url of the user|
| String    | `getAccessToken()`      | Returns access token that can be used to retrive data later. You might want to store it for later use.|
| long      | `getAccessTokenExpiry()`      | Expiry timestamp of the access token |



Security
========
To protect against [CSRF](https://en.wikipedia.org/wiki/Cross-site_request_forgery) during authorization, the sdk uses a 16 character token by default. If you want to use your own CSRF token, then use the `setState` method of the `LinkedInBuilder` class.

```Java
LinkedInBuilder.getInstance(MainActivity.this)
        .setClientID("<YOUR_CLIENT_ID_HERE>")
        .setClientSecret("<YOUR_CLIENT_SECRET_HERE>")
        .setRedirectURI("<YOUR_REDIRECT_URL_HERE>")
        .setState("<YOUR_CSRF_TOKEN_HERE>")
        .authenticate(LINKEDIN_REQUEST_CODE);
```

Contributing
============
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Support
=========
If you need any help, feel free to connect with me on twitter 
<a href="https://twitter.com/intent/tweet?screen_name=askShantanu&ref_src=twsrc%5Etfw" class="twitter-mention-button" data-show-count="false">@askShantanu</a>

OR 

Follow me for latest updates <a href="https://twitter.com/askShantanu?ref_src=twsrc%5Etfw" class="twitter-follow-button" data-show-count="false">@askShantanu</a>.

See my other work at [shantanudeshmukh.com](https://shantanudeshmukh.com)
