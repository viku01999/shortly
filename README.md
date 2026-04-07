# shortly
A scalable URL Shortener service built with Java 21, Spring Boot 3.0.5, and PostgreSQL, implementing MVC architecture, DTO/DAO patterns, and Redis caching. Supports short URL generation, redirection, and basic analytics.

# URL Shortener - Phase 1

## Project Description

This is the **Phase 1 implementation** of a URL Shortener service using:

- **Java 21**
- **Spring Boot 3.0.5**
- **PostgreSQL**
- MVC architecture
- DTO/DAO design
- Local development server

**Functionality implemented in Phase 1:**

1. Accept a **long URL** and generate a **short URL**.
2. Redirect users from **short URL → original long URL**.
3. Track clicks for each short URL (basic `click_count`).

**Note:** In Phase 1, caching and advanced scaling are not implemented yet.

---

## Architecture

### MVC Structure

```text
+-----------+      POST /shorten       +-----------------+
|  Client   | --------------------->  | UrlController   |
+-----------+                        +-----------------+
                                           |
                                           v
                                    +-----------------+
                                    | UrlShortenerSvc |
                                    +-----------------+
                                           |
                                           v
                                    +-----------------+
                                    | UrlMappingRepo  |
                                    +-----------------+
                                           |
                                           v
                                    +-----------------+
                                    | PostgreSQL DB   |
                                    +-----------------+

```

# URL Shortener - Phase 2

## Phase 2 Overview

Phase 2 focuses on **database optimization and uniqueness enforcement**.  
Building on Phase 1, we improve:

1. **Database design** with proper indexing
2. **Enforcing uniqueness** for both `long_url` and `short_code`
3. **Optimized queries** for faster lookup (single-query idempotency)
4. **Handling duplicate URL submissions gracefully**
5. Preparing for **caching and high-scale scenarios** in Phase 3

---

## Database Improvements

### Updated Table: `url_mapping`

```sql
CREATE TABLE url_mapping (
    id BIGSERIAL PRIMARY KEY,
    long_url TEXT NOT NULL UNIQUE,
    short_code VARCHAR(10) NOT NULL UNIQUE,
    click_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for fast lookup
CREATE UNIQUE INDEX idx_short_code ON url_mapping(short_code);
CREATE UNIQUE INDEX idx_long_url ON url_mapping(long_url);
```

```text
+-----------+      POST /shorten       +-----------------+
|  Client   | --------------------->  | UrlController   |
+-----------+                        +-----------------+
                                           |
                                           v
                                    +-----------------+
                                    | UrlShortenerSvc |
                                    |  (unique check) |
                                    +-----------------+
                                           |
                                           v
                                    +-----------------+
                                    | UrlMappingRepo  |
                                    |  (indexed)      |
                                    +-----------------+
                                           |
                                           v
                                    +-----------------+
                                    | PostgreSQL DB   |
                                    +-----------------+
                                    
```

# Phase 3 - Caching Layer with Redis

## Objective
- Improve performance for URL redirection
- Reduce PostgreSQL queries using in-memory caching (Redis)

## Implementation
1. Added Redis dependency (`spring-boot-starter-data-redis`)
2. Configured RedisTemplate for `UrlMapping` objects
3. Updated service layer to:
    - Check cache first
    - Fetch from DB if missing
    - Store result in Redis
    - Update click count asynchronously
4. TTL of 1 hour set for cached items

## Flow
```text
[Client Click]
↓
[Redis Cache?] → Yes → Return long URL
↓
No
[PostgreSQL] → Fetch URL
↓
[Store in Redis] → Return long URL
```

## Key Points
- Redis key = `short_code`
- Redis value = `UrlMapping` object
- Click count updates can be async to reduce latency
- Cache reduces DB load for popular URLs

---

# Phase 3 - Caching Layer with Redis

## Objective
- Reduce DB load for frequent redirects
- Improve URL redirection performance using in-memory caching

## Implementation Steps
1. Added Redis dependency (`spring-boot-starter-data-redis`)
2. Configured `RedisTemplate` for `UrlMapping` objects
3. Updated service layer:
   - Check Redis cache first
   - If cache miss → fetch from PostgreSQL
   - Store in Redis with TTL
   - Update click count asynchronously
4. Optional: Embedded Redis for development/testing

## Configuration
- Redis server running at `localhost:6379`
- TTL for cached entries: 1 hour
- Keys: `short_code`, Values: `UrlMapping`

## Flow Diagram

```text
[Client Click]
↓
[Redis Cache?] → Yes → Return long URL
↓
No
[PostgreSQL] → Fetch URL
↓
[Store in Redis] → Return long URL
```


## Key Points
- Redis reduces DB calls for popular short URLs
- Click count updates are async to improve latency
- Supports both embedded Redis (dev) and standalone Redis (prod)

## Redis
- Install redis and setup redis and run on port 
- `localhost:6379`

