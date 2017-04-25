## Postgres Bluemix service
A postgres service code example which will run on bluemix environment.  
Currently connected to a Postgres service with 20mb storage (for development)  
Writes and reads from a table with the following schema of 2 columns   
ID (INT - Primary key), MESSAGE (TEXT)


### To get TEST_TABLE data
Use REST GET call on:  
`http://postgres-service-antipestilential-apochromatism.eu-gb.mybluemix.net/test_table`  
Will return a json string with json object with columns as keys and row elements as values.  

### To insert a new record to TEST_TABLE   
Use REST POST call with query parameters:
* id - Integer - The primary key of the table
* message - Text - The data to be store for that record  

`http://postgres-service-antipestilential-apochromatism.eu-gb.mybluemix.net/insert/test_table?id=9&message=my_text`  
