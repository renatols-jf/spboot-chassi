# spboot-chassis

# Changelist

## 0.1.0 (RELEASE CANDIDATE 2)
- Implemented distributed tracing 
- Enhanced `@Inject` behavior
- Added `ApiCall` methods that accept a `Media`
- Made it possible to attach `Context` information to an `ApiCall` created through `chassis-api.yaml`
- Added `@AsTimedOperation` annotation

## 0.0.11
- Added `isBodyAvailable` to `ApiResponse`

## 0.0.10
- Fixed a bug related to API loading

## 0.0.9
- Added config for displaying application health as an average of the operations
  or as the current lowest operation health.
- Enabled the usage of environment variables in the various chassis.yaml files.
- Replaced `group` label integration identification with `provider`.
- Deprecated RestOperation
- Created ApiCall as an alternative to RestOperation.

## 0.0.8
- Added labels for application name and instance id. Application name is exported to the logs and metrics,
  and instance id to the metrics. Application name will only be exported if a value is found 
  in `chassis-labels.yaml`.
- Enabled configuration changes through `chassis-config.yaml` file.  
- Added `@Inject` annotation to inject / automatically initialize spring beans inside a `Request`

## 0.0.7
- Fixed labels loading which were not working from inside jar

## 0.0.6
- Changed TimedOperationException, which will only be thrown for `TimedOperation#execute` when a
non runtime exception happens.

## 0.0.5
- Added i18n support for labels

## 0.0.4
- Added support for labels
- Added health metric support for integrations

## 0.0.3
- Enabled `FieldRenderable` to render nested `Renderable` objects and collections.
- Changed `@Minimum` ignore threshold to `Integer.MIN_VALUE`.
- Replaced `TimedOperation.Executable` with `java.concurrent.Callable`
- Renamed `@Minimum` to `@Min` and added `@Max` validation
- Added support for Custom Validators on `@Validation`

# Discalimer
This project was and is being developed in my free time. This means I'll do my 
best to solve any issues, but a few issues are to be expected. This also means there
is a lot to be done, and a lot that could be better. That being said, 
this is currently in use in a handful of professional microservices without 
any issues. I'll continue to use it as it evolves, and I don't expect any 
downsides to it. Be it as it may, a test suite is needed. Until a comprehensive 
set of tests is written, this project's version will be labeled as less than 1.0.0.

Also, this project does not currently support NIO (Spring Webflux)  as it uses ThreadLocal to control
the request context. A simple solution is to add methods to take a snapshot of
the context and reinitialize after the NIO code. A more automatic solution would
require greater effort. Be it as it may, the snapshot is also not supported yet.
This is not related to the standard NIO Thread pool.

This was built and tested using Java 21 and Spring Boot 3.2.2. A Java 11 version with Sprint 2.x.x
is planned.

# Terminology
- Entry point is used as a general term to represent an event that triggers a request to start.
  It might be an HTTP request arriving at a rest endpoint (hereby called rest entry point), a message
  from a queue, etc.

# What is this project?
This is an implementation of a microservice's chassis pattern for spring boot 
applications. It deals with a few common concerns for distributed
(or not so distributed) applications:

- Logging
- Metrics and monitoring
- Stamp coupling
- Data validation
- Data transformation

# Motivation
I created this project based on the experiences I had while developing microservices
in the last couple of years or so, and also based on the reading I've done so far.
It's not uncommon for application needs to be ignored in favor of domain behavior, 
and the little code that exists is generally duplicated in many places. Also, I have
seen my fair share of anemic models, and while I understand it's never so simple to
get rid of data holders, I strive to use them the least possible.

The whole framework is based on the idea that all that happens within an application
request is connected to its current context. Data validation is not static, and 
neither is data transformation. The current context is available to the application 
anytime and can be used to transform and validate data, among other things.

# Usage
To use this project, you need to update your pom.xml if using Maven
```
<dependency>
    <groupId>io.github.renatols-jf</groupId>
    <artifactId>spboot-chassis</artifactId>
    <version>0.1.0-rc2</version>
</dependency>
```

or your build.gradle if using Gradle
```
implementation group: 'io.github.renatols-jf', name: 'spboot-chassis', version: '0.1.0-rc2'
```

This is a Spring Boot framework, and it will need to access Spring-managed
objects and dependency injection. A new interaction mechanism will be provided in
future releases, but currently, this project's main package needs to be scanned by
Spring. This is done by adding the `ComponentScan` annotation to the application, 
as in:

```
@SpringBootApplication
@ComponentScan("io.github.renatolsjf")
public class DemoApplication {
```

`@SpringBootApplication` adds `@ComponentScan` for the application's main package,
but I've seen issues with this when adding another package. 
So you might need to add it again, as in:

```
@SpringBootApplication
@ComponentScan("com.example.demo")
@ComponentScan("io.github.renatolsjf")
public class DemoApplication {
```

## Request
[Request](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/request/Request.java) 
is a unit of behavior. Everything that happens in the application should be
within a request. It automatically provides a means to log the request, update application
metrics, and some other useful application behavior. The idea is to think of a request
as a unit of processing, no matter the entry point. An HTTPS call should be mapped to
a request, just as a message consumed from a queue.

Request is an abstract class and needs to be extended to provide any functionality.
The idea is for the superclass to control application behavior, while the subclass
controls domain behavior.

The domain logic is to be implemented in the method `doProcess()` - 
it should **NEVER** be called directly, instead, `process()` should. At this point,
you might want to access Spring-managed objects, such as services. 
There are three ways of doing so (Note that an exception will be thrown if no beans qualify in all cases):
- Annotate the desired field with
  [Inject](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/request/Inject.java).
  `@Inject` will only work inside requests.
- `this.requestResource(MyService.class)` in which you provide the class you are expecting. 
  This is a syntactic sugar available only inside requests.
- `AppRegistry.getResource(MyService.class)` in which you provide the class you are expecting.
  This can be called anywhere.

After a request is finished, it must set its `RequestOutcome`. If no exception is thrown, the
request is deemed successful, and `RequestOutcome.SUCCESS` is initialized automatically.
There are two methods used to realize the final outcome in the case of an exception:
`Request#resolveError` and `Request#doResolveError` - both receive a `Throwable` as a parameter.

The idea is to identify the outcome based on the exception thrown. `Request#resolveError` is
called first. It checks if the `Throwable` is an instance of `ValidationException`, in which case
it sets the outcome as `RequestOutcome.CLIENT_ERROR`. If it is not, it defers the decision to
`Request#doResolveError`, which is abstract and must be implemented. In the case that 
`Request#doResolveError` return nulls (this **SHOULD NOT** happen), `Request#resolveError` 
sets the outcome as `RequestOutcome.SERVER_ERROR`.

There are a few request constructors available, but we will approach only the most complete:

```
public Request(String operation, String transactionId, String correlationId, List<String> projection,
                   Map<String, String> requestContextEntries)
```

### operation
Operation is the name given to the request or unit of behavior. While we might
have some more generic request implementations, it is expected that the subclass
initializes this value without it actually being one of its constructor parameters:

```
public CalculateMarginRequest(String transactionId, String correlationId, List<String> projection,
                   Map<String, String> requestContextEntries) extends Request {
    super("CALCULATE_MARGIN", transactionId, correlationId, projection, requestContextEntries);                  
}
```

This operation name will be the same used elsewhere to assert validations and
transformations, so it's probably a good idea to use a constant.

### transactionId
A unique identifier given to the transaction. The idea is to serve as an identifier
so a transaction can be traced through the various services. If a null 
transactionId is provided, a UUID4 will be created.

### correlationId
An external identifier given by an application outside the scope of 
the internal services. It enables the identification of every transaction or request
served with that corelationId.

### projection
Projection exists as a means to tackle stamp coupling. There are services that generate
big payloads as a response, and clients that need only a specific portion of that
response. This could lead to traffic overcharges, the need to map unnecessary fields,
among other issues.

Each request supports a list of field names that will be automatically filtered
before the response is given. Nestesd fields are represented with dots. 
So, if a request returns the following json:

