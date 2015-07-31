BatGen - A simple yet powerful code generator for MyBatis.

See http://batgen.github.io/documentation.html for more information.

1.0.0-RC1 Initial Release Candidate


## HOW TO USE:
```
    //takes 3 parameters, location of source files, package location, and database type.
    BatGen batGen = new BatGen( "src/test/resources", "org.batgen.sample", DatabaseType.ORACLE);
    batGen.run();
```

The tables used are formatted in an acceptable manner for the application to create the generated files. Your files should end in .txt , should match the below format, and should only contain one table. If your tables are not correctly formatted, an error will occur during the implementation of the code generation application. Each file is broken into three sections:

1. The first section is Comments.
   1. Generally the comment section is used to define what the table name is called.
   2. Comments here can be defined by a multi-line comment (starting with /* and ending with */) or a single line comment (//).
2. The second section is Settings.
   1. The keyword Settings must be surrounded by brackets ( [ ] ). The settings contain keywords CLASS, EXTENDS, and LINK.
   2. CLASS <Class name> <Database Name>
   			Specifies the name of the table (java) and the database name (sql) (The database name is not required). Each file must specify a class.
   3. EXTENDS <Superclass name>
   		 	Specifies a class name for the generated domain object to extend. If the superclass name is in a different package than the domain object, specify the full package name and it will be imported also. 
   4. LINK <Class one> <Class two> 
   		 	Allows automatic generation of many-to-many link tables. In addition to everything else specified in this file, the following additions will be automatically generated.
   		 		A field for every primary key in class one.
   		 		A field for every primary key in class two.
   		 		A Bo method, get<Class One>By<Class Two>Key(...) which takes one argument for each primary key in <Class Two>.
   		 		A Bo method, get<Class Two>By<Class One>Key(...) which takes two argument for each primary key in <Class One>.
3. The third section is Fields.
   1. The keyword Fields must be surrounded by brackets ( [ ] ).
   2. The field section contains all the columns related to that table.
   3. The data type is on the left, followed by the name of the column (camelCase), the database alias name (not required),special character (not required), and a comment (not required)
      * The applicable data types are: long, integer, double, string, blob, date, timestamp and boolean.
      * If the data type is an long, integer, or string, then you must also specify a magnitude  (total number of digits).
      * If the data type is an double, then you must also specify a integer part (number of digits to the left of the decimal point) and precision (number of digits to the right of the decimal point).
      * If the data type is an timestamp then you must also specify a precision (precision of the seconds ).
      * If the data type is an vstring, then you must also specify a magnitude (total number of digits) and also a comment section with the sql-statement on how to retrieve it surrounded by `. 
   4. The special characters that are allowed are !, *, ? and -.
      * ! signifies that the column is a primary key to the table. There can be multiple primary keys, but at least one has          to be defined as a LONG(some value) or STRING(some value). If you do not create a key column, one will be                    automatically generated for you.
      * * signifies that the column is required, and cannot be null.
      * ? signifies that the column is a searchable ID, used to get an list of all objects with same the ID.
      * - signifies that the column has it's sequence disabled.
4. The fourth section is Compound Indexes (Optional).
    1. The keyword Indexes must be surrounded by brackets ( [ ] ).
    2. Use to create compound indexes within current table
    3. Requires at least two inputs: the index name, and all the columns names(second column in the txt file) separated by commas.

5. The fifth section is Foreign Keys (Optional).
    1. The keyword ForeignKeys must be surrounded by brackets ( [ ] )
    2. Use to create references to a column from another table
	3. Compound foreign keys are accepted
    4. Follows the following syntax: First the column names in the current table that are referencing fields from other tables. Next the symbol '=>' denotes where the fields will be pointing to. And finally the table that is being referenced followed by the fields being referenced in parenthesis. The fields can be comma separated or space separated. Take a look at the following examples. the following examples.
  		* Example: first, last => Employee(first_name, last_name) 
  		* Example: first last => Employee(first_name last_name)
  		* Example: id => User(id)

EXAMPLE TABLE FILE: (employee.txt)
```
    /**
    * The Employee Table.
    */
    [Settings]
    CLASS       Employee
    
    [Fields]
    
    LONG(10)        employeeKey     EMPLOYEE_KEY!
    DOUBLE(10,2)    salary       // Salary of employee
    STRING(132)    lastName*      // Last Name
    STRING(132)    firstName     // First Name
    LONG(10)    supervisorKey   SUPERVISOR_KEY?
    
    [Indexes]
    name     lastName, firstName

    [ForeignKeys]
    supervisorKey => Supervisor(supervisorKey)
    firstName, lastName => Names(first, last)
```
