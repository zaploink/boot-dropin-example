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

# Running the example

The example application outputs a random quote-of-the-day, which is to be provided by a drop-in component.
If the expected class is not found (i.e. drop-in directory has been misconfigured or no drop-in JAR has been found)
then the application falls back to a default implementation that outputs a hard-coded quote.

```
./gradlew clean bootRun
```

Will run the application. The `dropin.directory` has been configured in `application.properties` but does not
yet contain a JAR. Therefore the application will output an error and use the fallback quote provider.

```
2020-03-09 01:27:42.133  INFO 19804 --- [           main] o.z.e.dropin.DropinExampleApplication    : Starting DropinExampleApplication on LAPTOP-P924427F with PID 5588 ([...]/boot-dropin-example/application)
2020-03-09 01:27:42.660  INFO 19804 --- [           main] o.z.e.dropin.DropinExampleApplication    : No active profile set, falling back to default profiles: default
2020-03-09 01:27:42.742 ERROR 19804 --- [           main] o.z.example.dropin.DropinClassLoader     : Configured drop-in directory '[...]/boot-dropin-example/application/../dropin-impl/build/libs/' does not exist or is not a directory. No drop-ins will be loaded.
2020-03-09 01:27:42.987  WARN 19804 --- [           main] o.z.example.dropin.QuotesProvider        : Drop-in 'Quotes' is not present, providing fallback implementation.
2020-03-09 01:27:42.988  INFO 19804 --- [           main] quote-of-the-day                         : ----------------------------------------------------------------------------------------------
2020-03-09 01:27:42.988  INFO 19804 --- [           main] quote-of-the-day                         : The absence of a message sometimes is a presence of one. -- Hasse Jerner
2020-03-09 01:27:42.988  INFO 19804 --- [           main] quote-of-the-day                         : ----------------------------------------------------------------------------------------------
2020-03-09 01:27:43.029  INFO 19804 --- [           main] o.z.e.dropin.DropinExampleApplication    : Started DropinExampleApplication in 0.712 seconds (JVM running for 1.092)
```

Now, let's build the real `Quote` provider Drop-in: 

```
./gradlew :dropin-impl:build
```

Will build the actual implementation of the Drop-in and create a JAR in the already configured `dropin.directory`.
Start the application again:

```
./gradlew bootRun
```

Now, the "dropped" component should be found and loaded and the output will be different (quote may vary):

```
2020-03-09 01:38:08.133  INFO 9496 --- [           main] o.z.e.dropin.DropinExampleApplication    : Starting DropinExampleApplication on LAPTOP-P924427F with PID 5588 ([...]/boot-dropin-example/application)
2020-03-09 01:38:08.138  INFO 9496 --- [           main] o.z.e.dropin.DropinExampleApplication    : No active profile set, falling back to default profiles: default
2020-03-09 01:38:08.141  INFO 9496 --- [           main] o.z.example.dropin.DropinClassLoader     : Drop-in directory is: [...]\boot-dropin-example\application\..\dropin-impl\build\libs
2020-03-09 01:38:08.247  INFO 9496 --- [           main] o.z.example.dropin.DropinClassLoader     : Adding the following external drop-in JAR files to class path: [[...]/boot-dropin-example/application/../dropin-impl/build/libs/dropin-impl.jar]
2020-03-09 01:38:08.250  INFO 9496 --- [           main] o.z.example.dropin.QuotesProvider        : Drop-in 'Quotes' found with version: 1.0.0
2020-03-09 01:38:08.253  INFO 9496 --- [           main] quote-of-the-day                         : ----------------------------------------------------------------------------------------------
2020-03-09 01:38:08.253  INFO 9496 --- [           main] quote-of-the-day                         : From little acorns mighty oaks do grow. -- American proverb
2020-03-09 01:38:08.253  INFO 9496 --- [           main] quote-of-the-day                         : ----------------------------------------------------------------------------------------------
2020-03-09 01:38:08.579  INFO 9496 --- [           main] o.z.e.dropin.DropinExampleApplication    : Started DropinExampleApplication in 1.049 seconds (JVM running for 1.554)
```

## Configuration of the drop-in directory

Please note that the drop-in directory is configured in the `application.properties` file. 
For this example it is configured relative to `${user.dir}`, which evaluates to the process dir. When running 
the application using gradle (as described here) `${user.dir}` will resolve to the `application` module, because that's
where the `bootRun` task is defined.

When you run the application from your IDE, e.g. by using a launch config, the process directory may change and
is more likely to be equal to the project root directory. 

This shows that it is generally not a good idea to provide a hard-coded drop-in directory location. 
You can provide a different location in your launch config with `-Ddropin.directory=./dropin-impl/build/libs` 
or by setting the env variable `DROPIN_DIRECTORY`.

# Going further

A more advanced implementation of this would be based on a plugin-mechanism, that scans 
for a series of components that implement a pre-defined abstract plugin interface,
registers them with their version and exposes them to the application.
