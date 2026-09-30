WOOgnl
======
Lets WOD bindings use [OGNL](https://github.com/orphan-oss/ognl)
expressions, and adds helper functions for formatting values in
bindings.

Configuration
-------------
WOOgnl installs its template parser when `ognl.active` is `true`,
which is the default. Set `ognl.inlineBindings=true` to allow inline
bindings in templates. `ognl.parserClassName` selects a different
template parser.

OGNL bindings
-------------
A binding value that starts with `~` is evaluated as OGNL. These
examples come from WOOgnl's original announcement:

```
// Calling static methods or reading static fields
String1: WOString {
	value = "~@ognl.webobjects.WOOgnl@OgnlSpecialCharacters";
}

// Conditionals: each previous value of the "." is pushed into #this
String2: WOString {
	value = "~name.length().(#this > 100? 2*#this : 20+#this)";
}

// String concatenation
String3: WOString {
	value = "~\"Hello Max \" + name";
}

// The "in" operator, which also works with NSArray and NSSet
String4: WOString {
	value = "~name in {\"Main\", \"Something\"} ? \"Yes\" : \"No\"";
}

// Variables; commas allow several actions in one expression
String5: WOString {
	value = "~#A=new com.webobjects.foundation.NSMutableArray(), #A.addObject(name), #A.addObjectsFromArray(session.languages), #A";
}
```

Helper functions
----------------
You often want to format a binding's value, but only some components
accept a formatter. You could add a method like `displayName()` to your
model, but that puts view code in the model. You could add it to a
component, but then it isn't reusable, and a component that renders
the value can't pass it to another binding.

Helper functions, like Rails' helpers, are reusable formatting methods
for bindings. For example, to give `Person` a common display name:

1. Create a class like this, in any package:

   ```java
   public class PersonHelper {
   	public String displayName(Person person) {
   		return person.firstName() + " " + person.lastName();
   	}
   }
   ```

2. Use it in a WOD file, with a pipe between the value and the helper
   function:

   ```
   PersonName : WOString {
   	value = currentPerson|displayName;
   }
   ```

You can write a `StringHelper`, `BooleanHelper` and so on in the same
way. To show booleans as "yes" or "no":

```java
public class BooleanHelper {
	public String yesNo(Boolean value) {
		return value != null && value.booleanValue() ? "yes" : "no";
	}
}
```

```
RandomValue : WOString {
	value = currentPerson.isAdmin|yesNo;
}
```

These examples use `WOString`, which accepts a formatter anyway.
Helper functions also work where a formatter can't:

```
HeaderFooter : HeaderFooterWrapper {
	title = currentPerson|displayName;
}
```

A helper class can define as many helper methods as you like.

### How it works

When a WOD file is parsed, each helper function binding is replaced
with a longer OGNL expression that resolves it, so it runs on the same
OGNL machinery as `~` bindings.

The helper class is chosen by the type of the value before the pipe.
If `getClass().getName()` returns `com.mdimension.Person`, WOOgnl
looks for a `PersonHelper` class, using the same bundle lookup as
components.

### Parameters

Parameters work too, because helper functions become OGNL calls. For
example, a truncating helper:

```java
public class StringHelper {
	public String truncate(String value, int atIndex) {
		String truncatedValue = value;
		if (value != null && value.length() > atIndex) {
			truncatedValue = value.substring(0, atIndex) + " ...";
		}
		return truncatedValue;
	}
}
```

```
HeaderFooter : HeaderFooterWrapper {
	title = pageMetadata.description|truncate(10);
}
```

You aren't limited to one parameter.

### Remapping helper instances

To give a type a custom helper class, register an instance for it. For
example, to make the whole app use `MyPersonHelper` for `Person`:

```java
WOHelperFunctionRegistry.setHelperInstanceForClassInFrameworkNamed(new MyPersonHelper(), Person.class, "app");
```

### Per-framework helper functions

Unqualified helper functions resolve in the "app" namespace. A
framework that registers its own helpers under its own name avoids
replacing the app's, for instance the top-level `StringHelper`:

```java
WOHelperFunctionRegistry.setHelperInstanceForClassInFrameworkNamed(new AjaxPersonHelper(), String.class, "Ajax");
```

To call a qualified helper function, prefix it with the framework
name:

```
HeaderFooter : HeaderFooterWrapper {
	title = currentPerson|Ajax.displayName;
}
```

### Subclassing helper instances

Helper instances can be subclassed. If your core framework has a
`StringHelper`, you can write `MyProjectStringHelper extends
StringHelper` and register it as the "app" helper.

### WOLips

In WOLips, go to Preferences > WOLips > WOD Editor and add the pipe,
open parenthesis and close parenthesis to the list of valid WOD
binding characters.
