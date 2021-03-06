rstl - The Resource Oriented Template Language
==============================================

Welcome to the Resource Oriented Template Language (rstl pronounced \'rest-əl\) project. This template language, implemented in Java allows a web developer to build an extensible, layout based web site in a language agnostic manner. The syntax and philosophy of the language is borrowed from the Django Template Language.

Requirements
------------

RSTL will run with any version of Java runtime greater than version 1.5.

Template language syntax
------------------------

### Template variables

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

### Template tags

#### _extends_
The _extends_ statement declares that the template inherits content from the parent specified in the
statement. The _extends_ statement if specified in a template must necessarily be the first statement in
the template. In the snippet below, the containing template inherits from a template name
"fragments/213liquidlayout.html".

```django
{% extends "fragments/213liquidlayout.html"%}
```

#### _include_
The _include_ statement declares that template should include rendered content from the specified
template. Note, that this does not mean that the template content is substituted in to the current
template . Rather, the template will invoke the included template with context provided to the current
template. The following snippet includes content from a template "fragments/includejs.html".

```django
{% include "fragments/includejs.html" %}
```

#### _block_
The _block_ statement specifies the beginning of a named block. A named block is a place holder in the
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

#### _endblock_
The _endblock_ statement specifies the end of a named block. The name of the block that it terminates is
an optional attribute of the statement.

```django
{% endblock main %}
```

#### _for_
The _for_ statement is one of the control statements in the template language that iterates over the
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

#### _endfor_
The _endfor_ statement terminates a for loop as shown below.

```django
{%endfor%}
```

#### _if_
The _if_ statement is a control construct in the template language that allows for conditional rendering
of a portion of the template. The following snippet renders content based on the presence of a context
variable. If the context variable is a boolean variable, then the conditional clause evaluates if the
variable evaluates to true.

```django
{%if product.item%}
This product is an item.
{%endif%}
```

#### _else_
The _else_ statement is an optional construct within a if statement block

```django
{%if product.item%}
This product is an item.
{%else%}
This product is not an item.
{%endif%}
```

#### _endif_
The _endif_ statement terminates an if and/or optionally an else statement.

```django
{%endif%}
```

#### _resourcegroup_*
The _resourcegroup_ statement is a named block construct that allows for zero or more resource
statements. It may be inherited and overridden exactly like a block statement, except that it may only
contain XHTML variant of resource statements. A resource group is used as a place holder and is
primarily used as a construct to aid in easy (tooled) manipulation of resources within the group.

```django
{%resourcegroup centerspot%}
{%resource.xhtml /resources/marketing/springsavings%}
{%resource.xhtml /resources/marketing/bogo%}
{%endresourcegroup%}
```

#### _endresourcegroup_*
The endresourcegroup statement terminates a resourcegroup block and may optionally contain the
name of the group that it terminates.

```django
{%endresourcegroup centerspot%}
```

#### _resource_*
The _resource_ statement allows the template to reference RESTful resources identified by their URI.
The URI may contain variable references to context variables with single curly braces as shown in the
examples below which are resolved by the template engine prior to fetching the resource
representations. There are two variants of the resource statement.
The statement resource.xhtml renders an XHTML representation of the identified resource. This
variant may take an optional widget name which the template engine will bind to the representation.
This variant of the resource statement is primarily used to render the representation along with the
template output, usually within a espot statement block.

```django
{%resource.xhtml /resources/marketing/{categoryid}/featuredproducts with scrollablepane%}
{%resource.xhtml /resources/marketing/bogo%}
```

The statement resource.json binds the JSON representation of the resource to the context using the
provided context name. The template may then reference attributes of the resource. It is recommended
that such references to resources be made outside control constructs so that the template engine may
pre-process them prior to rendering the template output.

```django
{%resource.json /resources/product/(productid} as prod%}
{%resource.json /resources/user/{userid} as user%}
```

#### _precondition_*
The _precondition_ statement allows the template engine to choose from a set of possible templates by
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

#### _layout_*
The _layout_ statement is used to specify the layout - the way content is laid out in a page. Layout
statements may be inherited by templates or overriden just like blocks and espots.

```django
{% layout "myfavoritelayout.html"%}
```

Usage
-----

Templates are primarily meant to be rendered by any Java code. To render a
template, you need to specify the location of the templates. This location is used to initialize a
template group as follows where the "templates" directory in the current working directory is the
location of the templates.

```java
TemplateGroup tg = new TemplateGroup("templates");
```

The template context is then set up by the java code. In the example below a list of names is set up in
the template context.
```java
Map<String, Object> initContext = new HashMap<String, Object>();
List<String> names = Arrays.asList("John", "Fred", "Betsy");
initContext.put("names", names);
TemplateContextImpl ctx = new TemplateContextImpl(initContext, tg);
```

If the template to be rendered resides in "stores/default/guest.html" in the "templates" directory used
to initialize the template group above, then the template may be rendered as follows where w is a
Writer object to which the template output will be written to.

```java
tg.render("stores/default/guest.html", ctx, w);
```

In the example below "guest.ctl" inherits from another template "layouts/nolayout.ctl" and renders the
names in a for loop with in a block named "content".

```django
{% extends "layouts/nolayout.html" %}
{% block content%}
<ul>
{%for name in names %}
<li> {{name}} </li>
{%endfor%}
</ul>
{% endblock content %}
```

The template "nolayout.html" defines the original content of the block and where the block should be
rendered as follows.

```django
<html>
  <head>
  </head>
  <body>
	{%block content%}{%endblock%}
  </body>
</html>
```

With the template context as defined above, the template would be rendered as

```html
<html>
  <head>
  </head>
  <body>
	<ul>
 	  <li> John </li>
	  <li> Fred </li>
	  <li> Betsy </li>
	</ul>
  </body>
</html>
```