Game Library 2D
===============

About
-----
A toolkit for developing 2D games in Java.

So, why should you use this library if it doesn't even support 3D? Well, I'm not sure you should! But if you do, it's completely free. I also accept pull requests (and name suggestions 😁). The goal of this project has always been to keep it simple and have fun programming. By limiting the scope to 2D I could manage the workload as a free-time project and focus on important functionality needed to create performant and good-looking 2D games. The API is extendible and hopefully easy to use.

`Game Library 2D` includes support for particles, lightning, animations, collision detection, sound effects and much more. You can easily create split-screen games with full joystick/controller support or create your own game server and network game. The quickest way to get started is to look at the demo applications.

Requirements
------------
- Java JDK 13
- Apache Maven

Getting started
---------------
The API ships in several modules, each a separate maven dependency.

To start developing your game you need to add at least the following two dependencies:

```xml
<dependency>
  <groupId>com.gamelibrary2d</groupId>
  <artifactId>gamelibrary2d-core</artifactId>
  <version>0.4.0</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.gamelibrary2d</groupId>
  <artifactId>gamelibrary2d-framework-lwjgl</artifactId>
  <version>0.4.0</version>
</dependency>
```

Additional modules are available in order to provide sound, collision detection, network support, etc.

You can see how these modules are used by looking at the source code of the demo applications.

Building
--------
Clone the repository and run the following Maven command from the root folder:
```
mvn package
```
A jar-folder will be created with the following three sub folders:
- demos - The Demo applications.
- api - The Game Library 2D modules.
- tools - Tools and applications.

Versioning
----------
No major version has been released. Breaking changes might occur at any time.