---

URL Shortener Project – Interview Cheat Sheet
1️⃣ System Overview
Function: Convert long URLs to short, unique URLs and redirect users.
Stack: Java 21 + Spring Boot 3.0.5, PostgreSQL, Redis.
Architecture:
Client
│
▼
Controller (Spring Boot)
│
▼
Service Layer
├─ Check Redis Cache
│      ├─ CACHE HIT → return URL
│      └─ CACHE MISS → query PostgreSQL
│
▼
PostgreSQL DB (with indexes)
│
└─ Response sent back & cached in Redis
Database: url_mapping table
Columns: id, long_url (unique), short_code (unique), click_count, created_at
Indexes on long_url and short_code (B-Tree) for fast lookup
Caching: Redis stores short_code → UrlMapping with TTL = 1 hour
2️⃣ Key Interview Questions & Answers
Q1: How do you generate a unique short URL?

A:

Used UUID.randomUUID().toString().substring(0,8) for short codes.
Ensured uniqueness by checking existsByShortCode(shortCode) before saving.
Q2: How do you avoid duplicate long URLs?

A:

Added a unique constraint on long_url in PostgreSQL.
First, check with existsByLongUrl(longUrl); if exists, return the existing short code.
Q3: Why use Redis caching?

A:

Reduce DB load and improve performance for frequent lookups.
Cache short_code → UrlMapping objects in Redis.
TTL = 1 hour to avoid stale data.
Q4: How does your caching logic work?

A:

Check Redis with short_code.
If exists → CACHE HIT → return URL.
If not → CACHE MISS → query DB → store result in Redis.
Logs added: “CACHE HIT” or “CACHE MISS”.
Q5: What is the role of database indexing?

A:

long_url and short_code are indexed (B-Tree).
Indexes allow O(log N) search instead of scanning full table.
Ensures queries like findByShortCode are fast even with millions of records.
Q6: Explain your DB schema.

A:

Column	Type	Notes
id	BIGSERIAL	Primary key
long_url	TEXT	UNIQUE, indexed
short_code	VARCHAR(8)	UNIQUE, indexed
click_count	INT	Defaults 0
created_at	TIMESTAMP	Default current_timestamp
Q7: How is Redis integrated in Spring Boot?

A:

spring-boot-starter-data-redis dependency.
RedisTemplate<String,Object> bean defined with JSON serializer.
Service layer uses it to store/fetch UrlMapping.
Connection pool configured in application.yml.
Q8: How do you ensure thread-safety?

A:

Redis operations are atomic.
UUID generation + DB unique constraint ensures no collisions.
Redis connection pool ensures multiple threads can read/write safely.
Q9: What are the advantages of this system?
Fast URL lookup due to Redis caching + DB indexing
Unique short codes for consistency
Scalable – can handle high read traffic with Redis
TTL prevents stale data accumulation
Q10: How would you scale this for millions of URLs?

A:

Shard the database or use multiple DB replicas.
Use distributed Redis cluster.
Use custom short code generation (e.g., Base62 with incremental counter) for better space utilization.
Add rate limiting and monitoring metrics.
Q11: How would you measure cache effectiveness?

A:

Track cache hit/miss logs in service.
Monitor Redis metrics (hit rate, memory usage).
Calculate: hit ratio = cache hits / (cache hits + misses)
Q12: How do you handle collisions?

A:

Even though UUID substring is highly unique, we check DB for existing short_code.
If collision occurs, generate a new UUID substring.
Q13: How do you ensure maintainability?

A:

DTOs separate API layer from DB entities.
Service layer handles business logic.
Repository layer handles DB operations.
Redis cache separated from DB logic.
Q14: Can you describe your RedisTemplate configuration?
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
RedisTemplate<String, Object> template = new RedisTemplate<>();
template.setConnectionFactory(factory);
template.setKeySerializer(new StringRedisSerializer());
template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
return template;
}
Keys stored as strings
Values serialized to JSON
Ensures objects are stored efficiently in Redis
Q15: Optional improvements you can discuss
Use @Cacheable for cleaner caching
Track metrics like cache hit ratio
Implement eviction policies (LRU) for high traffic
Add rate limiting to prevent abuse
3️⃣ Postman Example

Shorten URL:

POST http://localhost:6690/api/url
Content-Type: application/json

{
"longUrl": "https://example.com/some/long/url"
}

Response:

{
"shortUrl": "http://localhost:6690/api/url/8c73c4c9"
}

Redirect URL:

GET http://localhost:6690/api/url/8c73c4c9
First call → CACHE MISS
Subsequent calls → CACHE HIT
✅ 4️⃣ Interview Talking Points
Explain DB design, indexing, unique constraints
Explain Redis caching, TTL, cache hit/miss logs
Demonstrate service flow: DTO → DAO → Repository → Service → Controller
Mention optional improvements for scalability and monitoring
Show Postman request examples and CLI verification