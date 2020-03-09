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

# Going further
A more advanced implementation of this would be based on a plugin-mechanism, that scans 
for a series of components that implement a pre-defined abstract plugin interface,
registers them with their version and exposes them to the application.