```
{
   "aField":"aValue",
   "anotherField":true,
   "anObject":{
      "f1":12,
      "f2":null
   },
   "aList":[
      {
         "f3":false,
         "f4":"Andrew"
      },
      {
         "f3":true,
         "f4":"Ryan"
      }
   ]
}
```

the projection `["aField", "anObject.f2", "aList.f3"]` will yield: 

```
{
   "aField":"aValue",
   "anObject":{
      "f2":null
   },
   "aList":[
      {
         "f3":false
      },
      {
         "f3":true
      }
   ]
}
```

### requestContextEntries
We might want to log information that is not available to the current
service, but is used to identify messages in the service chain. Initializing this
map will make every entry* in it be logged with each message during the request
duration. The entries can also be supplied as a `String`, separating key from value
using `:` and separating entries using `;`, as in `aKey:aValue;anotherKey:anotherValue`.
It's highly recommended to use 
[EntryResolver](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/request/EntryResolver.java)
to do conversions if needed, though.

*Entries are logged in their own fields; exportation of fields other than message
depends on the logging configuration.

### traceparent
Tracing information, if available, should be propagated from service to service. Initializing
this information with the W3C trace context `traceparent` header will enable the chassis to
configure the tracing behavior and information for the request.

## Dependency Injection 
### A note on @Inject
Prior to version `0.1.0`, `@Inject` was meant as a means to inject Spring beans inside requests.

From version `0.1.0` forward, `@Inject` is a more general dependency injection feature and can inject objects
which lifecycle is not controlled by Spring. Moreover, it also enables behavior enhancement, e.g, all objects which methods
are annotated with `@Span` to enable tracing have to be annotated with `@Inject` somewhere down the injection tree. 
`@Inject` will only enhance behavior of classes with the appropriate annotations, but it will create an instance of
a class whether its behavior should be enhanced or not, only requiring a no-args constructor. Also, the beginning of an injection 
tree must be inside a `Request` - the first objects injected must be request fields.

The injection tree is the traversable graph of objects related to a request. Let's suppose that you have a `Request` class
called `RequestA`, a class `A`, and a class `B`. `RequestA` has a field of type `A`, and `A` has a field of type `B`.

```
public class RequestA extends Request {
...
    @Inject
    private A a;
...
}

public class A {
...
    @Inject 
    private B b;
...
}

public class B {
...
}
```

The above snippet will result in `A` being injected inside `RequestA` and `B` being injected inside `A`. If we
remove the `@Inject` annotation from the `A` field inside `RequestA`, that field will be null and, if 
instantiated manually, there will be no injection of `B` inside `A`.

A injection tree can be triggered outside a `Request` if so desired. Calling `AppRegistry::getResource`
for a type will trigger the injection process for the object graph starting with that type.

## Context
[Context](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/Context.java)
is the source of information for the current processing/request. It stores 
information like the operation in execution and the transactionId. It is
used by the framework to initiate transformations and validations. It also stores
information like elapsed time by type, which will be treated later in this
document.

It is initialized automatically as soon as a request object is created and
it's detroyed as soon as a request finishes. A context can be obtained
anywhere calling `Context.forRequest()`. Inside a request, a context is also an attribute
and can be accessed directly by calling `context`.
As a rule of thumb, we don't want to initialize the context manually elsewhere, 
so calling `Context.initialize` outside of a request will result in an error.
You also cannot call it manually inside a request, as it will result in an error
since the context already exists.

If you absolutely need to create a context outside a request, the class in which
you will be doing so needs to be annotated with 
[@ContextCreator](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/ContextCreator.java).
This will enable a context to be created. If a context already exists, an error will
still be thrown.

## Logging
Logging can be done by requesting an 
[ApplicationLogger](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/ApplicationLogger.java)
from the context: `ApplicationLogger logger = Context.forRequest().createLogger()`.
With an ApplicationLogger instance, you can use the default logging levels to log
information, as is: `logger.info(message, param1, param2).log()`. A few observations
are in order:

- There are a few fields that are always logged automatically:
  - transactionId: [see above](#transactionid)
  - correlationId: [see above](#correlationid)
  - operationTimes: [see below](#timing-operations-and-timer-classification)
  - operation: [see above](#operation)
  - elapsedTime: the time passed between the start of the request and the give log message

- Any log level provides two arguments: a message and an object varargs. Any varargs
  present will replace `{}` inside the message. 
  `logger.info("{} is greater than {}", number2, number1).log` 
  will result in "2 is greater than 1" being logged.
  
- `ApplicationLogger#error` also provides an exception argument. This will result
  in the stack trace being logged. Currently, framework exceptions are logged
  with their stack traces. A future release will allow this to be disabled.
  
- The logging level methods do not actually log the information but rather wait
  for the `log()` call. That is because we might want to add some extra information
  to the log. In a future release, the need of `log()` will be removed when
  extra information is not needed. Besides the fields present in the context, information related only
  to that message can also be logged:
    - `Context.forRequest().createLogger().log(message).attach("name", "Andrew").log()`
      will create a new field named "name" only for this message.
    - `Context.forRequest().createLogger().log(message).attachMap(aMap).log()`
      will create as many new fields as keys available in the map. This method
      also supports a String varargs to log only the desired fields.
    - `Context.forRequest().createLogger().log(message).attachObject(anObject).log()`
      will create as many fields as are available in the object. Again, you
      could filter the object providing only the keys you desire to log, but
      there is another approach for objects. You can annotate any field in a
      Class that you don't want to log as 
      [@Classified](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/data/Classified.java).
      This will create automatic transformations that will be applied to the field
      before exportation. You need to provide a 
      [ClassifiedCypher](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/data/cypher/ClassifiedCypher.java)
      implementation to the annotation. This strategy is used by the request as it logs
      its information. You can create yours or use one of the available (if none is specified,
      `IgnoringCypher` will be used):
        1. [HiddenClassifiedCypher](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/data/cypher/HiddenClassifiedCypher.java)
          which will print only if the field has a value or not.
        2. [IgnoringCypher](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/data/cypher/IgnoringCypher.java) 
          which will completely ignore that field.

Fields added via `attach` will **NOT** be exported in their own fields. Instead, they will be grouped
in a field called `context`. A future release will provide a configuration to change this behavior.
```
        Context.forRequest().withRequestContextEntry("fixedField", "Present in all messages!")
                .createLogger()
                .info("A message")
                .attach("aField", "aValue")
                .attach("anotherField", "anotherValue")
                .log();

        Context.forRequest()
                .createLogger()
                .info("A  second message")
                .attach("aThridField", "aThirdValue")
                .log();
```
will log: 
```
{"@timestamp":"2024-02-21T15:25:36.382-03:00","message":"A message","logger_name":"com.example.demo.DemoRequest","level":"INFO","context":"{\"anotherField\":\"anotherValue\",\"aField\":\"aValue\"}","fixedField":"Present in all messages!","operationTimes":"{internal=8, total=8}","operation":"DEMO_OPERATION","transactionId":"c52fa6c6-a5d9-4a39-961c-f832f232da57","elapsedTime":"8","application":"demo-application"}
{"@timestamp":"2024-02-21T15:25:36.382-03:00","message":"A  second message","logger_name":"com.example.demo.DemoRequest","level":"INFO","context":"{\"aThridField\":\"aThirdValue\"}","fixedField":"Present in all messages!","operationTimes":"{internal=8, total=8}","operation":"DEMO_OPERATION","transactionId":"c52fa6c6-a5d9-4a39-961c-f832f232da57","elapsedTime":"8","application":"demo-application"}
```
## Rendering
[Media](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/Media.java)
and 
[Renderable](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/Renderable.java)
are the main components of rendering. Every request terminates with render information,
even if there is nothing to render.  One of the main goals of the rendering framework
is to avoid the creation of DTOs. You should terminate `Request#doProcess`
with one of the following:

- `Media.ofRenderable(aRenderable)` providing a single instance of `Renderable` to render.
- `Media.ofCollection(aRenderableCollection)` providing a collection of `Renderables` to render.
- `Media.empty()` to render nothing.



### Renderable
Renderable denotes an object that can be rendered, it governs what and how is
rendered or exported. There two ways to mark a class as renderable:
implementing `Renderable` or implementing `FieldRenderable`.

`Renderable` provides a render method in which the desired information is exported:
```
package com.example.demo;

import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.Renderable;

public class Greeter implements Renderable {
    
    private final String name;
    
    public Greeter(String name) {
        this.name = name;
    }
    
    public String greet() {
        return "Hi! My name is: " + this.name;
    }
    
    @Override
    public Media render(Media media) {
        return media.print(name, this.name)
                .print("greeting", this.greet());
    }
    
}
```

Rendering the above class (calling `Media.ofRenderable(new Greeter("Andrew")).render()`) will yield:
```
{
   "name": "Andrew",
   "greeting": "Hi! My name is Andrew"
}
```

You can also render nested renderables of collections of renderables:
```
package com.example.demo;

import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.Renderable;

import java.util.ArrayList;
import java.util.List;

public class Greeter implements Renderable {

    private final String name;
    List<Echo> echoes = new ArrayList<>();

    public Greeter(String name) {
        this.name = name;
        this.echoes.add(new Echo());
        this.echoes.add(new Echo());
    }

    public String greet() {
        return "My name is: " + this.name;
    }

    @Override
    public Media render(Media media) {
        return media.print(name, this.name)
                .print("greeting", this.greet())
                .forkCollection("echoes", this.echoes);
    }

}

class Echo implements Renderable {
    @Override
    public Media render(Media media) {
        return media.print("echo", "what they said");
    }
}
```

Rendering the above class (calling `Media.ofRenderable(new Greeter("Andrew")).render()`) will yield:
```
{
   "name":"Andrew",
   "greeting":"Hi! My name is Andrew",
   "echoes":[
      {
         "echo":"what they said"
      },
      {
         "echo":"what they said"
      }
   ]
}
```

A single `Renderable` can be nested using `Media#forkRenderable` instead of
`Media#forkCollection`. As of version `0.0.3`, `Media#print` recognizes renderables and
collections, removing the need to call `Media#forkRenderable` and `Media#forkCollection`.

### FieldRenderable
`FieldRenderable` does not require an implementation for `Renderable#render`.
It walks the object inheritance tree and prints each class' fields up to `Object.class`

The rendering can be customized using 
[@RenderConfig](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/config/RenderConfig.java).
`RenderConfig` supports the following attributes:

- `operation`: indicates to which operations this configuration applies. It can be empty,
  a single operation, or multiple operations. A field can have more than one `RenderConfig`.
  If that's the case, the most suitable configuration will be applied. That is the
  first configuration which has the current operation listed or a configuration
  which has no operations configured.
- `policy` ([@RenderPolicy](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/config/RenderPolicy.java)):
  indicates whether the field will be rendered or ignored. This can be configured via
  `RenderPolicy.Policy` as in `@RenderConfig(policy = @RenderPolicy(RenderPolicy.Policy.RENDER))` 
  or `@RenderConfig(policy = @RenderPolicy(RenderPolicy.Policy.IGNORE))`. The default 
  behavior is to render.
- `alias`([@RenderAlias](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/config/RenderAlias.java)): 
  provides an alias for the current field: 
  `@RenderConfig(alias = @RenderAlias("anAlias"))`
- `transformer`([@RenderTransform](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/config/RenderTransform.java)):
  provides an implementation of 
  [RenderTransformer](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/config/RenderTransformer.java)
  that will transform the field value (`RenderTransformer` **MUST** have a public no 
  args constructor and be a public class):
  `@RenderConfig(transformer = @RenderTransform(MyTransformer.class))`
  
As of version `0.0.3`, `FieldRenderable` is able to render nested `Renderable` objects
and collections.

### Media
It's the means by which the information will be recorded. You never create a
Media object, instead you call `Media.ofRenderable`, `Media.ofCollection`, or
`Media.empty`. A call to `Media#render` actually exports the object information.

This is an area that can be improved upon. Render returns an `Object`, which is either
a `Map` for single instances or a `List` for collections. Spring Boot will 
return this without any issues, but a more powerful abstraction is a good idea.

### Transforming
Transforming is the means by which an output is transformed into another. This is **NOT**
the same as `@RenderTransform` from `@RenderConfig`. `@RenderTransform` applies to a single
field, while this operation applies to the whole data. It can be
called manually or initialized automatically according to the context. Currently,
the only context transformation available is the projection one.

Transformations can be done using 
[TransformingPath](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/transforming/TransformingPath.java),
an implementation of [MediaTransformer](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/MediaTransformer.java),
and [MediaContent](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/MediaContent.java).
Currently, very little support exists for this type of transformation. This is a part of the 
framework that needs to evolve, so I'll not write more information on this topic for now. 
You are probably better off not using it at the moment.

## Validation
Any class that implements 
[Validatable](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/Validatable.java)
can be validated simply by calling `myValidatableInstance.validate()`. This will trigger
an object lookup for any nested `Validatable` objects to validate all of them.

To configure validations, you have to annotate `Validatable` fields or 
methods with 
[@Validation](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/Validation.java).
A field or a method can have multiple validations configured. A `Validation` accepts
the following parameters:

- `operation`: indicates to which operations this validation applies. It can be empty,
  a single operation, or multiple operations. If a field or method has more than
  one `Validation`, all of those that have no operation configured or that have
  the current operation in its operation's list, will be applied.
  
- `nullable`([@Nullable](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/Nullable.java)):
  indicates whether a field or return of method can be null. Accepted values are:
    - `CAN_BE_NULL - @Validation(nullable = @Nullable(Nullable.NullableType.CAN_BE_NULL))`:
      in which the value can either be null or not.
    - `CANT_BE_NULL - @Validation(nullable = @Nullable(Nullable.NullableType.CANT_BE_NULL))`:
      in which the value cannot be null.
    - `MUST_BE_NULL - @Validation(nullable = @Nullable(Nullable.NullableType.CAN_BE_NULL))`:
      in which the value must be null.
- `min`([@Min](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/Min.java)):
  indicates the min value of the field - `@Validation(min = @Min(10))`.
- `max`([@Max](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/Max.java)):
  indicates the max value of the field - `@Validation(max = @Max(10))`.
- `oneOf` ([@OneOf](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/OneOf.java)):
  indicates that a field's value must be equal to one of the provided values -
  `@Validation(oneOf = @OneOf({"Ryan", "Andrew"}))`. Currently, it supports only
  `String` values, but it will be expanded in a future release.
- `pattern`([@Pattern](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/Pattern.java))  :
  indicates that a `String` must match a provided Regex -
  `@Validation(pattern = @Pattern("^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$"))`
- `custom`: array of [Validator](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/validators/Validator.java).
  Enables custom validators to be applied. Each `Validator` implementation **MUST** have a 
  public constructor that accepts a single `Validatable` parameter.
  
Every validation type, except custom, also accepts a message parameter, which will override the 
default error message in case of a validation error.

## API integrations - HTTP(s) calls
### ApiCall
[ApiCall](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/ApiCall.java)
is the preferred way to make HTTP requests. It's an attempt to create a more fluid API to configure and
make HTTP calls. To create an ApiCall, um request an instancen from
[ApiFactory](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/ApiFactory.java),
as in `ApiFactory.createApiCall()`. The following methors are available to configure the ApiCall 
(each returns the ApiCall object to enable chaining).

- `withProvider(String provider)`: It's used to identify to whom the service being called belongs.
  It's the first of a three-layered identification. I generally use the company
  or team responsible for the service.

- `withService(String service)`: It's used to identify to whom the service being called belongs to.
  It's the second of a three-layered identification. I generally use the actual
  service name being called.

- `withOperation(String operation)`: It's used to identify to whom the service being called belongs to.
  It's the third of a three-layered identification. I generally use a name for the
  operation being requested, e.g., Authorization.

- `withFollowRedirect(boolean followRedirect)`: It's used to indicate whether an ApiCall should redirect
  a request when prompted or return the result even when receiving a 3xx redirect.

- `withConnectTimeoutSeconds(long seconds)`: It's used to specify how long an ApiCall will wait before
  a connection is established. Defaults to 10.

- `withReadTimeOutSeconds(long seconds)`: It's used to specify how long an ApiCall will wait for a socket
  answer before throwing an error. Defaults to 40.

- `withFailOnError(boolean failOnError)`: It's used to indicate if an error will be thrown if an HTTP error
  code or connection issue will result in an exception thrown or not. Defaults to true.

- `withEndpoint(String endpoint)`: It's used to initialize the base endpoint to be called.

- `withQueryParam(String key, String value)`: It's used to add a param to the query string.

- `withUrlReplacement(String key, String value)`: Sometimes, part of the URL may need to be replaced,
  such as a URL that has some sort of identification and is provided via an environment variable.
  In order to be replaced, that part of URL need to be between `{}`. So, the following call
  `ApiFactory.createApiCall().withEndpoint("https://www.testsite.com/{productId}/price").withUrlReplacement("productId, "512")` 
  would ultimately send the request to `https://www.testsite.com/512/price`.

- `withApiMethod(ApiMethod apiMethod)`: The method to be executed. One of GET, POST, PUT, PATCH, or DELETE.

- `withHeader(String key, String value)`: Adds the desired header to the request. There are a few syntactic
  sugar header methods available. More will be added in future releases.

- `withBasicAuth(String username, String password)`: Adds an authorization header using Basic Auth.

- `withBearerToken(String token)`: Adds an authorization header using a Bearer Token.

- `withContenType(String contentType)`: Adds the `Content-Type` header.

- `withPropagateTrace(Boolean propagateTrace)`: Overrides the default trace propagation setting.

To make the request, we have a few behavior methods available. We have a method for each HTTP
method available: `ApiCall::get()`, `ApiCall::post()`, `ApiCall::put()`, `ApiCall::path()`, , `ApiCall::delete()`.
Despite having the option to configure the method calling `withApiMethod()`, that is generally not needed - a case when such
initialization is needed will be treated bellow. Except for `ApiCall::get()`, which expects no body,
all other methods described above have 4 different implementations:

- One that expects a `Renderable`, which will render the single object as the body of the request.
- One that expects multiple `Renderable`, which will render them as a list as the body of the request.
- One that expects a `Media`, which will be rendered as the body of the request.
- One that accepts any object. The API will make the best effort to export the object as the body. This is
  fine for Collections, Maps, etc. For a POJO, it will export the public getters.

DISCLAIMER: Currently, very limited support is  given for any content-type other than application/json and
application/x-www-form-urlencoded.

Besides the methods described above, there is also a method called `ApiCall::execute()`, which
has the same 3 variations. The method will use the ApiMethod configured vai `withApiMethod()`. Being able
to initialize the ApiMethod prior to the request execution will enable us to automatically configure
ApiCall objects as we will see bellow.

Upon executing a request, an
[ApiResponse](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/ApiResponse.java)
will be returned. It has the necessary data/behavior related to the request made. The following methods are available:

- `isConnectionError(): boolean`: Indicates whether the operation failed due to a connection / socket issue.
- `isRequestError(): boolean`: Indicates whether the operation completed with an HTTP status code error (4xx or 5xx)
- `isSuccess(): boolean`: Indicates whether the operation completed with an HTTP status code of success (2xx - or 3xx, in case redirections are not allowed)
- `isClientError(): boolean`: Indicates whether the HTTP status code is 4xx
- `isServerError(): boolean`: Indicates whether the HTTP status code is 5xx
- `isUnauthorized(): boolean`: Indicates whether the HTTP status code is 401
- `isForbidden(): boolean`: Indicates whether the HTTP status code is 403
- `getRawBody(): String`: Returns the HTTP response body as a String, if available.
- `getDuration(): long`: Returns the duration of the operation in milliseconds.
- `getHttpStatus(): String`: Returns the HTTP status or `CONNECTION_ERROR`, if `isConnectionError()` is true.
- `getHttpStatusAsInt(): int`: Returns the HTTP status or 0, if `isConnectionError()` is true.
- `getHeaders(): Map<String, String>`: Returns the headers present in the response.
- `getCause(): Throwable`: Returns an exception in case a connection error happened.
- `getBody(Class<T>): T`: Returns the response body transformed into the Type provided.
- `isBodyAvailable(): boolean`: Indicates wheter a Response Body is available or not.

If `failOnError` is true, an exception will be thrown in case the request is not successful :
- [IOApiException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/IOApiException.java)
  in case of a connection issue
- [ClientErrorApiException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/ClientErrorApiException.java)
  in case of a 4xx status other than 401 and 403.
- [UnauthorizedApiException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/UnauthorizedApiException.java)
  in case of a 401 status.
- [ForbiddenApiException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/ForbiddenApiException.java)
  in case of a 403 status.
- [ServerErrorApiException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/dsl/ServerErrorApiException.java)
  in case of a 5xx status.

#### Automatic API Configuration
It's possible to automatically configure Apis through a file called `chassis-api.yaml`, that should be placed
under the default resources folder. The following attributes can be configured (see ApiCall above for their meaning).
- `provider`: String.
- `service`: String.
- `operation`: String.
- `follow-redirect`: Boolean.
- `connection-timeout-seconds`: Integer.
- `read-timeout-seconds`: Integer.
- `fail-on-error`: Boolean.
- `endpoint`: String.
- `api-method`: String - GET, POST, PUT, PATCH, or DELETE.
- `basic-auth`: List with 2 elements - user and password.
- `bearer-token`: String.
- `header`: List with 2 elements - key and value. Supports multiple.
- `query-param`: List with 2 elements - key and value. Supports multiple.
- `url-replacement`: List with 2 elements - key and value. Supports multiple.
- `propagate-trace`: Boolean.

If an attribute supports multiple initializations, to initialize it multiple times an array with
a multiple of its total parameter count should be provided, e.g, to add 2 query parameters, a
list with 4 elements needs to be provided.

Also, the first element of the hierarchy will be the label by which that ApiCall is retrieved.
Here is an example of two different ApiCalls initialized by code and yaml.

You might want to initialize some contextual information within a request, like the current
transactionId in a header. For that, you can grab the current context with `$Context` and access
the transactionId, correlationId, or Operation calling `@Context.transactionId`,
`@Context.correlationId`, or `@Context.operation` respectively.
```
...
header:
  - X-TRANSACTION-ID
  - $Context.transactionId
  - X-CORRELATION-ID
  - $Context.correlationId
  - X-OPERATION
  - $Context.operation
```

YAML file
```
#label used to retrieve the operation
google-search: 
  operation: SEARCH
  service: GOOGLE_SEARCH
  provider: GOOGLE
  follow-redirect: false
  endpoint: https://www.google.com.br
  method: GET
  failOnError: false
  basicAuth: ["user", "pass"]
  queryParam: [test1, test2, test3, test4]
  header:
    - X-TRANSACTION-ID
    - $Context.transactionId
  
a-random-api
  operation: RANDOM_OPERATION
  service: RANDOM_SERVICE
  provider: RANDOM_PROVIDER
  endpoint: https://www.randomservice.com
  method: POST
  fail-on-error: true
  bearerToken: 043e8ea2-950c-466a-a7d2-78d693e60a62
```

Code creation and execution if not configured in YAML
```
ApiResponse apiResponse = ApiFactory.createApiCall()
  .withOperation("SEARCH")
  .withService("GOOGLE_SEARCH")
  .withProvider("GOOGLE")
  .withFollorRedirect(false)
  .withEndpoint("https://www.google.com.br")
  .withFailOnError(false)
  .withBasicAuth("user", "pass")
  .withQueryParam("test1", "test2")
  .withQueryParam("test3", "test4")
  .withHeader("X-TRANSACTION-ID", Context.forRequest().getTransactionId())
  .get();
  
ApiResponse apiResponse = ApiFactory.createApiCall()
  .withOperation("RANDOM_OPERATION")
  .withService("RANDOM_SERVICE")
  .withProvider("RANDOM_PROVIDER")
  .withEndpoint("https://www.randomservice.com")
  .withBearerToken("043e8ea2-950c-466a-a7d2-78d693e60a62")
  .post(() -> media.print("aKey", "aValue")); // A Renderable, or a FieldRenderable, or a Map, etc.  
```

Code creation and execution using YAML configuration
```
ApiResponse apiResponse = ApiFactory.apiFromLabel("googleSearch").execute();
ApiResponse apiResponse = ApiFactory.apiFromLabel("aRandomApi").execute(() -> media.print("aKey", "aValue")); // A Renderable, or a FieldRenderable, or a Map, etc.
```

Here is an example of the same ApiCall created and exe

### RestOperation
DISCALIMER - RestOperation has been deprecated in favor of `ApiCall`. It will be REMOVED in a
later release. Also, it does not support tracing.

Each HTTP request should be made using the 
[RestOperation](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/RestOperation.java) 
class. To create a `RestOperation` you should call `RestOperation#create` 
providing the following parameters:

- `provider`: It's used to identify to whom the service being called belongs.
  It's the first of a three-layered identification. I generally use the company
  or team responsible for the service.
  
- `service`: It's used to identify to whom the service being called belongs to.
  It's the second of a three-layered identification. I generally use the actual
  service name being called.
  
- `operation`: It's used to identify to whom the service being called belongs to.
  It's the third of a three-layered identification. I generally use a name for the
  operation being requested, e.g., Authorization.
  
- `uri`: self-explanatory; the URI being called.

- `headers`: A `Map<String, String>` with header information. Can be null.

- `body`: A `Map<String, Object>` with body information. Can be null.

- `HttpMethod`: The desired HTTP method.

`RestOperation#create` can also be replaced with `RestOperation#get`,
`RestOperation#post`, `RestOperation#patch`, `RestOperation#put` or
`RestOperation#delete` - these do not need the `HttpMethod` parameter.

To make the HTTP call, simply call `RestOperation#call`. Two implementations
are available: one that accepts a return type and one that accepts a return type
and an error type. If no return type is expected, a null value can be passed. If
an spefic error type is not expected, using the `RestOperation#call` without
the error type will result in errors being initialized in a simple `Map`. 

You can change connect and read timeouts for the call through `RestOperation#withConnectTimeout`, 
which defaults to 10 seconds, and `RestOperation#withReadTimeOut`, which defaults to 40 seconds. You can
also configure if a redirect should be followed with `RestOperation#withFollowRedirect`, which
defaults to true.

In case a connection issue happens, a
[IOErrorOperationException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/IOErrorOperationException.java)
will be thrown.
In the event of a non 2xx/3xx http status return, either a 
[ClientErrorOperationException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/ClientErrorOperationException.java) or a
[ServerErrorOperationException](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/ServerErrorOperationException.java)
will be thrown. Both are implementations of `StatusRestOperationException`,
which provides `getBody()` to get the parsed error.

Using `RestOperation` provides automatic logging and metrics creation for the 
call. These will be configurable in a future release.

## Monitoring
This framework exports metrics to Prometheus automatically using the Spring actuator.
It provides a facade for metrics, which will reflect in the Spring/Micrometer `MeterRegistry`.

### Metric types and creation
Metrics are created with the help of `MetricRegistry` - not  the same as `MeterRegistry` from
Spring/Micrometer. There is no need to store references to the created metrics. Each time
you try to build a `Metric`, the `MetricRegistry` will either create a new
one or return an existing `Metric` in case it's the same type and has the same name and
same tags as the one requested. Currently, there are 4 metrics that can be created:

- `Counter`: A counter, as the name says, counts something. As long as the application
  is running, its value never resets.
```
package com.example.demo;

import io.github.renatolsjf.chassis.Chassis;

public class MetricDemo {

    public MetricDemo() {

        /*
        Increases the value by 1
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_counter")
                .withTag("aTagName", "aTagValue")
                .buildCounter()
                .inc();
        /*
        Increases the value by the desired amount
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_counter")
                .withTag("aTagName", "aTagValue")
                .buildCounter()
                .inc(2d);

        /*
        Exported to Prometheus as:
        a_counter_total{aTagName="aTagValue",} 3.0
         */
    }

}
  ```
- `Gauge`: A gauge stores a value that can be changed as desired.
```
package com.example.demo;

import io.github.renatolsjf.chassis.Chassis;

public class MetricDemo {
    public MetricDemo() {
        /*
        Increases the value by 1
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_gauge")
                .withTag("aTagName", "aTagValue")
                .buildGauge()
                .inc();
        /*
        Increases the value by the desired amount
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_gauge")
                .withTag("aTagName", "aTagValue")
                .buildGauge()
                .inc(2d);

        /*
        Decreases the value by the desired amount
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_gauge")
                .withTag("aTagName", "aTagValue")
                .buildGauge()
                .dec();

        /*
        Decreases the value by the desired amount
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_gauge")
                .withTag("aTagName", "aTagValue")
                .buildGauge()
                .dec(2d);

        /*
        Sets the value to the current timestamp
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_gauge")
                .withTag("aTagName", "aTagValue")
                .buildGauge()
                .setToCurrentTime();

        /*
        Sets the value to the desired amount
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_gauge")
                .withTag("aTagName", "aTagValue")
                .buildGauge()
                .set(2d);
        
        /*
        Expord to Prometheus as:
        a_gauge{aTagName="aTagValue",} 2.0
         */

    }
}
```  

- `TrackingGauge`: A `TrackingGauge` is fundamentally the same as a `Gauge`. It
  differs in the way the measurement is done. You don't change its value
  manually. Instead, it tracks an `ObservableTask` object.
```
package com.example.demo;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.monitoring.ObservableTask;

public class MetricDemo {
    public MetricDemo() {

        /*
        Tracks the desired task
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_tracking_gauge")
                .withTag("aTagName", "aTagValue")
                .buildTrackingGauge()
                .track(() -> 2); //or .track(new Task());

        /*
        Tracks the desired task with a WeakReference. As soon as the WeakReference is cleared
        the metric will measure 0. Since the new task is not referenced anywhere else,
        this metric will default to 0 as soon as the garbage collector runs.
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_tracking_gauge")
                .withTag("aTagName", "aTagValue")
                .buildTrackingGauge()
                .weakTrack(new Task()); //or .weakTrack(() -> 2);
        
        /*
        Exported to Prometheus as:
        a_tracking_gauge{aTagName="aTagValue",} 100.0
         */

    }
}

class Task implements ObservableTask {
    @Override
    public double getCurrentValue() {
        return 100;
    }
}
```  
- `Histogram`: histogram counts distributions along a defined range. It can be used to
  calculate quantiles on Prometheus.
```
package com.example.demo;

import io.github.renatolsjf.chassis.Chassis;

public class MetricDemo {
    public MetricDemo() {

        /*
        Creates a histogram with bucket values of 0.1, 0.2, 0.5, 1, 5, and 10
         */
        Chassis.getInstance().getMetricRegistry().createBuilder("a_histogram")
                .withTag("aTagName", "aTagValue")
                .buildHistogram(.1d, .2d, .5d, 1d, 5d, 10d)
                .observe(.255d);
        
        /*
        Exported to Prometheus as:
        a_histogram_bucket{aTagName="aTagValue",le="0.1",} 0.0
        a_histogram_bucket{aTagName="aTagValue",le="0.2",} 0.0
        a_histogram_bucket{aTagName="aTagValue",le="0.5",} 1.0
        a_histogram_bucket{aTagName="aTagValue",le="1.0",} 1.0
        a_histogram_bucket{aTagName="aTagValue",le="5.0",} 1.0
        a_histogram_bucket{aTagName="aTagValue",le="10.0",} 1.0
        a_histogram_bucket{aTagName="aTagValue",le="+Inf",} 1.0
        a_histogram_count{aTagName="aTagValue",} 1.0
        a_histogram_sum{aTagName="aTagValue",} 0.255
         */

    }
}
```

## Built-in metrics
Each time a request is executed, a few pre-defined metrics are created as the operation runs. If
a request should not count towards application health, as the health request itself does not,
the request class should be annotated with 
[@HealthIgnore](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/request/HealthIgnore.java).
If the request does not have that annotation, the following metrics will be generated:

- A `Gauge` called `operation_active_requests`. It stores how many requests are running. 
  It increases as soon as a request starts and decreases as soon as the request finishes.
  It uses the current `operation` as a `tag`.
  
- A `Gauge` called `operation_health`. It stores the current health of a given operation.
  It uses the current `operation` as a `tag`.

- A `Histogram`, named `operation_request_time`. It stores the time taken for each request, 
  in milliseconds, in buckets. It uses as a `tag`: the current `operation`, the `outcome`
  of the request (success, client_error, or server_error), and the `timer_type`. More information
  on `timer_type` is provided in [Timing operations and timer classification](#timing-operations-and-timer-classification) section.
  
- If a `RestOperation` is executed, a `Histogram` named `integration_request_time`.
  It stores the time taken for each HTTP call, in milliseconds, in buckets. 
  It uses as a `tag`: the `provider` for the `RestOperation`, the `service` for the `RestOperation`
  the `operation` for the `RestOperation`, `outcome` which is either an `Http Status Code` or
  `connection_error`, and `type`, which defaults to `http`.
  
- A `Gauge` called `integration_health`. It stores the current health of a given integration.
  It uses as a `tag`: the `provider` for the `RestOperation`, the `service` for the `RestOperation`
  the `operation` for the `RestOperation`, and `type`, which is always `http`.


Here is how those metrics are exported to Prometheus:
```
operation_health{operation="DEMO_OPERATION",} 100.0
operation_active_requests{operation="DEMO_OPERATION",} 0.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",le="200.0",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",le="500.0",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",le="1000.0",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",le="2000.0",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",le="5000.0",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",le="10000.0",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",le="+Inf",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",} 1.0
operation_request_time_bucket{operation="DEMO_OPERATION",outcome="success",timer_type="internal",} 10.0
operation_request_time_max{operation="DEMO_OPERATION",outcome="success",timer_type="internal",} 10.0
integration_request_time_bucket{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="200.0",} 0.0
integration_request_time_bucket{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="500.0",} 0.0
integration_request_time_bucket{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="1000.0",} 1.0
integration_request_time_bucket{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="2000.0",} 1.0
integration_request_time_bucket{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="5000.0",} 1.0
integration_request_time_bucket{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="10000.0",} 1.0
integration_request_time_bucket{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="+Inf",} 1.0
integration_request_time_count{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",} 1.0
integration_request_time_sum{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",} 760.0
integration_request_time_max{provider="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",} 760.0
integration_health{provider="GOOGLE",operation="SEARCH",service="SEARCH",} 100.0
```

## Built-in health information
The application has a default 
[HealthRequest](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/request/HealthRequest.java) 
that exports health information in `json`.
It exports health percentage, request count, quantiles for the time taken for each type, and
result count by type. It does so by each operation, and also aggregates as application information.
The application health is not an average by default (but can be configured to be so). 
Instead, it reflects the health of the worst operation. A `HealthRequest` is not traceable and
does not affect application health. In other words, it's marked with both `@NotTraceable` and
`@HealthIgnore`.
To use it, create a `HealthRequest`, process it and render the result, such as 
`new HealthRequest().process().render()`. To tie this to a Spring rest entry point, just use something like:

```
@GetMapping("healthcheck")
public ResponseEntity healthCheck() {
    return ResponseEntity.ok(new HealthRequest().process().render());
}
```

A sample result:

```
{
    "application": {
        "health": 0.0,
        "load": {
            "requestCount": 3,
            "requestTime": {
                "internal": {
                    "quantiles": {
                        "0.5": 2,
                        "0.95": 5,
                        "0.99": 5
                    }
                },
                "http": {
                    "quantiles": {
                        "0.5": 574,
                        "0.95": 1424,
                        "0.99": 1424
                    }
                }
            }
        },
        "result": {
            "success": 2,
            "clientError": 0,
            "serverError": 1
        },
        "operations": [
            {
                "name": "INTEGRATION_TEST_2",
                "health": 0.0,
                "load": {
                    "requestCount": 1,
                    "requestTime": {
                        "internal": {
                            "quantiles": {
                                "0.5": 5,
                                "0.95": 5,
                                "0.99": 5
                            }
                        },
                        "http": {
                            "quantiles": {
                                "0.5": 1424,
                                "0.95": 1424,
                                "0.99": 1424
                            }
                        }
                    }
                },
                "result": {
                    "success": 0,
                    "clientError": 0,
                    "serverError": 1
                }
            },
            {
                "name": "LOGGING_TEST",
                "health": 100.0,
                "load": {
                    "requestCount": 1,
                    "requestTime": {
                        "internal": {
                            "quantiles": {
                                "0.5": 2,
                                "0.95": 2,
                                "0.99": 2
                            }
                        }
                    }
                },
                "result": {
                    "success": 1,
                    "clientError": 0,
                    "serverError": 0
                }
            },
            {
                "name": "INTEGRATION_TEST",
                "health": 100.0,
                "load": {
                    "requestCount": 1,
                    "requestTime": {
                        "internal": {
                            "quantiles": {
                                "0.5": 2,
                                "0.95": 2,
                                "0.99": 2
                            }
                        },
                        "http": {
                            "quantiles": {
                                "0.5": 574,
                                "0.95": 574,
                                "0.99": 574
                            }
                        }
                    }
                },
                "result": {
                    "success": 1,
                    "clientError": 0,
                    "serverError": 0
                }
            }
        ]
    },
    "integration": {
        "health": 100.0,
        "load": {
            "requestCount": 2,
            "requestTime": {
                "http": {
                    "quantiles": {
                        "0.5": 574,
                        "0.95": 1424,
                        "0.99": 1424
                    }
                }
            }
        },
        "result": {
            "200": 1,
            "403": 1
        },
        "providers": [
            {
                "name": "GOOGLE",
                "health": 100.0,
                "load": {
                    "requestCount": 2,
                    "requestTime": {
                        "http": {
                            "quantiles": {
                                "0.5": 574,
                                "0.95": 1424,
                                "0.99": 1424
                            }
                        }
                    }
                },
                "result": {
                    "200": 1,
                    "403": 1
                },
                "services": [
                    {
                        "name": "SEARCH",
                        "health": 100.0,
                        "load": {
                            "requestCount": 2,
                            "requestTime": {
                                "http": {
                                    "quantiles": {
                                        "0.5": 574,
                                        "0.95": 1424,
                                        "0.99": 1424
                                    }
                                }
                            }
                        },
                        "result": {
                            "200": 1,
                            "403": 1
                        },
                        "operations": [
                            {
                                "name": "SEARCH_IMAGES",
                                "health": 100.0,
                                "load": {
                                    "requestCount": 1,
                                    "requestTime": {
                                        "http": {
                                            "quantiles": {
                                                "0.5": 1424,
                                                "0.95": 1424,
                                                "0.99": 1424
                                            }
                                        }
                                    }
                                },
                                "result": {
                                    "403": 1
                                }
                            },
                            {
                                "name": "SEARCH",
                                "health": 100.0,
                                "load": {
                                    "requestCount": 1,
                                    "requestTime": {
                                        "http": {
                                            "quantiles": {
                                                "0.5": 574,
                                                "0.95": 574,
                                                "0.99": 574
                                            }
                                        }
                                    }
                                },
                                "result": {
                                    "200": 1
                                }
                            }
                        ]
                    }
                ]
            }
        ]
    }
}
```

## Tracing
As of version `0.1.0`, the framework supports tracing / distributed tracing. Every `Request` is
traced by default - note that traced is used in general since tracing might be disabled, or a
specific execution of a request might not be sampled, depending on the configuration.
If a `Request` is never to be sampled, it can be annotated with
[@NotTraceable](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/tracing/NotTraceable.java).

Considering that a request is traced and sampled, by default, a single `Span` will be created with the
request's name. The framework considers a method as the scope of a `Span`. To add more spans, 
the class in which a method or methods will be exported as spans, has to be annotated with
[@Traceable](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/tracing/Traceable.java).
This will not trigger automatic span creations, as the methods which will be traced also need to be annotated with
[@Span](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/tracing/Span.java).
`@Span` supports a single attribute, `value` for the name of the span. `value` can be omitted, in which
case the method name will be used.

Tracing works via behavior enhancement, which only happens via injections. In other words, for tracing
to work, the `@Traceable` object has to be inside the injection graph and must be annotated with `@Inject`.

To add custom span attributes to a `Span`, there are two annotations:
- [@SpanAttribute](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/tracing/SpanAttribute.java):
  Supports a key and a value that will be added as an attribute for the span that represents the method. Both the
  method and the class can be annotated with `SpanAttribute`. `SpanAttribute` belonging to the class will
  be used in all methods annotated with `@Span`. If both a class and method `SpanAttribute` have the
  same key, the method's value will be used.

- [@SpanAttributeParameter](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/tracing/SpanAttributeParameter.java):
  Used to annotate a parameter for a method annotated with `@Span`. Supports a `value` which is the key
  of the attribute. If none is provided, the method name will be used as key. The value for the attribute
  is the actual parameter value.  `@SpanAttributeParameter` takes precedence over `@SpanAttribute` if
  conflicting keys are found.

Currently, tracing information can only be exported to a `Zipkin` instance.

## Timing operations and timer classification
The framework enables us to classify the time taken while processing an operation. 
The classification is done using a simple `String` as a `Tag`. To measure the time for a block of code, 
we use [TimedOperation](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/timing/TimedOperation.java).

A `TimedOperation` will record the time it took automatically to the `Context`. 
You can tag the `TimedOperation` however you'd like, using `new TimedOperation(myTag)`, or using one of two
pre-defined tags: http `TimedOperation.http()`, used for HTTP calls, and db `TimedOperation.db()`,
used for database calls. 

`TimedOperation.http()` is used internally in `RestOperation` and `ApiCall`, and
`TimedOperation.db()` has to manually wrap a database call - since version `0.1.0` it's now possible
to annotate such methods with `@AsTimedOperation` instead of manually wrapping them, see below. It's not uncommon for database calls
to be automatic, having only the interface for the `Repository` created. To avoid wrapping every call
to the repository in `TimedOperation`, you can create a delegate for such cases:

```
package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DemoRepositoryDelegate extends JpaRepository<Demo, String> {
    List<Demo> findAllByStatus(Demo.Status status);
    Optional<Demo> findByAField(String aFieldValue);
}
```

```
package com.example.demo;

import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DemoRepository {

    @Autowired
    private DemoRepositoryDelegate demoRepositoryDelegate;

    public List<Demo> findUnfinishedDemos() {
        return TimedOperation.<List<Demo>>db()
                .execute(() -> this.demoRepositoryDelegate.findAllByStatus(Demo.Status.UNFINISHED));
    }

    public Optional<Demo> findByAField(String aFieldValue) {
        return TimedOperation.<Optional<Demo>>db()
                .execute(() -> this.demoRepositoryDelegate.findByAField(aFieldValue));
    }

    public void saveDemo(Demo demoData) {
        TimedOperation.db().run(() -> this.demoRepositoryDelegate.save(demoData));
    }

    public void deleteDemo(Demo demoData) {
        TimedOperation.db().run(() -> this.demoRepositoryDelegate.delete(demoData));
    }

}

```
A timed operation can be executed in two ways: `run` and `execute`. `run` expects a 
`java.lang.Runnable` and has no return type, while `execute` expects a `java.concurrent.Callable` 
and returns whatever type the generic call was made with. 

To run:
```
new TimedOperation("aTagValue").run(() -> System.out.println("I ran!"));
```
or
```
TimedOperation.db().run(() -> System.out.println("Pretend I am a database call!"));
```

To execute:
```
List<String> aList = new TimedOperation<List<String>>("aTagValue").execute(() -> Collections.emptyList());
```
or
```
List<String> aList = TimedOperation.<List<String>>http().execute(() -> Collections.emptyList()); //Pretend this is an HTTP request!
```

A negligible difference (of a few milliseconds) can be expected between the final log, prometheus
output, and the health request output. That happens because these calculations happen at the end
of the request, but the timer is still ticking. We could stop the timer as soon as the domain logic
is over for this wrap-up to use a stopped timer, but this is part of the request after all. 
Be it as it may, a future release will include a configuration to stop the timer.

A `TimedOperation` can be automatically traced by adding the span name, calling `TimedOperation::traced`.
Span attributes can be added by calling `TimedOperation::withTraceAttribute`, providing a `String` for the
attribute key and a `String` for the attribute value.

### @AsTimedOperation annotation
Since version `0.1.0`,
[@AsTimedOperation](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/timing/AsTimedOperation.java)
enables auto time recording for methods. Simply annotate a method with `@AsTimedOperation`, and it will
automatically add the time to the context. You can also annotate the class, in which case all of its methods
will be converted do `TimedOperation`. Since this is a behavior enhancement, the object has to
be in the injection graph - see [above](#dependency-injection). Since `@AsTimedOperation`
can also be configured to trace / create a span, `@AsTimedOperation` and `@Span` are incompatible.
`@AsTimedOperation` will be ignored if a method is also annotated with `@Span`.

The following attributes are supported for `@AsTimedOperation`:
- `tag`: String, required. Defines the tag for the timed operation. You can define any String value. 
  `AsTimedOperation` has 2 pre-defined constants: `AsTimedOperation.HTTP`, for a tag with the value `http`
  and `AsTimedOperation.DB`, for a tag with the value db.
- `traced`: Boolean, defaults to false. Indicates if the method will also be traced, create a span.
  The same `@SpanAttribute` and `@SpanAttributeParameter` also applies to a `@AsTimedOperation`method.
- `spanName`: String, defaults to empty. The name of the span to be used if the operation is traced.
  If empty, the method name will be used. Mind that, if it's the class that is annotated with
  `@AsTimedOperation`, the spanName shall be a concatenation of both the `spanName` attribute plus
  the method name.

### TimedOperation limitations
Currently, there is not a managed context for TimedOperations. That means that, if two TimedOperations are ran at
the same time, either concurrently or one timedOperation starts another before it finishes, the total time of both
will be count towards total execution time. Such cases will create errors in the total request execution time reported.
A fix is scheduled for a near future.

## A note about YAML loading and environment variables
It's possible to load values from environment variables. To do so, the YAML value should be put inside
${} - do not confound with $ in $Context mentioned above. It's also possible to use a default value in case the environment variable is not set by using 
a colon followed by a dash as in ${:-}.

The following uses MY_ENV to initialize the day
```
work:
  day: ${MY_ENV}
```

The following uses MY_ENV to initialize the or set it with the default `Monday`
```
work:
  day: ${MY_ENV:-Monday}
```

## Configuration
Configurations allow for changes to default application behavior. To change a behavior, insert
the desired value in a file called `chassis-config.yaml` or `chassis-config.yml` under the 
default resources folder. The following configurations can be changed:

- `logging.use-calling-class`: Boolean, defaults to `true`. Governs whether the stack trace will be 
  used or not to initialize the logging class. When creating an `ApplicationLogger`, 
  you can provide a class or not to be exported as the logger name. In case where no class is provided,
  as in `Context.forRequest().createLogger()`, this configuration will be used to initialize
  the class. If it's true, the class will be the calling class. If it's false, it will
  always be `ApplicatonLogger`.
- `logging.print-context-as-json`: Boolean, defaults to `true`. Governs whether the `context` field present
  in log messages will be exported as `json`.
- `logging.enable-default-attributes-overwrite`: Boolean, defaults fo `false`. Governs whether automatic logging
  attributes, such as `transactionId`, can have their value replaced with a call for 
  `Context#withRequestContextEntry` as in
  `Context.forRequest.withRequestContextEntry("transactionId", aNewValue)`.
- `logging.print-trace-id`: Boolean, defaults to `true`. Indicates whether traceId and spanId will be available in
  log messages.
- `validation.fail-on-execution-error`: Boolean, defaults to `true`. Governs whether an unexpected error
  in a validation attempt results in an exception. Every validation that fails for not matching
  the annotations for the current operation will result in an exception. For whatever
  reason, an exception can happen in the middle of the validation attempt. Let's say a method
  that is being validated throws an exception. If this configuration is true, the validation 
  will not happen, and an exception will be thrown. If this configuration is false, the validation
  will not happen, but it will be ignored, and if no other validation fails, the `Validatable`
  would be deemed valid. 
- `context.forbid-unauthorized-creation`: Boolean, defatuls to `true`. `Context.initialize` can't be called
  anywhere to create a `Context`. Allowing the `Context` creation is error-prone and, in almost
  all cases, not needed. If this configuration is true, unless the class in which 
  `Context.initialize` is being called is annotated with `@ContextCreator`, an exception will
  be thrown.
- `context.allow-correlation-id-update`: Boolean, defaults to `true`. Governs whether a `correlationId` can be
  updated after the context has been initialized. 
- `metrics.request.duration.histogram-range`: Integer list, defaults to `[200, 500, 1000, 2000, 5000, 10000]`.
  Governs the default `Histogram` buckets for request duration in milliseconds. 
- `metrics.request.duration.export-by-type`: Boolean, defaults to `true`. Governs whether `Histogram` metrics
  for request duration will be tagged with timer types. 
- `metrics.health-window-duration-minutes`: Integer, defaults to 5. Governs the maximum age of requests
  to be used in health calculations.
- `metrics.health-value-type`: String, defaults to `lowest`. The type of health calculation used to export
  the overral application health. Either `lowest`, to use the lowest operation health as the application heatlh,
  or `average`, to calculate the average of the health of the operations.
- `instrumentation.tracing.enabled`: Boolean, defaults to `false`. Determines if (distributed) tracing is enabled
  or not. If disabled, no tracing information will be recorded or created. Trace headers won't be created.
- `instrumentation.tracing.strategy`: String, defaults to `ALWAYS_SAMPLE`. Determine the sampling strategy for
  tracing information. If tracing is disabled, this configuration has no effect. Can be one of:
  - `ALWAYS_SAMPLE`: records every request.
  - `NEVER_SAMPLE`: records no request. Note that this is not the same as having the tracing disabled. This will still
    generate traceparent header for propagation.
  - `RATIO_BASED_SAMPLE`: partially records the sampling information based on `instrumentation.tracing.ratio` configuration.
  - `PARENT_BASED_OR_ALWAYS_SAMPLE`: use the traceparent header information from the upstream service to determine if the request
    is to be recorded. If not available, defaults to `ALWAYS_SAMPLE`.
  - `PARENT_BASED_OR_NEVER_SAMPLE`: use the traceparent header information from the upstream service to determine if the request
    is to be recorded. If not available, defaults to `NEVER_SAMPLE`.
  - `PARENT_BASED_OR_RATIO_BASED_SAMPLE`: use the traceparent header information from the upstream service to determine if the request
    is to be recorded. If not available, defaults to `RATIO_BASED_SAMPLE`.
- `instrumentation.tracing.ratio`: Double, defaults to 0.1. Determines the percentage of requests which should be recorded, in
  which 0 means none and 1 means all.
- `instrumentation.tracing.auto-propagation-enabled`: Boolean, defaults to `true`. Determines if `ApiCall` requests will
  automatically propagate the traceparent header. `ApiCall` supports an override for request by request configuration.
- `instrumentation.tracing.add-custom-prefix`: Boolean, defaults to `true`. Indicates whether the `custom.` preffix will be 
  added to span attributes added via `SpanAttribute` or `SpanAttributeParamenter`.
- `instrumentation.tracing.zipkin-url`: String, no default. Stores the zipkin url to which the traces will be sent.

## Labels
Labels are a means to change default labels, names, or captions for the framework. For that, you need to create
a file called `chassis-labels.yaml` under the default resources folder. Just add the data that you wish to override.
The following fields are supported:
- `application.name`: The name of the application.
- `application.instance-id`: The ID for this instance of the give application. Defaults to a UUID v4.
- `logging.application-name`: The name of the field under which the application name will be logged.  
- `logging.transaction-id`: The name of the field under which the transactionId will be logged.
- `logging.correlation-id`: The name of the field under which the correlationId will be logged.
- `logging.operation`: The name of the field under which the operation will be logged.
- `logging.elapsed-time`: The name of the field under which the elapsed time will be logged.
- `logging.operation-times`: The name of the field under which the operation times will be logged.
- `logging.context`: The name of the field under which the logging context will be logged.
- `metrics.name.operation-health`: The name of the metric created to display the operation health.
- `metrics.name.active-operations`: The name of the metric created to count active requests for an operation.
- `metrics.name.operation-time`: The name of the metric created to display the time taken by an operation.
- `metrics.name.integration-health`: The name of the metric created to display the integration health. 
- `metrics.name.integration-time`: The name of the metric created to display the time taken by an integration call.
- `metrics.tag.application-name`: The name of the metric tag used to identify the application.
- `metrics.tag.instance-id`: Then name of the metric tag used to identify the instance id. 
- `metrics.tag.operation`: The name of the metric tag used to identify operations.
- `metrics.tag.outcome`: The name of the metric tag used to identify the outcome.
- `metrics.tag.timer-type`: The name of the metric tag used to identify the timer type.
- `metrics.tag.provider`: The name of the metric tag used to identify the integration provider.
- `metrics.tag.service`: The name of the metric tag used to identify the integration service.
- `metrics.tag.type`: The name of the metric tag used to identify the integration type.
- `metrics.tag.value.http-type`: The name of the metric tag value used to identify the http integrations.

i18n is also supported by adding an underscore plus the language code after the name. That means files 
named like `chassis-labels_en_US.yaml`, `chassis-labels_pt_BR.yaml`, and `chassis-labels_en.yaml` can be used.
If the locale was set to `en_US`, the framework would search for files in the following order:
`chassis-labels_en_US.yaml`, `chassis-labels_en.yaml`, and `chassis-labels.yaml`.



# Next Steps
A few future updates have been thought of. Having said that, this does not neither represent
an order of priority nor a commitment. These are just a few items that might be done
in the future.

- Create a configuration to not log stack traces.
- Create a configuration to log attached fields in their own fields.
- Allow for `@OneOf` to accept values other than `String`.
- Create further validations, such as the minimum and the maximum size.
- Create a configuration to have some level of control in automatic logs and metrics.   
- Remove the need to call `.log()` for messages that have no attachment.  
- Evolve the way this framework interacts with Spring to remove the need for `@ComponentScan`
- Allow extra tags in automatic metrics.
- Create a configuration to stop the timer as soon as the domain logic is over (`Request#doProcess`)
- Create a summary type metric;
- Allow request duration metrics to be collected in measurements different from milliseconds.  
- Create mechanism to work with Java NIO.
- Create a configuration to not swallow rendering transformation errors, which is currently
  being done.
- Create a configuration to control whether an exception in a request will be wrapped in
  a `RequestException` that holds the original exception, and the outcome of the request.
- Implement default validators.
- Implement yml configuration for things like validations, and more.
- Create a circuit breaker or failsafe structure that can be used to wrap calls.
- Implement structure to enable A/B testing.
- Enable external configuration initialization, be it from yml or from some tool.
- Evolve rendering transformation
- Dynamic validation rule loading from a datasource.
- Cross-field or cross-concern validation.
- Creation of behavior flows using yml or other configuration tools. Possibly another project 
that uses this one as lib.
- Refactor undocumented proxy and genesis APIs to support external use.
- Create context management for `TimedOperation` so that if two timed operations bein run concurrently or one
  inside the other do not count twice towards total execution time.

# Sample project
The Sample project is based on version `0.0.2`. An update is soon to come.
[Demo Application](https://github.com/renatols-jf/spboot-chassis-demo)