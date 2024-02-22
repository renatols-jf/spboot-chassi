# spboot-chassis

# Discalimer
This project was and is being developed in my free time. This means I'll do my 
best to solve any issues, but a few issues are to be expected. This also means there
is a lot to be done, and a lot that could be better. That being said, 
this is currently in use in a handful of professional microservices without 
any issues. I'll continue to use it as it evolves, and I don't expect any 
downsides to it. Be it as it may, a test suite is needed. Until a comprehensive 
set of tests is written, this project's version will be labeled as less than 1.0.0.

Also, this project does not currently support NIO as it uses ThreadLocal to control
the request context. A simple solution is to add methods to take a snapshot of
the context and reinitialize after the NIO code. A more automatic solution would
require greater effort. Be it as it may, the snapshot is also not supported yet.

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
    <version>0.0.2</version>
</dependency>
```

or your build.gradle if using Gradle
```
implementation group: 'io.github.renatols-jf', name: 'spboot-chassis', version: '0.0.2'
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
There are two ways of doing so:
- `this.requestResource(MyService.class)` in which you provide the class you are expecting. 
  This is a syntatic sugar available only inside requests.
- `AppRegistry.getResource(MyService.class)` in which you provide the class you are expecting.
  This can be called anywhere.

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
duration.

*Entries are logged in their own fields; exportation of fields other than message
depends on the logging configuration.

## Context
[Context](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/Context.java)
is the source of information for the current processing/request. It stores 
information like the operation in execution and the transactionId. It is
used by the framework to initiate transformations and validations. It also stores
information like elapsed time by type, which will be treated later in this
document.

It is initialized automatically as soon as a request object is created and
it's detroyed as soon as a request finishes. A context can be obtained
anywhere calling `Context.forRequest`
As a rule of thumb, we don't want to initialize the context manually elsewhere, 
so calling `Context.initialize` outside of a request will result in an error.
You also cannot call it manually inside a request, as it will result in an error
since the context already exists.

If you absolutely need to create a context outside of a request, the class in which
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
      its information. You can create yours or use one of the available:
        1. [HiddenClassifiedCypher](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/data/cypher/HiddenClassifiedCypher.java)
          which will print only if the field has a value or not.
        2. [IgnoringCypher](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/context/data/cypher/IgnoringCypher.java) 
          which will completely ignore that field.

Fields added via `attach` will **NOT** be exported in their own fields. Intead, they will be groupped
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
`Media#forkCollection`

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
  that will transform the field value:
  `@RenderConfig(transformer = @RenderTransform(MyTransformer.class))`
  
Currently, there is no way to print nested renderables or a collection 
of nested renderables - this will be add in a future release. 
They should be configured not to be printed or some sort of workaround should
be done. You can override the `FieldRenderable` default behavior as follows:

```
package com.example.demo;

import io.github.renatolsjf.chassis.rendering.FieldRenderable;
import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.Renderable;
import io.github.renatolsjf.chassis.rendering.config.RenderConfig;
import io.github.renatolsjf.chassis.rendering.config.RenderPolicy;
import io.github.renatolsjf.chassis.rendering.config.RenderTransform;

import java.util.ArrayList;
import java.util.List;

public class Greeter implements FieldRenderable {

    private final String name;
    @RenderConfig(policy = @RenderPolicy(RenderPolicy.Policy.IGNORE))
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
        return FieldRenderable.super.render(media)
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

### Media
It's the means by which the information will be recorded. You never create a
Media object, instead you call `Media.ofRenderable`, `Media.ofCollection`, or
`Media.empty`. A call to `Media#render` actually exports the object information.

This is an area that can be improved upon. Render returns an `Object`, which is either
a `Map` for single instances or a `List` for collections. Spring Boot will 
return this without any issues, but a more powerful abstraction is a good idea.

### Transforming
Transforming is the means by which an output is transformed into another. It can be
called manually or initialized automatically according to the context. Currently,
the only context transformation available is the projection one.

