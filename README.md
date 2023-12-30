HTTP4e2
====

HTTP4e2 is an Eclipse HTTP Client plugin simplifying REST and Web Service testing, enabling developer to make HTTP packets directly from Eclipse. The packets include all HTTP requests, responses and the HTTP headers (which contain the cookies and caching information). It is a useful tool for HTTP header tampering and hacking, allowing HTTP code generation to a programming language of choice, as well as JMeter load testing.


### More information
http://nextinterfaces.com/http4e


![alt tag](http://nextinterfaces.com/http4e/images/main.png)

### Key Features
* Multiple Tabs 
* Auto Suggest (CTRL + SPACE)
* Response view format into Raw, Pretty, JSON, HEX, Browser DOM
* SSL/HTTPS and Unicode support
* Basic/Digest authentication
* Proxy support
* Parameterization of Headers/HTTP-Params 
* HTTP code export to Java, JavaScript, Prototype, jQuery, Flex, ActionScript, C#, VisualBasic, Ruby, Python, PHP, Cocoa and JMeter
* One click HTML Report generation
* Multipart Form support


### Install URL
http://nextinterfaces.com/http4e/install

Build Instructions
--------------

## With Ant

```
project-root> ant
```

This will generate 

```
install/
  features/
  plugins/
  http4e-eclipse-rest-http-client.tar
```

## With Maven

```
project-root> mvn build
```

And look into `http4e-Site/target`

To update version
```bash
mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=1.0.0-SNAPSHOT
```



License
--------------
Apache License v.2.0

<a href="http://with-eclipse.github.io/" target="_blank">
<img alt="with-Eclipse logo" src="http://with-eclipse.github.io/with-eclipse-0.jpg" />
</a>
