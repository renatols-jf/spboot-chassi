#spboot-chassi

# Discalimer
This project was and is being developed in my free time. This means I'll do my 
best to solve any issues, but a few issues are to be expected. This also means there
is a lot to be done, and a lot that could be better. That being said, 
this is currently in use in a handful of professional microservices without 
any issues. I'll continue to use it as it evolves, and I don't expect any 
downsides to it. Be it as it may, a test suite is needed. Until a comprehensive 
set of tests is written, this project's version will be labeled as less than 1.0.0.

#What is this project?
This is an implementation of a microservice's chassis pattern for spring boot 
applications. It deals with a few common concerns for distributed 
(or not so distributed) applications:

- Logging
- Metrics and monitoring
- Stamp coupling
- Data validation
- Data transformation

#Context
I created this project based on the experiences I had while developing microservices
in the last couple of years or so, and also based on the reading I've done so far.
It's not uncommon for application needs to be ignored in favor of domain behavior, 
and the little code that exists is generally duplicated in many places. Also, I have
seen my fair share of anemic models, and while I understand it's never so simple to
get rid of data holders, I strive to use them the least possible.

The whole framework is based on the idea that all that happens within an application
request is connected to its current context. Data validation is not static, and 
neither is data transformation. Based on that validation and transformations can be
created based on operations.

The idea is to lessen the quantity of boilerplate code written and enable developers
to focus on domain behavior. 