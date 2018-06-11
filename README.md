# jddclient
jddclient is a dynamic DNS client written in Java



Crossplatform dynamic DNS client

jddclient is a dynamic DNS client written in Java. Dynamic DNS providers make it possible to assign 
a well-known, stable domain name to a computer or router which has a variable, often changing IP 
address. jddclient determines the public IP address of the computer or the router and submits it to 
a dynamic DNS provider. It then periodically checks if the IP address was changed and if necessary 
it notifies the provider.

Features: 
* It runs on the Java Platform, so it can be installed on Windows, Linux etc. There is a downloadable deb package specifically for Ubuntu / Debian. 
* Easy configuration using two simple files (one for logging) 
* It can retrieve the public IP address from these sources: 
  * home/small business router with HTML administration interface 
  * a public web page usually created for this purpose by the providers 
  * local network card 
* Currently it supports the following providers: 
  * DnsMadeEasy.com 
  * DynDns.com 
* The author does not preclude the support of non-free providers. Actually this is the reason why this project was started. 
* Detailed logging using the Logback library. It can be customized, the configuration is well documented by the Logback project. 
* Notification e-mails if an update failed (part of logging) 
* It follows the recommendations of dynamic DNS providers, especially about avoiding unnecessary updates
