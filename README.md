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