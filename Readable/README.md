# Welcome to the Readable language.

Created in Spring 2023 by Tommy Lariccia at The Westminster Schools of Atlanta, GA.

# Overview

The Readable language is intended to be extremely easy to code in and, more importantly, read code in. Like python,
it uses whitespace instead of brackets, as well as English-language operators (e.g. "or") where useful; however, 
Readable does not support the return statement everywhere in a function (the last value is returned, 
ensuring one-input-one-output), and allows for optional type enforcement. 

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

# Variables

Variables support character a-z, A-Z, and '_'. Variables cannot be a language keyword like "true" (case-sensitive, 
they can be "True"). Variables cannot be declared; they can only be initialized. Initialization can be done either 
a) dynamically, or b) statically. A dynamic variable will always be dynamic, and a static variable will be locked to 
its type.

The initialization and reassignment of a dynamic variable are the same; for example:

```python
x = 2
x = 'test'
```

The initialization and reassignment of a static variable is likewise the same:

```python
int x = 2
int x = 'test'
```

Reassignment requires the type so as to force the programmer and reader to be aware of the type require (in an otherwise 
dynamic language). 

A static variable can not be set equal to the value contained in a dynamic variable, or the value returned by a normal
function.
# Types 

There are four primitive types (integers, floats, strings, boolean) and one collection-type (dynamic array), technically
primitive as there are no objects. Example declarations and other information:

| Type    | Static Declaration  | Example Value(s) | Notes                                   |
|---------|---------------------|------------------|-----------------------------------------|
| Integer | int a = 7           | 7, 0, -1         | Range from -2^31 to (2^31)-1.           |
| String  | str person = 'alan' | 'Alan, '         | Cannot use ", only '. Single-line only. |
| Boolean | bool on_off = true  | true, false      | Only true or false.                     |
| Floats  | float cost = 3.2    | 2.0, 1.73, -2.2  |                                         |
# Operators
The following are the supported mathematical operators (excludes assignment operator). Note that operators are calling 
built-in functions, so it's possible to, for example, add a static and dynamic variable and assign the sum to a 
_dynamic_ variable; however, if an output is assigned to a static variables, all parameters must also be static and 
of the same type.

Order of operations is equivalent to math. Parenthesis are supported.

Note integers and floats may be used with each other, but all integers will be converted to floats.

| Operator   | Types Defined To          | Example Code                | Nary   | Notes                                                              |
|------------|---------------------------|-----------------------------|--------|--------------------------------------------------------------------|
| Add ("+")  | Integers, Floats, Strings | 1 + 2, 'cat' + 'dog'        | N-ary  | Adding strings is concatenation.                                   |
| Mult ("*") | Integers, Floats          | 2 * 3, 4 * 4.0, -7 * baddum | N-ary  |                                                                    |
| Div ("/")  | Integers, Floats          | 3 / 2, 3.1 / 2              | Binary | Integer division converts to floats first--so no integer division. |
| Sub ("-")  | Integers, Floats          | 2 - var                     | N-ary  |                                                                    |
| Neg ("-")  | Integers, Floats          | -1, -cat                    | Unary  |                                                                    |

# Comparators

The following are the supported operators. Equality checks are allowed for any same types (excepting int and float); 
i.e. they will return false if comparing unlike types. All other comparators are restricted to integers and floats.

| Operator           | Example Code                                           |
|--------------------|--------------------------------------------------------|
| Equality ("==")    | 2 == 3 // false, 2 == 2.0 // true, 2 == "cat" // false |
| Greater than (">") | 2 > 3 // false, 2 > "cat" // error                     |
| Less than ("<")    | 2 < 3 // true,  2 > "cat" // error                     |

# Arrays

All arrays are dynamic arrays; arrays may be dynamically or statically typed. 

Dynamic initializations/assignments are as so:

`arr = [1, 'cat', true]`

Static initializations/assignments are similar to Java (per above, initializations and assignments are identical):

```
int[] arr = [1, 2, 3]
int num = 2
int[] arr = [1, num, 3]  // Works
num2 = 3  // Note: Dynamically typed
int[] arr = [1, num, num3]  // Breaks
```

