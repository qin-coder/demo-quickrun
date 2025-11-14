# QuickRun Delivery Platform Demo - Microservices Architecture
A reproduction of the real-world **microservices-based** delivery platform I developed for Shenyang LEDAO Network Technology Co., Ltd. (Oct 2020 - Mar 2022). 
This demo showcases a complete microservices architecture that served 400,000+ users with 100,000+ daily active users before the company ceased operations.

## 🏢 Project Background
-**Original Company**: Shenyang LEDAO Network Technology Co., Ltd.
-**My Role**: Backend Developer (Remote from Germany during Master's studies)
-**Project Period**: October 2020 - March 2022
-**Platform Scale**: 400,000 registered users, ~100,000 DAU

This demo serves as technical proof of my contributions to the original QuickRun platform - a same-city delivery service similar to Uber Eats or DoorDash, where I was primarily responsible for the **Task Service** and **Order Service** development.

## 🔧 Technology Stack

- **Backend**: Java 21, SpringBoot 3.5.7 , Spring Data JPA
- **Message Broker**: RabbitMQ with JSON serialization  
- **Database**: PostgreSQL
- **API**: RESTful APIs
- **Testing**: JMeter for load testing, JUnit for unit tests
- **Deployment**: Docker containerization

## 🏗️ Microservices Architecture
### Architecture Overview
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web Client    │────│   API Gateway    │────│   Order Service │
│    (Vue.js)     │    │  (Spring Cloud)  │    │  (Spring Boot)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │                         │
                              │                         │
                      ┌─────────────────┐    ┌─────────────────┐
                      │   Task Service  │    │  RabbitMQ Broker│
                      │  (Spring Boot)  │    │   (Message Queue)│
                      └─────────────────┘    └─────────────────┘
                              │                         │
                      ┌─────────────────┐    ┌─────────────────┐
                      │   Task DB       │    │   Order DB      │
                      │   (PostgreSQL)  │    │   (PostgreSQL)  │
                      └─────────────────┘    └─────────────────┘
```
---
### Service Responsibilities

| Service             | Port  | Technology | My Responsibility |
|-------------------|-------|-------------------------|---------------------|                                                        
|  **API Gateway**  |8989 | Spring Cloud Gateway | Routing & Load Balancing |
|  **Task Service** |8081 |Spring Boot + JPA	✅ | Primary Developer |
|  **Order Service** |8082 |Spring Boot + JPA	✅ | Primary Developer |
|  **Task Database**  | 15432 |PostgreSQL | Database Design |
|  **Order Database**  | 25432   |PostgreSQL | Database Design |
|  **Message Broker**  |5672   |RabbitMQ | Architecture Design |




## 🏗️  My Microservices Implementation
## Services I Developed
### Task Service (/task-service)

- Delivery task configuration and management
- Dynamic pricing calculation (base fee + per km rate)
- Task availability and status management
- RESTful APIs for task operations

### Order Service (/order-service)

- Complete order lifecycle management
- Real-time order tracking and status updates
- Event-driven architecture with RabbitMQ
- High-concurrency optimizations for 100K+ DAU

## 🐇 RabbitMQ Event-Driven Microservices

### Core Message Queues
I designed and implemented a robust event-driven system using RabbitMQ to handle the throughput requirements of 100,000+ daily active users:

```
// Event Types for Different Business Scenarios
- OrderCreatedEvent → new.orders.queue
- OrderDeliveredEvent → delivered.orders.queue  
- OrderCancelledEvent → cancelled.orders.queue
- OrderErrorEvent → error.orders.queue (dead letter handling)
```
### Key Features I Implemented
#### 1. Performance Monitoring & Metrics

- Real-time processing rate tracking (orders/sec)

- Average processing time monitoring

- Slow processing detection and alerting

- Success/failure rate statistics

#### 2. Error Handling & Recovery

- Dead letter queue configuration for failed messages

- Retry mechanisms for transient failures

- Comprehensive logging and event persistence

## 📊 Microservices Benefits in Production
### Scalability (Handled 100K+ DAU)

- **Independent Scaling**: Task Service and Order Service can scale separately based on load

- **Database Isolation**: Each service has its own database, preventing bottlenecks

- **Async Processing**: RabbitMQ handles peak loads without blocking services

### Resilience

- **Circuit Breaker Pattern**:  Services handle failures gracefully

- **Event Sourcing**: Events are stored for replay and recovery

- **Dead Letter Queues**: Failed messages are handled without data loss

### Development Velocity

- **Team Autonomy**: Different teams can work on different services

- **Technology Flexibility**: Each service can use optimal technology stack

- **Independent Deploymen**: Services can be deployed without affecting others


## 🎯 API Endpoints
### Task Service (My Implementation)
```
# Get all delivery tasks
curl http://localhost:8989/task/api/tasks

# Get active tasks only  
curl http://localhost:8989/task/api/tasks/active

# Create new delivery task
curl -X POST http://localhost:8989/task/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"name":"Express Delivery","baseFee":10.00,"perKmRate":2.00}'
```

### Order Service (My Implementation)
```
# Create delivery order
curl -X POST http://localhost:8989/order/api/orders \
  -H "Content-Type: application/json" \
  -d '{"username":"customer123","customerName":"John Doe","taskId":1,"distanceKm":5.0}'

# Get order list
curl http://localhost:8989/order/api/orders

# Stress test endpoints I built for performance validation
curl http://localhost:8989/order/api/jmeter-test/stats
```
## 🧪 Performance Testing
I implemented comprehensive stress testing to validate system performance by Jmeter under production-like loads:

```
# Async order creation (handles 1000+ concurrent users)
curl -X POST http://localhost:8989/order/api/jmeter-test/create-single-order-async

# Batch processing test
curl -X POST "http://localhost:8989/order/api/jmeter-test/create-batch-orders?batchSize=50"

# Real-time performance metrics
curl http://localhost:8989/order/api/jmeter-test/stats
```

## 📈 Production Metrics & Monitoring
### Original Platform Performance:

- Users: 400,000+ registered users across microservices

- DAU: 100,000+ daily active users

- Throughput: 1,000+ orders/minute during peak hours

- Availability: 99.9% across all services

- Response Time: <200ms for synchronous calls
