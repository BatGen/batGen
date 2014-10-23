BatGen - A simple yet powerful code generator for MyBatis.

See https://github.com/BatGen/batGen/wiki for more information.

1.0.0-RC1 Initial Release Candidate


## HOW TO USE:
```
    //takes 3 parameters, location of source files, package location, and database type.
    BatGen batGen = new BatGen( "src/test/resources", "org.batgen.sample", DatabaseType.H2);
    batGen.run();
```

EXAMPLE SOURCE FILE: (employee.txt)
```
    [Settings]
    PACKAGE org.batgen.sample
    CLASS       Employee
    
    [Fields]
    
    LONG(10)        employeeKey     EMPLOYEE_KEY!
    DOUBLE(10,2)    salary       // Salary of employee
    STRING(132)    lastName*      // Last Name
    STRING(132)    firstName     // First Name
    LONG(10)    supervisorKey   SUPERVISOR_KEY?
```

SPECIAL SYMBOLS:
```
* the field is required, can not be null
- SequenceDisabled
! the field is a primary key
? the field is a search Id, can be used to search for list of objects with this Id
// allow comments to be inserted after
```
