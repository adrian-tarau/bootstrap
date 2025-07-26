## Workspace

A typical dashboard to display a set of records (data set) is organized in the following sections:

1. The title of the  dashboard
2. The contextual help for the dashboard
3. The toolbar (available actions)
4. The number of record matching the current filter and number of pages (by default 25 records per page)
5. An optional time range filter, if the dashboard has at least one timestamp field
6. A grid with record matching the current filter

![Workspace](/images/workspace.png)
_Figure 1. Typical view of an application dashboard_

## Fields

A dashboard will display only the most important fields in the data grid (due to limited space available), however, additional fields can be available to be used in search expressions. Most of the time, field names are similar to the label in the table header. and they are case-insensitive: _name_ and _Name_ represents the same field.

Fields can be used to search through the document using a _query_ (search expression) as described bellow.

## Query

Results can be filtered by typing search expression (queries) in the search field present in the header. Multiple expressions can be joined
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

![Result](/images/result.png)
_Figure 1. Typical view of an application dashboard_

Some of the fields in the grid (mostly strings) can be clicked and the search criteria is changed by including another expression using the field corresponding with the column and the value that was clicked. The user can easily drill down and locate the records that he/she needs by creating more restrictive filters. 