Access to the index of an array, for static or dynamic, can be done as follows. Indices start at zero. IndexOutOfBound 
errors may be raised.

`a = arr[3]`

Dynamic array assignment (at specific indices) is similar:

`arr[3] = 5.0`

Static array assignments must acknowledge type and use static variables, as above:

```
int a = 4
int[] arr[4] = a
```

# Functions

Functions begin with the keyword "func", followed by an optional type (for "static" functions, which must have only 
static parameters), following by the name with parentheses and parameters inside. Parameters can be static and are 
optional. Dynamic functions may have one or more static parameters; static functions must have only static parameters. 

The last line (and only the last line) must be a return statement.

A dynamic function is as follows:

```python

func my_function(param1, int param2):
    param1 = param2
    return param1
    
```

A static function is as follows:

```python

func int my_function(int param1, int param2):
    return param1 + param2
    
```

Functions (including lambda functions) may be called as follows. Note type-acknowledgement is required in static 
functions:

`a = my_func(parem1, parem2)`

# Lambda Functions

Lamdba functions must be assigned to _dynamic_ variables. They are modeled off of Javascript's syntax, however, they may
only support one line, which must be an evaluate-able to a value (i.e. no return statement). An example of a 
parameter-free lambda function is as follows:

`my_func = () => print('cat')`

This would simply print "cat" when called.

Parameters must only be dynamic. A lambda function with parameters looks like this:

`my_func = (num1, num2) => num1 + num2`

# Looping

There are two accepted loops: The while loop and the foreach loop. 

The while loop is written like this:

```python
num = 1
while num < 3:
    print(num)
    num = num + 1
```

The foreach loop requires a variable for each value to be carried by, as well as an array or integer to iterate through.

The following would print out all numbers one to ten. Note that number is a newly defined variable.

```python
foreach number in [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]:
    print(number)
```

The following would print out all numbers zero to nine. All integers n iterated through in a foreach loop are treated as
an array of all numbers 0 to n-1, inclusive.

```python
foreach number in 10:
    print(number)
```

# Conditionals

Conditionals take only booleans or expressions evaluating booleans ("if 2" is meaningless and invalid). The if-statement
is supported, along with "else if" and "else" blocks. The else statement does not take a condition. For instance:

```python
if option == 1:  // is option 1
    print("Option 1")
else if option == 2:  // is option 2
    print("Option 2")
else:  // option is any other number
    print("A different option.")
```

# Built-in Functions

The following are built-in functions. Note all are dynamic but enforce typing through the "type" function; the "type"
function does not require this, as it is written in Java. Also note all seem to support multiple arguments. This is not
supported for user-defined functions, rather, it is an interpreter-trick (e.g. add(1, 2, 3) becomes add(1, add(2, 3))). 

| Function | Output Type                                              | Explanation                                                |
|----------|----------------------------------------------------------|------------------------------------------------------------|
| print    | No output (Null does not exist; error is instead thrown) | Prints data                                                |
| type     | string                                                   | Returns type of the object provided; type(2) returns "int" |
| add      | integer, float, or string                                |                                                            |
| sub      | integer or float                                         |                                                            |
| div      | integer or float                                         |                                                            |
| mult     | integer or float                                         |                                                            |
| neg      | integer or float                                         |                                                            |

# Keywords and Operators

The following keywords and operators are taken.

| Keyword/Operator | Explanation                                                 |
|------------------|-------------------------------------------------------------|
| int              | Initialization of integer type                              |
| float            | Initialization of float type                                |
| bool             | Initialization of boolean type                              |
| str              | Initialization of string type                               |
| =                | Assignment Operator                                         |
| +                | Addition Operator                                           |
| -                | Subtraction Operator or Negation Operator (when unary)      |
| *                | Multiplication Operator                                     |
| /                | Division Operator                                           |
| ==               | Equal comparator                                            |
| >                | Greater than comparator                                     |
| <                | Less than comparator                                        |
| func             | Begins function definition                                  |
| return           | returns data in a function                                  |
| while            | Begins while loops                                          |
| foreach          | Begins foreach loops                                        |
| in               | Use in separation of variable and iterable in foreach loops |
| if               | Use to begin if and else if blocks                          |
| else             | Use to begin else and else if blocks                        |