Transformations can be done using 
[TransformingPath](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/transforming/TransformingPath.java),
an implementation of [MediaTransformer](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/MediaTransformer.java),
and [MediaContent](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/rendering/MediaContent.java).
This is a part of the framework that needs to evolve, so I'll not write more 
information on this topic at the moment. An example will be available in the
sample project, though.

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
- `minimum`([@Minimum](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/Minimum.java)):
  indicates the minimum value of the field - `@Validation(minimum = @Minimum(10))`. 
  Currently, a minimum value of 0 disables the validation. In a future release, 
  this will be changed to `Integer.MIN_VALUE`.
- `oneOf` ([@OneOf](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/OneOf.java)):
  indicates that a field's value must be equal to one of the provided values -
  `@Validation(oneOf = @OneOf({"Ryan", "Andrew"}))`. Currently, it supports only
  `String` values, but it will be expanded in a future release.
- `pattern`([@Pattern](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/validation/annotation/Pattern.java))  :
  indicates that a `String` must match a provided Regex -
  `@Validation(pattern = @Pattern("^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$"))`
  
Every validation type also accepts a message parameter, which will override the 
default error message in case of a validation error.

## API integrations - HTTP(s) calls
Each HTTP request should be made using the 
[RestOperation](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/integration/RestOperation.java) 
class. To create a `RestOperation` you should call `RestOperation#create` 
providing the following parameters:

