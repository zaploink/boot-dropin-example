This sample project demonstrates how a simple "drop-in" mechanism for spring-boot
can be implemented.

# Drop-in
A _drop-in_ is a JAR that provides the implementation of an (optional) application component
which can be provided at runtime, e.g. in the target environment of the application.
If present, it will be loaded at server start, otherwise not.

Such an implementation may be desirable for cases such as:
1. external dependency can not be part of the packaging process (e.g. due to licensing restrictions)
1. you want to be able to provide upgrades for a specific component without redeployment
1. different customers require different dependencies/implementations and you don't want to set up a separate build 
  target for each customer
 
## Possible Pitfalls 
- Extra care should be taken when you intend to regularly update the external component.
Since the component is loosely coupled, you should ensure that component implementtions provide 
at least a version of some sort, so that you always know what is installed in the system, and can 
also implement compatibility or sanity checks, if necessary.

- Also note that the presented simple approach can be dangerous when your
component has many dependencies in possibly different versions than the main application.
Since all those dependencies end up on the class-path as well, there might be
class version conflicts. So it's best to keep drop-in implementations as slim and
dependency-free as possible. Alternatively consider using a proper service-oriented architecture,
 such as OSGi, which uses separate classloaders to prevent leaking of (unwanted) depenedencies.
 
# Example implementation
The provided implementation is very crude: it only checks for a specific drop-in/component
implementation to be present on the class path, and if so, provides it to the application as a bean.

If no implementation is found, then a minimal fall-back implementation will be provided.

**The main goal of this example is to demonstrate how a specific component can be decoupled 
from the main application's build and only be provided at runtime/in the target environment.** 

## Setup
- main application
- shared module with drop-in component interface definition
- example drop-in component implementation (not a dependency of the main application)

The latter has been included as a sub-module for convenience reasons (so that the example can be loaded 
into the IDE as a single project), but note that normally the implementation
would be a completely separate project with an own build.

# Spring Boot PropertiesLauncher

Enable with 

```
bootJar {
	manifest {
		attributes 'Main-Class' : 'org.springframework.boot.loader.PropertiesLauncher'
	}
}
```

Configuring the Spring-boot PropertiesLauncher to launch the application will load the 
launcher first and then use the `boot.jar` and any configured external classpaths to execute
the main class. 

For more information see the [official documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html#executable-jar-launching),
the [PropertiesLauncher API documentation](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/loader/PropertiesLauncher.html)
and the [PropertiesLauncher source code](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/PropertiesLauncher.java).

## gradle / bootRun

This behavior does _not_ work when using the `bootRun` task, because this task does never invoke the
configured launcher, but instead runs the application's main class directly in dev mode, with the gradle
classpath as application classpath. Launcher config properties have no effect here.

## IDE / IntelliJ

Launching the application from an IDE such as IntellJ does not execute the `PropertiesLauncher`.
The application main class is usually started directly, using the main module's classpath.

To enforce usage of the `PropertiesLauncher`, a specialized launch configuration has to be set up,
as described by Andy Wilkinson in this [Stackoverflow post](https://stackoverflow.com/a/37889382/2119598):

> When you use PropertiesLauncher it sets up a class loader with the contents of the configured loader.path and then uses this class loader to load and call your application's main class. When you launch your application's main class directly in your IDE, PropertiesLauncher isn't involved so the loader.path system property has no effect.
>  
> It is possible to use PropertiesLauncher in your IDE but it'll require a bit of extra configuration. You'll need to configure a run configuration that has spring-boot-loader and your application on the classpath that launches PropertiesLauncher. You can then use the loader.main system property to tell PropertiesLauncher the name of your application's main class.
  
A simpler way to make the external / provided library part of the IntelliJ launch config is to 
add it with `compileOnly` to the project's build script. This will result in _not_ packaging the dependency
but to include it in the launch config's classpath, as described in [this Gradle blog post](https://blog.gradle.org/introducing-compile-only-dependencies),
specifically:

> As part of our commitment to quality IDE support, compile-only dependencies continue to work with Gradle’s IDEA and Eclipse plugins. When used within IntelliJ IDEA, compile-only dependencies are mapped to IDEA’s own provided scope. Within Eclipse, compile-only dependencies are not exported via project dependencies.

## java / launch scripts
`PropertiesLauncher` works with `java -jar application.jar` and launcher configuration parameters
can be provided via JVM options, e.g. `java -Dloader.path=lib,/opt/lib -Dloader.home=. -jar application.jar` 
will add any external jars inside the `./lib` and `/opt/lib` directories to the classpath.

Use `-Dloader.debug=true` to show debug output when launching the app.

# Going further
A more advanced implementation of this would be based on a plugin-mechanism, that scans 
for a series of components that implement a pre-defined abstract plugin interface,
registers them with their version and exposes them to the application.
