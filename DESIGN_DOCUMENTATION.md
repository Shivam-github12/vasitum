# Interview Scheduling System - Design Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Database Schema](#database-schema)
4. [API Documentation](#api-documentation)
5. [Error Handling](#error-handling)
6. [Race Condition Handling](#race-condition-handling)
7. [Pagination Strategy](#pagination-strategy)
8. [Design Patterns](#design-patterns)
9. [Flow Diagrams](#flow-diagrams)
10. [Trade-offs and Decisions](#trade-offs-and-decisions)

## System Overview

The Interview Scheduling System is a Spring Boot application that allows:
- Interviewers to set their weekly availability
- System to generate interview slots for the next 2 weeks
- Candidates to view and book available slots
- Proper handling of concurrent bookings and race conditions
- Cursor-based pagination for better performance

### Key Features
- **Automatic Slot Generation**: Based on interviewer availability
- **Race Condition Prevention**: Using pessimistic locking
- **Cursor-based Pagination**: Better performance than offset-based
- **Debouncing**: Prevents duplicate submissions
- **Clean Architecture**: Separation of concerns
- **Comprehensive Testing**: Unit and integration tests

## Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│              Presentation Layer          │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │   REST APIs     │ │   Web UI        ││
│  │   Controllers   │ │   Templates     ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│              Business Layer             │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │   Services      │ │   DTOs          ││
│  │   Business      │ │   Validation    ││
│  │   Logic         │ │                 ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│              Data Layer                 │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │   Repositories  │ │   Entities      ││
│  │   JPA           │ │   Database      ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
```

### Technology Stack
- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Frontend**: Thymeleaf, Bootstrap 5, Vanilla JavaScript
- **Testing**: JUnit 5, H2 Database
- **Build Tool**: Maven

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────────┐       ┌─────────────────────┐
│    Interviewer      │       │  AvailabilitySlot   │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │◄─────┤│ id (PK)             │
│ name                │  1:N  ││ interviewer_id (FK) │
│ email (UNIQUE)      │       ││ day_of_week         │
│ max_interviews_week │       ││ start_time          │
│ created_at          │       ││ end_time            │
│ updated_at          │       ││ is_active           │
└─────────────────────┘       └─────────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────────┐
│   InterviewSlot     │
├─────────────────────┤
│ id (PK)             │
│ interviewer_id (FK) │
│ start_time          │
│ end_time            │
│ status              │
│ candidate_name      │
│ candidate_email     │
│ booked_at           │
│ version (Optimistic)│
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

### Table Descriptions

#### Interviewer
- Stores interviewer information and weekly capacity
- `max_interviews_per_week`: Controls booking limits
- Unique email constraint prevents duplicates

#### AvailabilitySlot
- Defines weekly recurring availability patterns
- `day_of_week`: ENUM (MONDAY-SUNDAY)
- `is_active`: Soft delete functionality

#### InterviewSlot
- Generated slots based on availability
- `status`: AVAILABLE, BOOKED, CANCELLED
- `version`: Optimistic locking for race conditions
- Indexes on `start_time`, `status`, `interviewer_id`

## API Documentation

### Base URL: `/api/v1`

### Interviewer Management

#### POST /interviewers
Create a new interviewer with availability slots.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "maxInterviewsPerWeek": 5,
  "availabilitySlots": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00",
      "endTime": "17:00"
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "maxInterviewsPerWeek": 5,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

#### GET /interviewers/{id}
Get interviewer details.

**Response:** `200 OK`

#### GET /interviewers
Get all interviewers.

**Response:** `200 OK`

#### PUT /interviewers/{id}
Update interviewer information.

#### POST /interviewers/{id}/generate-slots
Manually trigger slot generation for an interviewer.

### Interview Slot Management

#### GET /interview-slots/available
Get available interview slots with cursor-based pagination.

**Query Parameters:**
- `cursor` (optional): Base64 encoded cursor for pagination
- `limit` (optional, default=20, max=100): Number of results

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": 1,
      "interviewerName": "John Doe",
      "interviewerEmail": "john@example.com",
      "startTime": "2024-01-15T10:00:00",
      "endTime": "2024-01-15T11:00:00",
      "status": "AVAILABLE"
    }
  ],
  "nextCursor": "eyJpZCI6MX0=",
  "prevCursor": null,
  "hasNext": true,
  "hasPrev": false,
  "size": 1
}
```

#### POST /interview-slots/book
Book an interview slot.

**Request Body:**
```json
{
  "slotId": 1,
  "candidateName": "Jane Smith",
  "candidateEmail": "jane@example.com"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "interviewerName": "John Doe",
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-15T11:00:00",
  "status": "BOOKED",
  "candidateName": "Jane Smith",
  "candidateEmail": "jane@example.com",
  "bookedAt": "2024-01-01T10:30:00"
}
```

#### PUT /interview-slots/{slotId}
Update booking details.

#### DELETE /interview-slots/{slotId}/cancel
Cancel a booking.

#### GET /interview-slots/{slotId}
Get slot details.

#### GET /interview-slots/interviewer/{interviewerId}
Get all slots for an interviewer.

## Error Handling

### Error Response Format
```json
{
  "code": "ERROR_CODE",
  "message": "Human readable message",
  "timestamp": "2024-01-01T10:00:00"
}
```

### Validation Error Format
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "fieldErrors": {
    "name": "Name is required",
    "email": "Invalid email format"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### Error Codes
- `RESOURCE_NOT_FOUND` (404): Resource doesn't exist
- `SLOT_BOOKING_ERROR` (409): Booking conflict or business rule violation
- `CONCURRENT_MODIFICATION` (409): Optimistic locking failure
- `VALIDATION_ERROR` (400): Request validation failed
- `INTERNAL_ERROR` (500): Unexpected server error

## Race Condition Handling

### Problem
Multiple users attempting to book the same slot simultaneously could result in:
- Double bookings
- Data inconsistency
- Poor user experience

### Solutions Implemented

#### 1. Pessimistic Locking
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM InterviewSlot s WHERE s.id = :id")
Optional<InterviewSlot> findByIdWithLock(@Param("id") Long id);
```

#### 2. Optimistic Locking
```java
@Version
private Long version;
```

#### 3. Database Constraints
- Unique constraints on critical fields
- Check constraints for business rules

#### 4. Transactional Boundaries
```java
@Transactional
public InterviewSlotDto bookSlot(BookSlotRequest request) {
    // Atomic operation
}
```

### Race Condition Flow

```
User A                    User B                    Database
  │                        │                          │
  ├─ Request Slot 1        ├─ Request Slot 1         │
  │                        │                          │
  ├─ Acquire Lock ────────────────────────────────────┤
  │                        │                          │
  ├─ Check Availability    │                          │
  │                        │                          │
  ├─ Book Slot            │                          │
  │                        │                          │
  ├─ Release Lock ────────────────────────────────────┤
  │                        │                          │
  │                        ├─ Acquire Lock ──────────┤
  │                        │                          │
  │                        ├─ Check Availability     │
  │                        │   (Already Booked)      │
  │                        │                          │
  │                        ├─ Return Error ──────────┤
```

## Pagination Strategy

### Cursor-based vs Offset-based

#### Offset-based Problems
- Performance degrades with large offsets
- Inconsistent results during data changes
- Not suitable for real-time data

#### Cursor-based Benefits
- Consistent performance
- Stable pagination during data changes
- Better for real-time applications

### Implementation

```java
@Query("SELECT s FROM InterviewSlot s WHERE s.status = 'AVAILABLE' " +
       "AND s.startTime >= :startTime AND s.endTime <= :endTime " +
       "AND (:cursor IS NULL OR s.id > :cursor) " +
       "ORDER BY s.id ASC")
List<InterviewSlot> findAvailableSlotsCursor(
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime,
    @Param("cursor") Long cursor,
    Pageable pageable
);
```

### Cursor Encoding
```java
private String encodeCursor(Long id) {
    return Base64.getEncoder().encodeToString(id.toString().getBytes());
}
```

## Design Patterns

### 1. Repository Pattern
- Abstracts data access logic
- Enables easy testing with mocks
- Separates business logic from data persistence

### 2. Service Layer Pattern
- Encapsulates business logic
- Provides transaction boundaries
- Enables code reuse

### 3. DTO Pattern
- Data transfer between layers
- Input validation
- API versioning support

### 4. Factory Pattern
- Entity creation logic
- Complex object initialization

### 5. Strategy Pattern
- Different pagination strategies
- Multiple booking validation rules

## Flow Diagrams

### Slot Booking Flow

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │    │  Controller │    │   Service   │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │
       ├─ POST /book ────▶│                  │
       │                  │                  │
       │                  ├─ bookSlot() ───▶│
       │                  │                  │
       │                  │                  ├─ Acquire Lock
       │                  │                  │
       │                  │                  ├─ Validate Slot
       │                  │                  │
       │                  │                  ├─ Check Capacity
       │                  │                  │
       │                  │                  ├─ Update Status
       │                  │                  │
       │                  │                  ├─ Release Lock
       │                  │                  │
       │                  │◄─ SlotDto ──────┤
       │                  │                  │
       │◄─ 200 OK ───────┤                  │
       │                  │                  │
```

### Slot Generation Flow

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Interviewer │    │   Service   │    │ Repository  │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │
       ├─ Create ────────▶│                  │
       │                  │                  │
       │                  ├─ Save ─────────▶│
       │                  │                  │
       │                  ├─ Generate Slots │
       │                  │                  │
       │                  │ ┌─ For each day in 2 weeks
       │                  │ │                │
       │                  │ ├─ Check availability
       │                  │ │                │
       │                  │ ├─ Create hourly slots
       │                  │ │                │
       │                  │ └─ Save slots ──▶│
       │                  │                  │
       │◄─ Success ──────┤                  │
       │                  │                  │
```

## Trade-offs and Decisions

### 1. Pessimistic vs Optimistic Locking

**Decision**: Pessimistic locking for booking operations

**Rationale**:
- Booking conflicts are common in scheduling systems
- User experience is better with immediate feedback
- Prevents wasted work on doomed transactions

**Trade-off**: Slightly reduced concurrency for better consistency

### 2. Cursor vs Offset Pagination

**Decision**: Cursor-based pagination

**Rationale**:
- Better performance with large datasets
- Consistent results during data changes
- More suitable for real-time applications

**Trade-off**: More complex implementation, no random page access

### 3. Slot Generation Strategy

**Decision**: Pre-generate slots for 2 weeks

**Rationale**:
- Faster booking response times
- Easier capacity management
- Better user experience

**Trade-off**: Storage overhead, periodic regeneration needed

### 4. Database Choice

**Decision**: MySQL with JPA/Hibernate

**Rationale**:
- ACID compliance for booking operations
- Rich query capabilities
- Mature ecosystem

**Trade-off**: Vertical scaling limitations vs NoSQL horizontal scaling

### 5. Monolithic vs Microservices

**Decision**: Monolithic architecture

**Rationale**:
- Simpler deployment and testing
- Lower operational complexity
- Sufficient for current scale

**Trade-off**: Harder to scale individual components

### 6. Synchronous vs Asynchronous Processing

**Decision**: Synchronous booking with async slot generation

**Rationale**:
- Immediate booking confirmation required
- Slot generation can be delayed
- Simpler error handling

**Trade-off**: Potential blocking on slot generation

## Performance Considerations

### Database Optimization
- Indexes on frequently queried columns
- Connection pooling
- Query optimization

### Caching Strategy
- Application-level caching for static data
- Database query result caching
- CDN for static assets

### Monitoring and Metrics
- Application performance monitoring
- Database performance metrics
- Business metrics (booking success rate)

## Security Considerations

### Input Validation
- Server-side validation for all inputs
- SQL injection prevention via parameterized queries
- XSS prevention in web interface

### Authentication & Authorization
- Future: JWT-based authentication
- Role-based access control
- API rate limiting

### Data Protection
- Sensitive data encryption
- Audit logging
- GDPR compliance considerations

## Deployment and Operations

### Environment Configuration
- Environment-specific properties
- Database migration scripts
- Health check endpoints

### Monitoring
- Application logs
- Performance metrics
- Error tracking

### Backup and Recovery
- Database backup strategy
- Disaster recovery procedures
- Data retention policies

## Future Enhancements

### Short-term
- Email notifications for bookings
- Calendar integration
- Mobile-responsive UI improvements

### Medium-term
- Multi-timezone support
- Advanced scheduling rules
- Analytics dashboard

### Long-term
- Microservices architecture
- Machine learning for optimal scheduling
- Integration with external calendar systems