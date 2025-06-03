# 51Degrees IP Intelligence Engines - Examples

![51Degrees](https://51degrees.com/img/logo.png?utm_source=github&utm_medium=repository&utm_content=readme_main&utm_campaign=java-open-source "Data rewards the curious") **Java IP Intelligence**

[Developer Documentation](https://51degrees.com/ip-intelligence-java/index.html?utm_source=github&utm_medium=repository&utm_content=documentation&utm_campaign=java-open-source "developer documentation")

## Introduction

These examples are not distributed as maven jars and need to be built by you.

## Required Files

See [ip-intelligence-data](https://github.com/51Degrees/ip-intelligence-data/) 
repository for instructions on obtaining the necessary data files for on-premise detection.

This project contains sub-modules - **console**, giving examples that are intended 
to be run from the command line/console and **web**, illustrating use
of 51Degrees Web/Servlet integration. There is also a **shared** sub-module
containing various helpers for the examples.

Among other things, the examples illustrate:
- use of the fluent builder to configure a pipeline
- use of a configuration options file to configure a pipeline
- use of the cloud IP intelligence service
- use of the on-premise IP intelligence service
- use of IP intelligence pipeline for off-line processing tasks
- configuring IP intelligence trade-offs between speed and conserving memory

## Cloud resource keys

You will require [resource keys](https://51degrees.com/documentation/_info__resource_keys.html)
to use the Cloud API, as described on our website. Get resource keys from
our [configurator](https://configure.51degrees.com/), see our [documentation](https://51degrees.com/documentation/_concepts__configurator.html) on
how to use this.
 
A resource key configured with the properties needed
to run most of the examples can be obtained [here](https://configure.51degrees.com/jqz435Nc). 
To use the resource key in the example it can be supplied as a
command line parameter, pasted into the configuration file (where there is one)
or supplied as either an environment variable or a system
property called "TestResourceKey".

Some cloud examples require an enhanced resource key containing a license key. And some
on-premise examples require you to provide a license key. You can find out about 
resource keys and license keys at our [pricing page](https://51degrees.com/pricing). 

## Running examples with changes to Pipeline packages

A common use case is to make a change to the Pipeline logic in
ip-intelligence-java and then use these examples to observe the results of the
change.

By default, the examples are configured to use the packages from Maven central.
In order to produce and use local packages instead:

- Clone and make your changes to ip-intelligence-java
- Set the version of the device detection packages that we're going to create to 0.0.0:
  `mvn versions:set-property -Dproperty="project.version" "-DnewVersion=0.0.0"`
- Create and install the packages locally (skipping tests is needed):
  `mvn clean install [-DskipTests]`
- Modify the POM for the examples to reference these new local packages. This can
  be done by editing the POM directly or by using the command line:
  `mvn versions:set-property -Dproperty="ip-intelligence.version" "-DnewVersion=0.0.0"`

The same principle can be applied to incorporate changes in pipeline-java if needed.

## Examples

The tables below describe the examples available in this repository.

### Cloud (coming soon)

Cloud examples will be added once the cloud service for IP Intelligence becomes available.

### On-Premise

| Example                  | Description                                                                                                                                                                                                                    |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GettingStarted (Console) | How to use the 51Degrees on-premise IP intelligence API to determine details about IP addresses.                                                                                                                               |
| GettingStarted (Web)     | How to use the 51Degrees on-premise IP intelligence service to determine details about IP addresses as part of a simple Java servlet website.                                                                                 |
| Metadata                 | How to access the meta-data that relates to the properties available in IP intelligence detection.                                                                                                                             |
| OfflineProcessing        | Example showing how to ingest a file containing IP addresses and perform IP intelligence detection against the entries.                                                                                                        |
| PerformanceBenchmark     | How to configure the various performance options and run some simple performance tests for IP intelligence.                                                                                                                    |
| UpdateOnStartup          | How to configure the Pipeline to automatically update the IP intelligence data file on startup. Also illustrates 'file watcher'. This will refresh the IP intelligence engine if the specified data file is updated on disk. |

## Running built examples from command line

Running

```bash
mvn package
```

will produce "fat" JARs inside `target` subfolders.

Use them with relevant example class entrypoints like:

```bash
java -cp .\console\target\ip-intelligence-java-examples.console-4.4.19-jar-with-dependencies.jar fiftyone.ipintelligence.examples.console.OfflineProcessing
```

