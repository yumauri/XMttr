# XMttr SQL

*Emitter* module for XMttr, perform queries to database.
Hybrid module, while physically it is emitter – it acts like generator too. It's because there is no safe way to create SQL request separately from executing it. Module creates query, then binds data variables to it and executes it. So, functionally, this module is generator and emitter *simultaneously*.

#### Template example

Module reads file template (set in *xmttr.template* property), finds all queries and executes them. Each query is considered as an array of variables, and module executes each query as many times as array size. Each query should start with new line, name ending with **>** sign. Query name should be array in variables.

For example:
```
SQL0>
 
insert into USERS (
	FIRST_NAME,
	LAST_NAME,
	PHONE
) values (?,?,?)
 
SQL1> insert into PETS (PET_NAME, AGE) values (?,?)
```
and variables:
```
; first query will be executed 10 times
SQL0=[10

; parameters for the first query
SQL0.1=/[A-Z][a-z]{5,10}
SQL0.2=/[A-Z][a-z]{5,10}
SQL0.3=/\\+[1-9][0-9]{10}
 
; second query will be executed from 3 to 5 times
SQL1=[3,5
 
; parameters for the second query
SQL1.1=/[A-Z][a-z]{5,10}
SQL1.2|int=/([12][0-9]|[0-9])
```
In order to bind variables with correct data type, it is possible to set variables types. You can see it in the last line above.
By default variables considered as strings. For now, following types are supported:

 * string
 * int
 * boolean
 * date
 * double

You should separate type from name using **|** sign or **!** sign. Parameters for type should be separated from type using same signs (one of them, not matter which one). For date type you can set format (should be compatible with *java.text.SimpleDateFormat*):
```
SQL2.2|date!MM.dd.YYYY=/(0[1-9]|1[012])/([0][1-9]|[12][0-9]|3[01])/19[5-8][0-9]
```

#### Properties

| property | description | default value |
|----------|-------------|---------------|
|xmttr.emitter.jdbc.driver|JDBC driver| |
|xmttr.emitter.jdbc.url|database URL| |
|xmttr.emitter.jdbc.user|login| |
|xmttr.emitter.jdbc.password|password| |
|xmttr.emitter.jdbc.simulate|*true* – don't execute queries,<br> *false* – execute queries|false|
