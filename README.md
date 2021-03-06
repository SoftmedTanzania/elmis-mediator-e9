
# Tanzania Epicor9 - eLMIS mediator
[![Java CI Badge](https://github.com/SoftmedTanzania/elmis-mediator-e9/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/SoftmedTanzania/elmis-mediator-e9/actions?query=workflow%3A%22Java+CI+with+Maven%22)
[![Coverage Status](https://coveralls.io/repos/github/SoftmedTanzania/elmis-mediator-e9/badge.svg?branch=development)](https://coveralls.io/github/SoftmedTanzania/elmis-mediator-e9?branch=development)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/83968316d58146889a6387a5c44444b2)](https://app.codacy.com/gh/SoftmedTanzania/elmis-mediator-e9?utm_source=github.com&utm_medium=referral&utm_content=SoftmedTanzania/elmis-mediator-e9&utm_campaign=Badge_Grade)

An [OpenHIM](http://openhim.org/) mediator for processing data  from MSD Epicor9 and sending it to eLMIS.

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
  java -jar target/elmis-mediator-e9-0.1.0-jar-with-dependencies.jar
```

