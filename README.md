A common library for supporting a MVVM architecture 

Make sure it is at the same directory as the App module
ie 
/home/user/YourApp
/home/user/CommonLib

It supports the following types of repository:

MBaaS (Mobile backend as service)
ie Firebase, Kumulos etc

Retrofit

The app module can decide which to use

also contains some common:

stlyes,
dimens,
theme

The app module can decide whether to use traditonal Retrofit callbacks 
or Coroutine for making network calls
