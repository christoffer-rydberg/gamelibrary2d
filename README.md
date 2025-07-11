Game Library 2D
===============

About
-----
`Game Library 2D` is a cross-platform 2D game library for Java. It is free to use and comes with a wide range of
features to create great looking and performant games. Visual effects include particle systems, lightning and
animations. The library also offers extensive multiplayer support. You can easily create your own game server and/or
make local multiplayer games with split-screen and controller support.

Requirements
------------

- Java JDK 8
- Apache Maven

Getting started
---------------
The library ships in several modules, each a separate maven dependency.

Every game needs to include the core module:

```xml

<dependency>
    <groupId>com.gamelibrary2d</groupId>
    <artifactId>gamelibrary2d-core</artifactId>
    <version>0.25.0</version>
</dependency>
```

And an OpenGL framework implementation:

```xml

<dependency>
    <groupId>com.gamelibrary2d</groupId>
    <artifactId>gamelibrary2d-framework-lwjgl</artifactId>
    <version>0.25.0</version>
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