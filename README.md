GitHub User Application
-----------------------------
**Author**: Ruben Tipparach

**Description**:

This application is a REST API combined with message handlers for RabbitMQ that fetch user and repository data. The app can be run as a single deployment, or the two profiles can be deployed as separate deployments, allowing you to independently scale the API gateway and the message handlers (consumers).
**How to Run**:
  * What you'll need:
    * IntellijIDEA(recommended)
    * Docker Desktop (or equivalent local docker env)
    * Java 22 or higher
  * Open project in Intellij and run the **GitHubUserApplication**
  * or run the command: `./gradlew clean build bootRun`
  * to interact with REST API navigate to: `http://localhost:8080/swagger-ui/index.html#`


**Architecture**:
````
____________       _____________      ____________
|          |       | Message   |      |          |
| API      |  <--> | Handlers  | <--> |  Github  |
| Gateway  |       | & Caching |      |  API     |
____________       _____________      ____________
     ^                  ^
      \____Rabbit MQ___/
````
* The app is split into two parts. 
  * Part 1: The API gateway with rest controllers and Swagger UI to document the endpoints. 
  * Part 2: The message handlers that communicate via Rabbit MQ.
* Why was Rabbit MQ chosen as the message broker?
  * API is simple and lightweight for what was needed for messaging.
  * It's well documented.
  * I've actually not used it before in Java/Spring Boot (tried it in C# for fun years ago)
  * Starting the container was fast and responsive, good sign for getting things going quickly.
* Why does the API gateway use WebFlux? Is there a difference with normal Rest Controllers?
  * WebFlux provides asynchronous calls. While there is a slight overhead to programming asynchronously in Java, the exercise allowed me to use my previous experience in utilizing it for this small app.
  * Rest controllers would work just as fine for this. WebFlux couples really well with reactive DB frameworks like Hibernate Reactive.
  
**Tech Stack**:
* Spring Boot Starter
  * heavily relies on the spring boot libraries for asynchronous rest api calls
  * uses messaging to communicate between REST APIs and Web Clients that call out to GitHub
* Docker
  * Runs Rabbit MQ and Keycloak(security not implement for time) containers
* RabbitMQ
  * Primary method of passing messages within the app. Allows for independent scaling of REST API and consumers/handlers.
  * To keep the app simple, I opted to leave everything in a single project and segregate the service/components using the `@Profile` annotation.
* Misc/Other Libraries
    * Contains health actuators to monitor app health.
    * Lombok - makes Java easy like Kotlin
  
**Class Overview**:
 * GithubRestController
   * Handles incoming JSON requests from other apps or the front-end. Uses reactive webflux to avoid synchronous CPU blocking calls.
 * Producers
   * Creates the requests for the Rabbit MQ template, and sends it to the appropriate message queue.
 * Consumers
   * Contains caching logic and web client logic that calls out to GitHub.
 * GithubUserReposService
   * Calls out to the two message handlers, and formats the message in the required format.
 * CacheService
   * Manages the cache size, if it grows too much, reset the cache.
   * Although we have a cache expiration time, It's not yet implemented. This is intended to intelligently grab data once cache has expired.
   * Cache data will replace existing data if it exists and the checksum is different.
 * Unit Tests
   * Mainly focuses on testing the caching logic, rate limit fallback logic, and the data mapping logic. 

**Future Improvements**:
* Keycloak/OAuth2
    * This was cut out of the app for time, it was faster to develop and test endpoints without having to authenticate.
    * Authentication with at least a Bearer token and an Auth server to host the public key and JWT would be nice for security.
* Docker Compose the Application
  * For the app to be ready for deployment, I could make a docker file to containerize the JAR and run it in docker.
  * This isn't my strong suit so I left it out as it was all too relevant to the task of putting together a Spring app.
* Deployment scripts, CI/CD pipelines
  * I don't have a cloud service account to host anything at the moment, but this would be nice to have as it would allow the app to run on the web.
* Secrets storage
  * To make things like the credentials for RabbitMQ more secure or future authentication credentials could be stored in Vault or GitHub secrets.
* GitHub Authentication
  * While I chose the path of local caching, a github auth account would've been nice to have.
* Seperated Deployment, and separated modules.
  * To extend on the idea of the deployment scripts, we could split the app into two separate deployments and scale them independently depending on the load.
* Database fallback
  * Initially I had the idea of including a postgres migration script to create a DB, but it felt like overkill, and that local caching would suffice.

