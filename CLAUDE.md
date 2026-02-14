# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.2 web application (Java 17) using Maven. Artifact: `com.example:first-idea`.

## Build & Run Commands

```bash
# Build
./mvnw clean package

# Run the application (embedded Tomcat, default port 8080)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=FirstIdeaApplicationTests

# Run a single test method
./mvnw test -Dtest=FirstIdeaApplicationTests#contextLoads
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Architecture

- **Entry point**: `FirstIdeaApplication` in `com.example.first_idea` — standard `@SpringBootApplication` bootstrap
- **Web layer**: Spring MVC (`spring-boot-starter-webmvc`) — add `@RestController` or `@Controller` classes under `com.example.first_idea`
- **Static assets**: `src/main/resources/static/`
- **Templates**: `src/main/resources/templates/`
- **Config**: `src/main/resources/application.properties`
