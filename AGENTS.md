# Instructions

This project is developed using Java and Spring Boot. The standard guidelines for Java and Spring Boot should be followed, 
unless they are overwritten by guidelines in this file.

## Code Formatting

- Indentation: 4 spaces.
- Blank Lines: Use to separate logical blocks of code.
- Line Length: Maximum 120 characters.
- Use IntelliJ IDEA default code style for Java.

## Java Style

- Use UTF-8 encoding.
- Use descriptive names for classes, methods, and variables.
- Avoid `var` keyword, prefer explicit types.
- Preference for immutability:
  - Avoid mutations of objects, specially when using for-each loops or Stream API using `forEach()`.
  - Avoid magic numbers and strings; use constants instead.
  - Check emptiness and nullness before operations on collections and strings using `net.microfalx.lang.StringUtils` and `net.microfalx.lang.ObjectUtils`.
  - Avoid methods using `throws` clause; prefer unchecked exceptions.
- Avoid comments, unless the business logic is complex and not self-explanatory. If comments are necessary, ensure they are clear and concise.
- Comments could be applied for: cron expressions, Regex patterns, TODOs or given/when/then separation in tests.
- Use `@Override` annotation when overriding methods.
- Avoid Objects.*isNull() and Objects.*nonNull() for one or two variables; prefer direct null checks for better performance.
- Wrap multiple conditions in a boolean variable for better readability
- Prefer early returns.
- Avoid else statements when not necessary and try early returns.

## Lombok Annotations

- Use `@RequiredArgsConstructor` from Lombok for dependency injection via constructor.
- Use `@Slf4j` from Lombok for logging.
- Use `@Builder(setterPrefix = "with"))` for complex object creation.
- Avoid `@Data` annotation; prefer `@Getter` and `@Setter` for granular control.

## Annotations

- **`@Service`**: For business logic classes.
- **`@Repository`**: For data access classes that extend JPA repositories or interact with the database.
- **`@RestController`**: For web controllers.
- **`@Component`**: For generic Spring components.
- **`@Configuration`**: For Spring configuration classes.
- **`@Autowired`**: Prefer constructor injection for production code or when the number of injected fields is small and field injection only for tests.
- **`@ConfigurationProperties`**: For binding related properties avoid multiple `@Value` annotations. From more than 2 properties, consider using this annotation.
- **`@Transactional`**: Only Service classes should be annotated with @Transactional, when needed.
- Circular dependencies should be avoided. Avoid `@Order` annotation for dependency resolution.

## Mappers (As a development team choose MapStruct or strictly static Mappers)


## Exception Handling

- Custom Exceptions: Create custom domain exception classes extending `RuntimeException`.
- Global Exception Handler: Use `@ControllerAdvice` and `@ExceptionHandler` to handle exceptions globally.
- HTTP Status Codes: Map exceptions to appropriate HTTP status codes in REST controllers.
- Error Response Structure: Define a consistent error response structure

## Testing

- Use JUnit 5 for unit and integration testing.
- Use Mockito for mocking dependencies in unit tests.
- Do not add `test` prefix to test method names; instead, use descriptive names that indicate the behavior being tested.
- Use `@WebMvcTest(ControllerClass.class)` for testing Spring MVC controllers.
- Use `@SpringBootTest` for integration tests that require the Spring context.
- Use `given/when/then` structure in test methods for clarity.
- Method naming should follow camelCase convention for test methods (e.g., `getUserByIdOk`, `getUserByIdNotFound`).
- Avoid reflection in tests, when possible.
- Avoid business logic in tests; focus on behavior verification.

## Logging

- Use `@Slf4j` annotation from Lombok for logging to avoid boilerplate code with Logger instances.
- Log at appropriate levels: `DEBUG`, `INFO`, `WARN`, `ERROR`.
- Include contextual information in logs (e.g., request IDs, user IDs).
- Avoid logging sensitive information.
- Use structured logging for better log management.
- Format log messages with placeholders (e.g., `{}`) instead of string concatenation.

## Database Access

- Use Spring Data JPA for database access as much as possible
- For complex queries, use `@Query` annotation with JPQL or native SQL for simple queries in repository interfaces.
- Schema is defined in `resources/sql/TYPE/schema` and data in `resources/sql/TYPE/data` and are automatically executed at application startup (TYPE = mysql for MySQL, postgres for Postgres; default target database is mysql).
- Custom (native) queries are stored in `resources/sql/TYPE/queries` and can be executed using `net.microfalx.bootstrap.jdbc.support.Query` (build on top of Spring Boot JdbcClient) and `net.microfalx.bootstrap.jdbc.support.QueryProvider` to create the database specific queries.
- For complex queries (upsetting, multiple joins, etc.) or in the absence of entities and repositories, always consider using custom native queries stored in `resources/sql/TYPE/queries`
- Use transactions for operations that modify the database, and ensure that they are properly rolled back in case of exceptions.
- Avoid N+1 query problems by using `fetch` joins or `@EntityGraph` 