# Google Results REST API Implemented in AKKA-HTTP
This application exposes an API to retrieve a JSON-formatted search result based on a supplied query string. This is achieved through making a HTTP request to Google for the supplied query and then parsing the outgoing HTML with the JSoup parsing framework.

## To run locally
This application is currently configured to run on port 8001. To run this application you need SBT installed, and then when within the directory of the repository enter the following command:
```sbt run```

## To test locally
To test this application an installation of sbt is also required. From the shell you then will run the following command:
```sbt test```

## To access
This application is deployed with Heroku. Since the API only supports GET requests you can hit it directly in the browser at the documented endpoints through
the following address:
```https://fierce-oasis-45476.herokuapp.com```

*(e.g ```https://thawing-badlands-55815.herokuapp.com/query/hello%20world``` )*

*Note: Due to the way in which Heroku availability zones work, there's a limited guarantee that the results
the heroku server returns will reflect what you would get back hitting the requestUrl through your browser*

## API

### GET /query/:q

Takes in a string query which is then used for a Google search.

Responds with a JSON document with the URI associated with the retrieved search result, a long with a title.

Example response:

```json
{
  "uri": "http://example.com/",
  "title": "Welcome to Example.com!",
  "description": "Example.com contains all things relevant to Examples".
  "requestUrl": "http://google.co.uk/search?q=example"
}
```
Error codes:
- 503 ServiceUnavailable when Google is returning a non-200 response to the search request, or when the formatting of its search result page has changed
