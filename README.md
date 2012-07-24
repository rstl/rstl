rstl - The Resource Oriented Template Language
==============================================

Welcome to the Resource Oriented Template Language (rstl pronounced \'rest-əl\) project. This template language, implemented in Java allows a web developer to build an extensible, layout based web site in a language agnostic manner. The syntax and philosophy of the language is borrowed from the Django Template Language.

Requirements
------------

RSTL will run with any version of Java runtime greater than version 1.5.

Template language syntax
------------------------

### Variables

Variables in the template language are referenced with in double curly braces and cause substitution
of values from the template context that maps to the same name.
For example the following snippet references an attribute named "user" from the context used to
render the template.

```django
{{user}}
```

Variables may reference attributes of the context values using a "." notation. If the variable is a Map
object, then the attribute value is fetched from the Map object. If it is a regular Java object, then it
uses a getter method to retrieve the attribute value. In the snippet below, the "fullname" attribute of a
object referenced as "user" in the context is retrieved if the "user" object is an instance of a Map. 
If the object is not a map, then the getFullname() method of the object is invoked.

```django
{{user.fullname}}
```

Variable values that are lists may be indexed using a numeric attribute specifying the index within the
list. The following snippet references the 10th element in a list of names.*

```django
{{names.10}}
```

Variable values may be filtered using a Unix style pipe filter with predefined or custom filters. The
"name" attribute of a variable named "store" in the context is lower cased using the "lower" filter, the
result of which is then url encoded using the "urlencode" filter in the example below.

```django
{{store.name|lower|urlencode}}
```

The built-in filters supported by the language follow:

* _urlencode_ - Encode the variable value according to URL encoding rules
* _upper_ - Convert the variable value to upper case characters
* _lower_ - Convert the variable value to lower case characters
* _join_ - Join a list of objects into a single string
* _xmlescape_ - Escape the variable value so that any XML elements are escaped according to XML escaping rules

### extends
The extends statement declares that the template inherits content from the parent specified in the
statement. The extends statement if specified in a template must necessarily be the first statement in
the template. In the snippet below, the containing template inherits from a template name
"fragments/213liquidlayout.html".

```django
{% extends "fragments/213liquidlayout.html"%}
```

### include
The include statement declares that template should include rendered content from the specified
template. Note, that this does not mean that the template content is substituted in to the current
template . Rather, the template will invoke the included template with context provided to the current
template. The following snippet includes content from a template "fragments/includejs.html".

```django
{% include "fragments/includejs.html" %}
```

### block
The block statement specifies the beginning of a named block. A named block is a place holder in the
template output that can be overriden by a inherited template. While a block may be nested within
another block, block names and definition are global to the template scope. If a template does not
override the block, the default content specified in the ancestor template will be rendered. The
template engine renders content of the parent first replacing content from blocks that are overridden,
before rendering content in the current template. Only content outside any non-overriding block is
appended to the template output. It is possible to reference the content of a block as defined by the
template's parent using the special variable "block.super". The following snippet shows a block
named main that renders an empty div.

```django
{% block main %}
<div></div>
{% endblock %}
```

### endblock
The endblock statement specifies the end of a named block. The name of the block that it terminates is
an optional attribute of the statement.

```django
{% endblock main %}
```

### for
The for statement is one of the control statements in the template language that iterates over the
contents of a list. There are a few builtin variables that are available with in a for loop. These are

* _forloop.counter_ - the current iteration count through the loop (1-indexed)
* _forloop.counter0_ - the current iteration count through the loop (0-indexed)
* _forloop.revcounter_ - the current iteration in the loop from the end of the list (1-indexed)
* _forloop.revcounter0_ - the current iteration in the loop from the end of the list (0-indexed)
* _forloop.parent_ - the parent of the current forloop in case of nested for loops.

The following snippet iterates through a list (names) set in the context and renders each value (name)
in a loop.

```django
{%for name in names%}
{{forloop.counter}}) {{name}}
{%endfor%}
```

If the context variable names is set to the list ["Roger", "Fran", "Barney"], the snippet above would
produce

```
1) Roger
2) Fran
3) Barney
```

### endfor
The endfor statement terminates a for loop as shown below.

```django
{%endfor%}
```

### if
The if statement is a control construct in the template language that allows for conditional rendering
of a portion of the template. The following snippet renders content based on the presence of a context
variable. If the context variable is a boolean variable, then the conditional clause evaluates if the
variable evaluates to true.

```django
{%if product.item%}
This product is an item.
{%endif%}
```

### else
The else statement is an optional construct within a if statement block

```django
{%if product.item%}
This product is an item.
{%else%}
This product is not an item.
{%endif%}
```

### endif
The endif statement terminates an if and/or optionally an else statement.

```django
{%endif%}
```

### resourcegroup*
The resourcegroup statement is a named block construct that allows for zero or more resource
statements. It may be inherited and overridden exactly like a block statement, except that it may only
contain XHTML variant of resource statements. A resource group is used as a place holder and is
primarily used as a construct to aid in easy (tooled) manipulation of resources within the group.

```django
{%resourcegroup centerspot%}
{%resource.xhtml /wcs/resources/marketing/springsavings%}
{%resource.xhtml /wcs/resources/marketing/bogo%}
{%endresourcegroup%}
```

### endresourcegroup*
The endresourcegroup statement terminates a resourcegroup block and may optionally contain the
name of the group that it terminates.

```django
{%endresourcegroup centerspot%}
```

### resource*
The resource statement allows the template to reference RESTful resources identified by their URI.
The URI may contain variable references to context variables with single curly braces as shown in the
examples below which are resolved by the template engine prior to fetching the resource
representations. There are two variants of the resource statement.
The statement resource.xhtml renders an XHTML representation of the identified resource. This
variant may take an optional widget name which the template engine will bind to the representation.
This variant of the resource statement is primarily used to render the representation along with the
template output, usually within a espot statement block.

```django
{%resource.xhtml /wcs/resources/marketing/{categoryid}/featuredproducts with scrollablepane%}
{%resource.xhtml /wcs/resources/marketing/bogo%}
```

The statement resource.json binds the JSON representation of the resource to the context using the
provided context name. The template may then reference attributes of the resource. It is recommended
that such references to resources be made outside control constructs so that the template engine may
pre-process them prior to rendering the template output.

```django
{%resource.json /wcs/resources/product/(productid} as prod%}
{%resource.json /wcs/resources/user/{userid} as user%}
```

### precondition*
The precondition statement allows the template engine to choose from a set of possible templates by
matching a set of built-in or custom preconditions. When more than one precondition is specified, all
of them have to match for the template to be selected. The set of built-in preconditions are :

* _precondition.authorized_ - Refers to a request from a user that has been authenticated
* _precondition.google_ - The request is from a google search result
* _precondition.search_ - The request is from a local search result
* _precondition.day_ - The request is made on a particular day
* _precondition.tod_ - The request is made on a particular time of day
* _precondition.weekend_ - The request is made on a weekend
* _precondition.yahoo_ - The request is from a yahoo search result

```django
{%precondition.authorized%}
{%precondition.google%}
```

### layout*
The layout statement is used to specify the layout - the way content is laid out in a page. Layout
statements may be inherited by templates or overriden just like blocks and espots.

```django
{% layout "myfavoritelayout.html"%}
```