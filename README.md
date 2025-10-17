# 51Degrees IP Intelligence Engines - Examples

![51Degrees](https://51degrees.com/img/logo.png?utm_source=github&utm_medium=repository&utm_content=readme_main&utm_campaign=java-open-source "Data rewards the curious") **Java IP Intelligence**

[Developer Documentation](https://51degrees.com/ip-intelligence-java/index.html?utm_source=github&utm_medium=repository&utm_content=documentation&utm_campaign=java-open-source "developer documentation")

## Introduction

These examples are not distributed as maven jars and need to be built by you.

## Required Files

See [ip-intelligence-data](https://github.com/51Degrees/ip-intelligence-data/)
repository for instructions on obtaining the necessary data files for on-premise detection.

**IMPORTANT**: The enterprise IP Intelligence data file must be placed in the `ip-intelligence-data`
directory at the root of this repository. The expected filename is `51Degrees-EnterpriseIpiV41.ipi`.

```
ip-intelligence-java-examples/
├── ip-intelligence-data/
│   └── 51Degrees-EnterpriseIpiV41.ipi
├── console/
├── web/
└── shared/
```

This project contains sub-modules - **console**, giving examples that are intended
to be run from the command line/console and **web**, illustrating use
of 51Degrees Web/Servlet integration. There is also a **shared** sub-module
containing various helpers for the examples.

Among other things, the examples illustrate:
- use of the fluent builder to configure a pipeline
- use of a configuration options file to configure a pipeline
- use of the on-premise IP intelligence service
- use of IP intelligence pipeline for offline processing tasks
- configuring IP intelligence trade-offs between speed and conserving memory

## Examples

The table below describes the examples available in this repository.

### Cloud (coming soon)

Cloud examples will be added once the Cloud service for IP Intelligence becomes available.

### On-Premise

| Example                  | Description                                                                                                                                                                                                                    |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GettingStartedOnPrem (Console) | How to use the 51Degrees on-premise IP intelligence API to determine details about IP addresses.                                                                                                                       |
| GettingStartedWebOnPrem (Web)   | How to use the 51Degrees on-premise IP intelligence service to determine details about IP addresses as part of a simple Java servlet website.                                                                         |
| MetadataOnPrem           | How to access the meta-data that relates to the properties available in IP intelligence detection.                                                                                                                             |
| OfflineProcessing        | Example showing how to ingest a file containing IP addresses and perform IP intelligence detection against the entries.                                                                                                        |
| PerformanceBenchmark     | How to configure the various performance options and run some simple performance tests for IP intelligence.                                                                                                                    |
| UpdateDataFile           | How to configure the Pipeline to automatically update the IP intelligence data file on startup. Also illustrates 'file watcher'. This will refresh the IP intelligence engine if the specified data file is updated on disk. |

## Running Examples and Tests

**IMPORTANT**: All examples and tests must be run from the repository root directory (`ip-intelligence-java-examples/`).

### Running Tests

To run all tests:

```bash
# From the repository root directory
mvn test
```

To run tests for a specific module:

```bash
# Console tests
mvn test -pl console

# Web tests
mvn test -pl web/getting-started.onprem
```

### Running Examples with Maven

Console examples can be run directly using Maven exec plugin:

```bash
# From the repository root directory
mvn compile exec:java -pl console -Dexec.mainClass="fiftyone.ipintelligence.examples.console.GettingStartedOnPrem"
```

Web examples:

```bash
# From the repository root directory
mvn compile exec:java -pl web/getting-started.onprem -Dexec.mainClass="fiftyone.ipintelligence.examples.web.GettingStartedWebOnPrem"
```

The web example will start a Jetty server on port 8082. Access it at: `http://localhost:8082`

### Running Examples from Packaged JARs

Alternatively, you can build "fat" JARs and run examples from the command line.

First, build the packages:

```bash
mvn package
```

This will produce JARs with dependencies inside `target` subfolders.

Then run examples using the JAR:

```bash
java -cp .\console\target\ip-intelligence-java-examples.console-4.4.19-jar-with-dependencies.jar fiftyone.ipintelligence.examples.console.OfflineProcessing
```
