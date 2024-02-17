## Workspace

## Fields

Dashboard will display only the most important fields in the grid (due to limited space available, however, additional fields can be available to be used in search expressions.

Most of the time, field names are similar to the label in the table header. and they are case-insensitive: _name_ and _Name_ represents the same field.

Fields can be used to search through the document using a _query_ (search expression) as described bellow.

## Query

Results can be filtered by typing search expression (queries) in a in the search fields. Multiple expressions can be joined
together using <b>AND</b> or <b>OR</b> to form complex expressions. 

Each dashboard has a list of available fields (displayed in the tooltip of the search fields for each dashboard) to be used to create expressions.

The following comparison operators can be used with fields:
* `=`: The field is equal to the expression value (use single or double quotes for strings with whitespaces)
* `<>`: The field is equal to the expression value (use single or double quotes for strings with whitespaces) 
* `<`: The field is less than the expression value 
* `<=`: The field is less or equal than the expression value 
* `>`: The field is greater than the expression value 
* `>=`: The field is greater or equal than the expression value 
* `between`: The field is between two numerical values
* `like`: The field matches a SQL like expression, where _*_ matches any characters and _?_ matches a single character 
* `regex`: The field matches a regular expression (case-insensitive)
* `contains`: The field contains (case-insensitive) a given string 
* `in`: The field has one of the given values (use single or double quotes for strings with whitespaces)
* `not in`: The field does not have one of the given values (use single or double quotes for strings with whitespaces)
* `null`: The field is _null_
* `not null`: the field is _not null_ 

## Results

By default, when a dashboards is displayed, the query is restricted to the current day (if a timestamp is available in the dashboard) and there is no search expression. 