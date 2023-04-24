# Welcome to the Readable language.

Created in Spring 2023 by Tommy Lariccia at The Westminster Schools of Atlanta, GA.

# Overview

The Readable language is almost identical stylistically to Python. It is dynamically-typed and features readable 
keywords ("and" over "&&") as well as useful macros like foreach loops, lambda statements, and argument unpacking. 
Readable is designed to support a simple-yet-modern programming style; i.e. it features recursion, but not OOP, and 
functions-as-objects, but not asynchronity. Whitespace and vertical line shifts are used over brackets on principle.

The Cross-Type Operator Specification is found [here.](https://docs.google.com/spreadsheets/d/16pL6AbSjgbPTst_2zs8T8gJsx3temBiaAq3BABHyDWU/edit?usp=sharing)

# Blocks and New Statements

New statements are deliminated by the newline ("\n") character. Every line in a single block (i.e. in functions, loops, 
and conditionals) begins with four spaces _more_ than the previous block.
# Comments
Single-line comments are marked out by "//". Double line comments use /* and */. For instance:

`// This is a single line comment`

```
/* This is
A Double
Line Comment */
```

# Blocks and New Statements

All errors (include syntax errors) close the program immediately. 

# Variables

Variable names permit characters a-z, A-Z, and '_' anywhere in the string. The digits 0-9 are also allowed in a variable name,
but not as the first character. Variables cannot be a language keyword like "true" (case-sensitive -- they can be "True"). 
Variables can only be initialized; they cannot be declared. Initializations and reassignments of variables are 
(syntactically) identical; for example:

```
x = 2
x = "test"
```

# Types 

There are four primitive types (integers, floats, strings, boolean) and one collection-type (dynamic array), technically
primitive as there are no objects. Example declarations and other information:

| Type    | Example Value(s) | Notes                                   |
|---------|------------------|-----------------------------------------|
| Integer | 7, 0, -1         | Range from -2^31 to (2^31)-1.           |
| String  | 'Alan, '         | Cannot use ", only '. Single-line only. |
| Boolean | true, false      | Only true or false.                     |
| Floats  | 2.0, 1.73, -2.2  |                                         |

Functions (and built-in functions) are also treated like objects or primitives by the language. 

# Operators
The following are the supported mathematical operators (excludes assignment operator and "range" operator). Note 
that operators are calling built-in functions. 

Readable's order of operations is equivalent to math's. Parenthesis are supported.

Note integers and floats may be used with each other, but all integers will be converted to floats.

| Operator   | Supported Types                             | Example Code                | Nary  | Notes                                                              |
|------------|---------------------------------------------|-----------------------------|-------|--------------------------------------------------------------------|
| Add ("+")  | Integers, Floats, Strings, Arrays           | 1 + 2, 'cat' + 'dog'        | N-ary | Adding strings is concatenation.                                   |
| Mult ("*") | Integers, Floats, Booleans, Strings, Arrays | 2 * 3, 4 * 4.0, -7 * baddum | N-ary | See the Cross-Type Operator Specification above.                   |
| Div ("/")  | Integers, Floats                            | 3 / 2, 3.1 / 2              | N-ary | Integer division converts to floats first--so no integer division. |
| Sub ("-")  | Integers, Floats, Arrays, Strings           | 2 - var                     | N-ary | See the Cross-Type Operator Specification above.                   |
| Neg ("-")  | Integers, Floats                            | -1, -cat                    | Unary |                                                                    |

# Comparators

The following are the supported comparative operators. Equality checks are allowed for any same types (excepting int and 
float, which can be compared, and arrays, which cannot be compared with themselves). Errors will be thrown when 
comparing unlike types. All other non-equivalency comparators are restricted to integers, strings, and floats.

| Operator                        | Example Code                                           |
|---------------------------------|--------------------------------------------------------|
| Equality ("==")                 | 2 == 3 // false, 2 == 2.0 // true, 2 == "cat" // error |
| Inequality ("!=")               | 2 != 3 // true, 2 != 2.0 // false, 2 != "cat" // error |
| Greater than (">")              | 2 > 3 // false, 2 > "cat" // error                     |
| Less than ("<")                 | 2 < 3 // true,  2 > "cat" // error                     |
| Greater than or equal to (">=") | 2 >= 3 // false, 2 >= "cat" // error                   |
| Less than or equal to ("<=")    | 2 <= 3 // true,  2 <= "cat" // error                   |

# Arrays

Initializations/assignments of arrays are written like this:

`arr = [1, 'cat', true]`


Access to the index of an array an be done as follows. Indices start at zero. Errors may be raised if the provided indices
are out of bounds.

`a = arr[3]`

Negative indices are supported:

```
a = [1, 2, 3]
print(a[2] == a[-1])    // true
```

Array re-assignment at specific indices is similar:

`arr[1] = 5.0`

# Functions

Function definitions begin with the keyword "func" and are followed by the function identifier with parentheses and 
parameters inside.

A function's local scope cannot see variables in the global scope, except variables referencing other functions. 

Functions may return `null`, explicitly or implicitly (via no return statement). Functions be n-nary if their only 
parameter is prefixed by the `*` symbol. That parameter will have an array as its value type. 

Function calls can unpack an array into many arguments by, similarly, prefacing the array with the `*` symbol.  

A function definition is written like this:

```
func my_function(a, b):
    return a + b
```

Functions (including lambda functions) are called like this:
```
a = foo(2, 3)
```

# Lambda Functions

Lamdba functions are supported, and are modeled off of Javascript's syntax. They do not have return statements, but 
rather have a single expression which is evaluated within the scope and returned.

`my_func = () => print('cat')`

This would simply print "cat" when called, and return null. 

A lambda function with parameters looks like this:

`my_func = (num1, num2) => num1 + num2`

Calling `my_func(2, 3)` would return 5. 

# Looping

There are two possible loops: The while loop and the foreach loop. 

The while loop is written like this:

```
num = 1
while num < 3:
    print(num)
    num = num + 1
```

The foreach loop requires a variable for each value to be carried by, as well as an array, integer, or "range" to 
iterate through.

The following would print out all numbers one to ten. Note that number is a newly defined variable.  (Note foreach
can also iterate over strings, treating them as single-character string arrays.)

```
foreach number in [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]:
    print(number)
```

The following would print out all numbers zero to nine. All integers n iterated through in a foreach loop are treated as
an array of all numbers 0 to n-1, inclusive.

```
foreach number in 10:
    print(number)
```

Ranges are expressed similar as [number]..[number], e.g. `2..4`, inclusive on the left operand and exclusive on the 
right, allowing the larger number in front. For example, the following would print all numbers 10 to 1, decreasing,
and inclusive:

```
foreach number in 10..0:
    print(number)
```

Expressions can also be inserted as the operands to the range operator.

# Conditionals

Conditionals take accept any type (except functional types) as expressions, as objects are evaluated for "truthiness". 
The if-statement is supported, along with "else if" and "else" blocks. The else statement does not take a condition. 
For instance:

```
if option == 1:  // is option 1
    print("Option 1")
else if option == 2:  // is option 2
    print("Option 2")
else:  // option is any other number
    print("A different option.")
```

The operators 'and', 'or', and not (expressed as '!') are all supported. 

# Built-in Functions

The following are built-in functions. The functions 'sum' and 'multiply' are n-ary. The functions 'divide', 'AND', 'OR'
and 'subtract' are binary. The rest take only one argument. 

| Function | Output Type                      | Explanation                                                |
|----------|----------------------------------|------------------------------------------------------------|
| print    | null                             | Prints data, with newline                                  |
| type     | string                           | Returns type of the object provided; type(2) returns "int" |
| len      | integer                          | Returns length of array or string                          |
| truthy   | boolean                          | Returns the "truthiness" of a value.                       |
| sum      | integer, float, array, or string |                                                            |
| subtract | integer, string, array or float  |                                                            |
| divide   | integer or float                 |                                                            |
| multiply | integer, string, array or float  |                                                            |
| AND      | boolean                          |                                                            |
| OR       | boolean                          |                                                            |
| NOT      | boolean                          |                                                            |


# Keywords and Operators

The following keywords and operators are taken.

| Keyword/Operator | Explanation                                                 |
|------------------|-------------------------------------------------------------|
| int              | Initialization of integer type                              |
| float            | Initialization of float type                                |
| bool             | Initialization of boolean type                              |
| str              | Initialization of string type                               |
| null             | The null-type singleton.                                    |
| =                | Assignment Operator                                         |
| +                | Addition Operator                                           |
| -                | Subtraction Operator or Negation Operator (when unary)      |
| *                | Multiplication Operator                                     |
| /                | Division Operator                                           |
| ==               | Equal comparator                                            |
| !=               | Equal comparator                                            |
| >                | Greater than comparator                                     |
| <                | Less than comparator                                        |
| >=               | Greater than or equal to comparator                         |
| <=               | Less than or equal to comparator                            |
| func             | Begins function definition                                  |
| lambda           | Begins lambda function definition                           |
| return           | returns data in a function                                  |
| while            | Begins while loops                                          |
| foreach          | Begins foreach loops                                        |
| in               | Use in separation of variable and iterable in foreach loops |
| if               | Use to begin if and else if blocks                          |
| else             | Use to begin else and else if blocks                        |
| and              | Both operands evaluate to `true`.                           |
| or               | One or both operands evaluate to `true`.                    |
| !                | `true` becomes false, and vice versa.                       |
| ..               | Range binary operator.                                      |
