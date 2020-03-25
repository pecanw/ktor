<img src="http://ktor.io/assets/images/ktor_logo.png" alt="Ktor" width="600" style="max-width:100%;">

[![Official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Download](https://api.bintray.com/packages/kotlin/ktor/ktor/images/download.svg) ](https://bintray.com/kotlin/ktor/ktor/_latestVersion)
[![TeamCity Build](https://img.shields.io/teamcity/http/teamcity.jetbrains.com/s/KotlinTools_Ktor_BuildGradle.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=KotlinTools_Ktor_BuildGradle&branch_KotlinTools_Ktor=%3Cdefault%3E&tab=buildTypeStatusDiv)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

Ktor is a framework for quickly creating web applications in Kotlin with minimal effort.

```kotlin
import io.ktor.server.netty.*
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.server.engine.*

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("Hello, world!", ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}
```

* Runs embedded web server on `localhost:8080`
* Installs routing and responds with `Hello, world!` when receiving a GET http request for the root path

## Principles

#### Unopinionated

Ktor Framework doesn't impose a lot of constraints on what technology a project is going to use – logging, 
templating, messaging, persistent, serializing, dependency injection, etc. 
Sometimes it may be required to implement a simple interface, but usually it is a matter of writing a 
transforming or intercepting function. Features are installed into the application using a unified *interception* mechanism
which allows building arbitrary pipelines. 

Ktor Applications can be hosted in any servlet container with Servlet 3.0+ API support such as Tomcat, or 
standalone using Netty or Jetty. Support for other hosts can be added through the unified hosting API.

Ktor APIs are mostly functions calls with lambdas. Thanks to Kotlin DSL capabilities, the code looks declarative. 
Application composition is entirely up to the developer's choice – with functions or classes, using dependency injection 
framework or doing it all manually in the main function. 

#### Asynchronous

The Ktor pipeline machinery and API are utilising Kotlin coroutines to provide easy-to-use asynchronous 
programming model without making it too cumbersome. All host implementations are using asynchronous I/O facilities
to avoid thread blocking. 

#### Testable

Ktor applications can be hosted in a special test environment, which emulates a web server to some 
extent without actually doing any networking. It provides easy way to test an application without mocking 
too much stuff, and still achieve good performance while validating application calls. Running integration tests with a real 
embedded web server are of course possible, too.

## Documentation

Please visit [ktor.io](http://ktor.io) for Quick Start and detailed explanations of features, usage and machinery.

* Getting started with [Gradle](http://ktor.io/quickstart/gradle.html) 
* Getting started with [Maven](http://ktor.io/quickstart/maven.html) 
* Getting started with [IDEA](http://ktor.io/quickstart/intellij-idea.html) 

## Inspirations

Kotlin web frameworks such as Wasabi and Kara, which are currently deprecated.

## Reporting Security Vulnerabilities

If you find a security vulnerability in Ktor, we kindly request that instead of using our public issue tracker, you instead reach out to the JetBrains security team via our [responsible disclosure process](https://www.jetbrains.com/legal/terms/responsible-disclosure.html).

## Contributing

Please see [the contribution guide](CONTRIBUTING.md) and the [Code of conduct](CODE_OF_CONDUCT.md) before contributing.
