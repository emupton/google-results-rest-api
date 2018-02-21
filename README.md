# Google Results REST API Implemented in AKKA-HTTP
This application exposes an API to retrieve a JSON-formatted search result based on a supplied query string. This is achieved through making a HTTP request to Google for the supplied query and then parsing the outgoing HTML with the JSoup parsing framework.

## To run
This application is currently configured to run on port 8001. To run this application you need SBT installed, and then when within the directory of the repository enter the following command:
```sbt run```