**Example**
* Command
```
curl -X 'GET' \
  'http://localhost:8080/github/user-repos/rubentipparach' \
  -H 'accept: */*'
```
* Response
```json
{
  "userMetaData": {
    "error": false,
    "timestamp": "2025-02-18T07:17:40.027421900Z",
    "cacheExpired": false,
    "checksum": 513298568,
    "cacheData": false
  },
  "repoMetaData": {
    "error": false,
    "timestamp": "2025-02-18T07:17:40.027421900Z",
    "cacheExpired": false,
    "checksum": 513298568,
    "cacheData": false
  },
  "user_name": "RubenTipparach",
  "display_name": "Ruben Tipparach",
  "avatar": null,
  "geo_location": "Ushira Galactic Federation",
  "email": null,
  "url": "https://api.github.com/users/RubenTipparach",
  "created_at": null,
  "repos": [
    {
      "name": "anekin-and-baby-cat-adventures",
      "url": "https://api.github.com/repos/RubenTipparach/anekin-and-baby-cat-adventures"
    },
    {
      "name": "AnimalStarfighter",
      "url": "https://api.github.com/repos/RubenTipparach/AnimalStarfighter"
    },
    {
      "name": "ArtificialIntelligence_Experiments",
      "url": "https://api.github.com/repos/RubenTipparach/ArtificialIntelligence_Experiments"
    },
    {
      "name": "B.U.D.D.Y.",
      "url": "https://api.github.com/repos/RubenTipparach/B.U.D.D.Y."
    },
    {
      "name": "balloon-vr-tutorial",
      "url": "https://api.github.com/repos/RubenTipparach/balloon-vr-tutorial"
    },
    {
      "name": "BaroqueUI",
      "url": "https://api.github.com/repos/RubenTipparach/BaroqueUI"
    },
    {
      "name": "br-meow",
      "url": "https://api.github.com/repos/RubenTipparach/br-meow"
    },
    {
      "name": "CardboardApocalypse",
      "url": "https://api.github.com/repos/RubenTipparach/CardboardApocalypse"
    },
    {
      "name": "Career-Fair-2.0",
      "url": "https://api.github.com/repos/RubenTipparach/Career-Fair-2.0"
    },
    {
      "name": "casino-games",
      "url": "https://api.github.com/repos/RubenTipparach/casino-games"
    },
    {
      "name": "cavetube",
      "url": "https://api.github.com/repos/RubenTipparach/cavetube"
    },
    {
      "name": "ChristmasIsland",
      "url": "https://api.github.com/repos/RubenTipparach/ChristmasIsland"
    },
    {
      "name": "CowsVsUFO",
      "url": "https://api.github.com/repos/RubenTipparach/CowsVsUFO"
    },
    {
      "name": "creator-companion",
      "url": "https://api.github.com/repos/RubenTipparach/creator-companion"
    },
    {
      "name": "custom-fps-lab",
      "url": "https://api.github.com/repos/RubenTipparach/custom-fps-lab"
    },
    {
      "name": "Dauntlet",
      "url": "https://api.github.com/repos/RubenTipparach/Dauntlet"
    },
    {
      "name": "Disqualitifcation",
      "url": "https://api.github.com/repos/RubenTipparach/Disqualitifcation"
    },
    {
      "name": "drawio-github",
      "url": "https://api.github.com/repos/RubenTipparach/drawio-github"
    },
    {
      "name": "DynamicDispatcher",
      "url": "https://api.github.com/repos/RubenTipparach/DynamicDispatcher"
    },
    {
      "name": "ezhack-nettools",
      "url": "https://api.github.com/repos/RubenTipparach/ezhack-nettools"
    },
    {
      "name": "FishCommander",
      "url": "https://api.github.com/repos/RubenTipparach/FishCommander"
    },
    {
      "name": "Fleet-Hackers-Taccom",
      "url": "https://api.github.com/repos/RubenTipparach/Fleet-Hackers-Taccom"
    },
    {
      "name": "fleet-haxor",
      "url": "https://api.github.com/repos/RubenTipparach/fleet-haxor"
    },
    {
      "name": "food-bowl",
      "url": "https://api.github.com/repos/RubenTipparach/food-bowl"
    },
    {
      "name": "freebooks",
      "url": "https://api.github.com/repos/RubenTipparach/freebooks"
    },
    {
      "name": "FruitNinjaCardboard",
      "url": "https://api.github.com/repos/RubenTipparach/FruitNinjaCardboard"
    },
    {
      "name": "gis-hackathon-toolbag",
      "url": "https://api.github.com/repos/RubenTipparach/gis-hackathon-toolbag"
    },
    {
      "name": "GrandTheftStarfighter",
      "url": "https://api.github.com/repos/RubenTipparach/GrandTheftStarfighter"
    },
    {
      "name": "heavyphoton.github.io",
      "url": "https://api.github.com/repos/RubenTipparach/heavyphoton.github.io"
    },
    {
      "name": "hex-game-template",
      "url": "https://api.github.com/repos/RubenTipparach/hex-game-template"
    }
  ]
}
```
