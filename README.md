# Engine By Starling Tech task.

## Summary:

To create a simple web application that allows users to round up their transactions within
a week and transfer that money to a savings goal. Users can call the /roundup endpoint to execute this task.

## Assumptions/Future improvements:

- Currently, the application does not mark the transactions as rounded up. This can be done by updating the transaction
  in the database.
  So everytime the user calls the /roundup endpoint, the application will round up the transactions and transfer the
  money to the savings goal.
- The date window is set to current date time minus 7 days.
- We assume only the primary account is needed, and we take the currency
  from account, filter by this and use it as our savings account currency.
- Some values such as savings account name & target amount as hard coded, this wouldn't be the case for prod.
- Generic exceptions are used when requests fail, these would need changed, so they don't mask unexpected exceptions.
- Assuming the json fields are set as not null in the db as their presence is required for the task to be executed.

## Requirements:

- Java 17 (https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- Maven (https://maven.apache.org/)
- IDE (not essential, but recommended).

## How to run:

1. Clone the repository
2. Open the project in your IDE
3. Run `mvn clean install` to download the dependencies
4. Add api token to application.properties file
5. Run the project from the main class
6. Open your browser and navigate to `http://localhost:8080/api/roundup` to execute the task