
# Tanzania Epicor9 - eLMIS mediator
[![Java CI Badge](https://github.com/SoftmedTanzania/elmis-mediator-e9/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/SoftmedTanzania/hfr-mediator/actions?query=workflow%3A%22Java+CI+with+Maven%22)
[![Coverage Status](https://coveralls.io/repos/github/SoftmedTanzania/elmis-mediator-e9/badge.svg?branch=development)](https://coveralls.io/github/SoftmedTanzania/elmis-mediator-e9?branch=development)

An [OpenHIM](http://openhim.org/) mediator for processing data  from Epicor9 and sending it to eLMIS.

# Getting Started
Clone the repository and run `npm install`

Open up `src/main/resources/mediator.properties` and supply your OpenHIM config details and save:

```
  mediator.name=ELMIS-Mediator-E9
  # you may need to change this to 0.0.0.0 if your mediator is on another server than HIM Core
  mediator.host=localhost
  mediator.port=4000
  mediator.timeout=60000

  core.host=localhost
  core.api.port=8080
  # update your user information if required
  core.api.user=root@openhim.org
  core.api.password=openhim-password
```

To build and launch our mediator, run

```
  mvn install
  java -jar target/hfr-mediator-0.1.0-jar-with-dependencies.jar
```