- `group`: It's used to identify to whom the service being called belongs.
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
In the event of an error, either a 
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
  It uses as a `tag`: the `group` for the `RestOperation`, the `service` for the `RestOperation`
  the `operation` for the `RestOperation`, `outcome` which is either an `Http Status Code` or
  `connection_error`, and `type`, which is always `rest`.


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
integration_request_time_bucket{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="200.0",} 0.0
integration_request_time_bucket{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="500.0",} 0.0
integration_request_time_bucket{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="1000.0",} 1.0
integration_request_time_bucket{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="2000.0",} 1.0
integration_request_time_bucket{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="5000.0",} 1.0
integration_request_time_bucket{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="10000.0",} 1.0
integration_request_time_bucket{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",le="+Inf",} 1.0
integration_request_time_count{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",} 1.0
integration_request_time_sum{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",} 760.0
integration_request_time_max{group="GOOGLE",operation="SEARCH",outcome="200",service="SEARCH",type="rest",} 760.0
```

## Built-in health information
The application has a default `HealthRequest` that exports health information in `json`.
It exports health percentage, request count, quantiles for the time taken for each type, and
result count by type. It does so by each operation, and also aggregates as application information.
The application health is not an average. Instead, it reflects the health of the worst operation.

```
{
   "application":{
      "health":100.0,
      "load":{
         "requestCount":1,
         "requestTime":{
            "internal":{
               "quantiles":{
                  "0.5":13,
                  "0.95":13,
                  "0.99":13
               }
            }
         }
      },
      "result":{
         "success":1,
         "clientError":0,
         "serverError":0
      }
   },
   "operations":[
      {
         "name":"DEMO_OPERATION",
         "health":100.0,
         "load":{
            "requestCount":1,
            "requestTime":{
               "internal":{
                  "quantiles":{
                     "0.5":13,
                     "0.95":13,
                     "0.99":13
                  }
               }
            }
         },
         "result":{
            "success":1,
            "clientError":0,
            "serverError":0
         }
      }
   ]
}
```

## Timing operations and timer classification
The framework enables us to classify the time taken while processing an operation. 
The classification is done using a simple `String` as a `Tag`. To measure the time for a block of code, 
we use [TimedOperation](https://github.com/renatols-jf/spboot-chassis/blob/master/src/main/java/io/github/renatolsjf/chassis/monitoring/timing/TimedOperation.java).

A `TimedOperation` will record the time it took automatically to the `Context`. 
You can tag the `TimedOperation` however you like, using `new TimedOperation(myTag)`, or using one of two
pre-defined tags: http `TimedOperation.http()`, used for HTTP calls, and db `TimedOperation.db()`,
used for database calls. 

`TimedOperation.http()` is used internally in `RestOperation`, and
`TimedOperation.db()` has to manually wrap a database call. It's not uncommon for database calls
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
`java.lang.Runnable` and has no return type, while `execute` expects a `TimedOperation.Executable` 
and returns whatever type the generic call was made with. In a future release, 
`TimedOperation.Executable` will be dropped in favor of `java.concurrent.Callable`. 

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

## Configuration
Although a configuration module exists and is accessible via `Chassis.getInstance().getConfig()`,
currently no changes to the configurations can be made. Be it as it may, the following 
configurations are in use:

- `useCallingClassNameForLogging`: Defaults to `true`. Governs whether the stack trace will be 
  used or not to initialize the logging class. When creating an `ApplicationLogger`, 
  you can provide a class or not to be exported as the logger name. In case where no class is provided,
  as in `Context.forRequest().createLogger()`, this configuration will be used to initialize
  the class. If it's true, the class will be the calling class. If it's false, it will
  always be `ApplicatonLogger`.
  
- `printLoggingContextAsJson`: Defaults to `true`. Governs whether the `context` field present
  in log messages will be exported as `json`.
  
- `allowDefaultLoggingAttributesOverride`: Defaults fo `false`. Governs whether automatic logging
  attributes, such as `transactionId`, can have their value replaced with a call for 
  `Context#withRequestContextEntry` as in
  `Context.forRequest.withRequestContextEntry("transactionId", aNewValue)`.
  
- `validatorFailOnExecutionError`: Defaults to `true`. Governs whether an unexpected error
  in a validation attempt results in an exception. Every validation that fails for not matching
  the annotations for the current operation will result in an exception. For whatever
  reason, an exception can happen in the middle of the validation attempt. Let's say a method
  that is being validated throws an exception. If this configuration is true, the validation 
  will not happen, and an exception will be thrown. If this configuration is false, the validation
  will not happen, but it will be ignored, and if no other validation fails, the `Validatable`
  would be deemed valid.
  
- `forbidUnauthorizedContextCreation`: Defatuls to `true`. `Context.initialize` can't be called
  anywhere to create a `Context`. Allowing the `Context` creation is error-prone and, in almost
  all cases, not needed. If this configuration is true, unless the class in which 
  `Context.initialize` is being called is annotated with `@ContextCreator`, an exception will
  be thrown.

- `allowContextCorrelationIdUpdate`: Defaults to `true`. Governs whether a `correlationId` can be
  updated after the context has been initialized.
  
- `monitoringRequestDurationRanges`: Defaults to `new double[]{200, 500, 1000, 2000, 5000, 10000}`.
  Govern the default `Histogram` buckets for request duration in milliseconds.
  
- `exportRequestDurationMetricByType`: Defaults to `true`. Governs whether `Histogram` metrics
  for request duration will be tagged with timer types.
  
- `healthTimeWindowDuration`: Defaults to 5 minutes. Governs the maximum age of requests
  to be used in health calculations.

# Next Steps
A few future updates have been thought of. Having said that, this does not neither represent
an order of priority nor a commitment. These are just a few items that might be done
in the future.

- Enable configuration changes.
- Create a configuration to not log stack traces.
- Create a configuration to log attached fields in their own fields.
- Allow for `FieldRenderable` to print nested `Renderable` objects.
- Replace `@Minimum` ignore value from 0 to `Integer.MIN_VALUE`
- Allow for `@OneOf` to accept values other than `String`.
- Create further validations, such as the maximum value permitted, and the minimum and the maximum size.
- Create a configuration to have some level of control in automatic logs and metrics.  
- Remove the need to call `.log()` for messages that have no attachment.  
- Evolve the way this framework interacts with Spring to remove the need for `@ComponentScan`
- Replace `TimedOperation.Executable` with `java.concurrent.Callable`.
- Create a configuration to calculate application health as a media instead of worst.
- Allow extra tags in automatic metrics.
- Create a configuration to stop the timer as soon as the domain logic is over (`Request#doProcess`)
- Create a summary type metric;
- Create some kind of label structure to override default names for metrics, tags, and logging fields.
- Allow request duration metrics to be collected in measurements different from milliseconds.  
- Create mechanism to work with Java NIO.
- Implement distributed tracing.
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
  
# Sample project
To come. Does not exist yet.