# SignON Orchestrator
The SignON Orchestrator repository is part of [SignON](https://signon-project.eu/) an EU H2020 research and innovation funded project.  
The SignON Orchestrator manages and coordinates incoming requests from the [SignON Mobile App](https://github.com/signon-project/wp2-mobile-app) and [SignOn ML App](https://github.com/signon-project/wp2-ml-app), and it is responsible to interact internally with the SignON Pipeline.  

In detail, the SignON Mobile App interfaces with the SignON Orchestrator using REST APIs (see [SignON Orchestrator OpenAPI](https://github.com/signon-project/wp2-orchestrator-openapi)), while a message broker facilitates communication between the SignON Orchestrator and SignON Dispatchers.  
The SignON Orchestrator activates the SignON Dispatchers, which in turn contact the object storage (if necessary) and the SignON Pipeline components responsible for the related processing based on the different input (text, audio, or video) and the requested output (text, audio, avatar).

For further details please refer to public deliverable [D2.5 - Final release of the Open SignON Framework](https://signon-project.eu/publications/public-deliverables/).

## Getting Started

### Prerequisites
- JAVA VM: [OpenJDK 11](https://openjdk.org/)
- Message Broker: [RabbitMQ](https://www.rabbitmq.com/)
- Object Storage: [MinIO](https://min.io/)
- SignON Dispatchers:
    - [SignON WP3 Dispatcher](https://github.com/signon-project/wp2-dispatcher-for-wp3)
    - [SignON WP4 Dispatcher](https://github.com/signon-project/wp2-dispatcher-for-wp4)
    - [SignON WP5 Dispatcher](https://github.com/signon-project/wp2-dispatcher-for-wp5)
- Server stub generator:
    - [AsyncAPI Generator v1.8.27](https://github.com/asyncapi/generator)
        ```
        npm install -g @asyncapi/generator@1.8.27
        ```
    - [AsyncAPI Java Spring Template v0.18.3](https://github.com/asyncapi/java-spring-template)
        ```
        npm install -g @asyncapi/java-spring-template@0.18.3
        ```

For further details about the configuration of the components please refer to [SignON Framework Docker Compose](https://github.com/signon-project/wp2-framework-docker-compose).

### Installation
- Clone this repository with recursive option:  
    ```
    git clone --recursive git@github.com:signon-project/wp2-signon-orchestrator/signon-orchestrator.git
    ```

- Compile and generate code:  
    ```
    mvn clean install
    ```

### Usage
1. Configure the SignON Orchestrator (`src/main/resources/application.yml`)
2. Start the SignON Orchestrator
      ```
      docker build signon-orchestrator-X.X.X.jar
      /java -Dspring.config.location=/your/repo/path/config.yml -jar /your/repo/path/signon-orchestrator-X.X.X.jar 
      ```
      > N.B. `config.yml` overwrites `src/main/resources/application.yml`.

3. Simulate SignON Mobile App requests with `cURL`  
    Thanks to the REST APIs, it is possible to simulate the SignON Mobile App and SignOn ML App requests using the `cURL` command from a terminal (see [Example Requests](docs/ExampleRequests.md)).


## Additional information
### Compatibility Matrix

| signon-orchestrator | signon-orchestrator-openapi | signon-orchestrator-asyncapi |
|:-------------------:|:---------------------------:|:----------------------------:|
|         17.0        |             14.0            |             8.0              |
|         15.0        |             13.0            |             8.0              |
|         14.0        |             12.0            |             8.0              |
|         13.0        |             11.0            |             8.0              |
|         12.0        |             11.0            |             8.0              |
|         11.0        |             10.0            |             7.0              |
|         10.0        |             9.0             |             7.0              |
|         9.0         |             8.0             |             7.0              |
|         8.2         |             7.1             |             7.0              |
|         8.0         |             7.0             |             7.0              |
|         7.0         |             6.0             |             6.0              |
|         6.0         |             5.0             |             5.0              |
|         5.0         |             4.0             |             4.0              |
|         4.0         |             4.0             |             4.0              |
|         3.0         |             3.0             |             3.0              |
|         2.1         |             2.0             |             2.0              |
|         2.0         |             2.0             |             2.0              |
|         1.0         |             1.0             |             1.0              |
|         0.1         |             0.1             |             0.1              |

### Documentation
The following technical documentation is available:
* SignON Orchestrator OpenAPI ([link](https://github.com/signon-project/wp2-signon-orchestrator-openapi/tree/master/docs/markdown))
* Errors Documentation ([link](/docs/Errors.md))

#### Other details
- IDE: Visual Studio Code
- The `asyncapi_codegen.sh` script is used by maven to generate the server stub from the AsyncAPI (see `pom.xml`)

## Authors
This project was developed by [FINCONS GROUP AG](https://www.finconsgroup.com/) within the Horizon 2020 European project SignON under grant agreement no. [101017255](https://doi.org/10.3030/101017255).  
For any further information, please send an email to [signon-dev@finconsgroup.com](mailto:signon-dev@finconsgroup.com).

## License
This project is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
