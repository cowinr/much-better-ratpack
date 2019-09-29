Code used to implement the API defined for [Much Better 'interview'](https://github.com/shanmuha/interviewer), 
reproduced below:

## The task
The task is to create an backend application that provides a set of APIs to be called from a frontend UI.

## The API
The API will have 4 endpoints and will communicate JSON with at least the following functionality for each end point:

- `/login` - a POST request that will accept no input and return a token (which need to be used in subsequent calls to the API, in the Authorization header). Every call to /login will return a new token and every invocation to this endpoint creates a new user, gives them a preset balance in a preset currency.
- `/balance` - a GET request that will accept an Authorization header (with the token value output from /login) and will return the current balance along with the currency code.
- `/transactions` - a GET request that will accept an Authorization header (with the token value output from /login) and will return a list of transactions done by the user with atleast the date, description, amount, currency for each transaction.
- `/spend` - a POST request that will accept an Authorization header (with the token value output from /login), JSON content representing one spend transaction with the transaction date, description, amount, currency.


## Technology stack
- use Ratpack as Java server side framework and Redis as the datastore.
- the backend will run as a standalone java process + the in-memory datastore process

## Requirements
- Implement the 4 API endpoints
- Feel free to spend as much or as little time on the exercise as you like
- Feel free to use whatever additional frameworks / libraries / packages you like
- Your code should be in a state that you would feel comfortable releasing to production
- The endpoints for the same token might be called from multiple clients concurrently.
- Writing unit/integration tests are optional but highly encouraged
- Dockerising the two components is optional and encouraged