
# Tanzania Health Facility Registry mediator

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/83968316d58146889a6387a5c44444b2)](https://app.codacy.com/gh/SoftmedTanzania/hfr-mediator?utm_source=github.com&utm_medium=referral&utm_content=SoftmedTanzania/hfr-mediator&utm_campaign=Badge_Grade)

An [OpenHIM](http://openhim.org/) mediator for processing health facility data  from hfr and sending it to various health systems.

# Getting Started
Clone the repository and run `npm install`

Open up `src/main/resources/mediator.properties` and supply your OpenHIM config details and save:

```
  mediator.name=HFR-Mediator
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

