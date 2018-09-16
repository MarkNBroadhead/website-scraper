# Website Scraper

Scrapes a website to fetch inventory information

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Maven 3, Java 8, MySQL database.

### Installing

A step by step series of examples that tell you have to get a development env running

* Add database hostname, username, password to src/main/resources/application.properties
* Install project dependencies, package jar:
    `mvn package -DskipTests`

### Running
Run the project while including information about database to deposit information. Currently only supports MySql.

`java -jar .\target\website-scraper-0.0.1-SNAPSHOT.jar`

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
