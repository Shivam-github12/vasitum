# Interview Scheduling System

A comprehensive Spring Boot application for managing interview scheduling with automatic slot generation, race condition handling, and cursor-based pagination.

## Features

- ✅ **Interviewer Management**: Create and manage interviewers with weekly availability
- ✅ **Automatic Slot Generation**: Generate interview slots for next 2 weeks based on availability
- ✅ **Slot Booking**: Candidates can view and book available slots
- ✅ **Race Condition Handling**: Pessimistic locking prevents double bookings
- ✅ **Cursor-based Pagination**: Better performance than offset-based pagination
- ✅ **Debouncing**: Prevents duplicate form submissions
- ✅ **Clean Architecture**: Proper separation of concerns
- ✅ **Comprehensive Testing**: Unit and integration tests
- ✅ **Basic Web UI**: Simple interface for demonstration
- ✅ **Error Handling**: Proper HTTP status codes and error messages

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Frontend**: Thymeleaf, Bootstrap 5, Vanilla JavaScript
- **Testing**: JUnit 5, H2 Database (for tests)
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

## Setup Instructions

### 1. Database Setup

Create a MySQL database:
```sql
CREATE DATABASE interview_scheduler;
CREATE USER 'scheduler_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON interview_scheduler.* TO 'scheduler_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Application Configuration

Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/interview_scheduler?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=scheduler_user
spring.datasource.password=password
```

### 3. Build and Run

```bash
# Clone or navigate to the project directory
cd vasitum

# Build the project
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## API Endpoints

### Interviewer Management
- `POST /api/v1/interviewers` - Create interviewer
- `GET /api/v1/interviewers` - Get all interviewers
- `GET /api/v1/interviewers/{id}` - Get interviewer by ID
- `PUT /api/v1/interviewers/{id}` - Update interviewer
- `POST /api/v1/interviewers/{id}/generate-slots` - Generate slots

### Interview Slot Management
- `GET /api/v1/interview-slots/available` - Get available slots (with pagination)
- `POST /api/v1/interview-slots/book` - Book a slot
- `PUT /api/v1/interview-slots/{id}` - Update booking
- `DELETE /api/v1/interview-slots/{id}/cancel` - Cancel booking
- `GET /api/v1/interview-slots/{id}` - Get slot details
- `GET /api/v1/interview-slots/interviewer/{id}` - Get slots by interviewer

## Web Interface

- **Main Page** (`/`): View and book available slots
- **Admin Panel** (`/admin`): Manage interviewers and generate slots

## Usage Examples

### 1. Create an Interviewer

```bash
curl -X POST http://localhost:8081/api/v1/interviewers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "maxInterviewsPerWeek": 5,
    "availabilitySlots": [
      {
        "dayOfWeek": "MONDAY",
        "startTime": "09:00",
        "endTime": "17:00"
      },
      {
        "dayOfWeek": "TUESDAY",
        "startTime": "09:00",
        "endTime": "17:00"
      }
    ]
  }'
```

### 2. Get Available Slots

```bash
curl "http://localhost:8081/api/v1/interview-slots/available?limit=10"
```

### 3. Book a Slot

```bash
curl -X POST http://localhost:8081/api/v1/interview-slots/book \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "candidateName": "Jane Smith",
    "candidateEmail": "jane@example.com"
  }'
```

## Testing

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=InterviewSlotServiceTest
```

## Architecture Highlights

### Clean Architecture
- **Controllers**: Handle HTTP requests/responses
- **Services**: Business logic and transactions
- **Repositories**: Data access layer
- **Entities**: Domain models
- **DTOs**: Data transfer objects

### Race Condition Prevention
- Pessimistic locking for booking operations
- Optimistic locking with versioning
- Transactional boundaries

### Pagination Strategy
- Cursor-based pagination for better performance
- Base64 encoded cursors
- Consistent results during data changes

### Error Handling
- Global exception handler
- Proper HTTP status codes
- Structured error responses

## Design Patterns Used

1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic encapsulation
3. **DTO Pattern**: Data transfer between layers
4. **Factory Pattern**: Entity creation
5. **Strategy Pattern**: Different pagination approaches

## Performance Considerations

- Database indexes on frequently queried columns
- Connection pooling
- Cursor-based pagination
- Pessimistic locking only where necessary

## Security Features

- Input validation with Bean Validation
- SQL injection prevention via JPA
- XSS prevention in templates
- Proper error handling without information leakage

## Monitoring and Logging

- Structured logging with SLF4J
- Debug logging for development
- Error tracking and reporting

## Future Enhancements

- JWT-based authentication
- Email notifications
- Calendar integration
- Multi-timezone support
- Analytics dashboard
- Mobile app

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions or issues, please create an issue in the repository or contact the development team.