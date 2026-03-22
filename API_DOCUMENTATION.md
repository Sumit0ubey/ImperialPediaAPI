# 📡 Imperial Pedia API - Complete API Documentation

<div align="center">

![API Documentation](https://img.shields.io/badge/API-Documentation-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0.0-orange?style=for-the-badge)

**Comprehensive guide to all Imperial Pedia REST endpoints, request/response formats, and validation rules.**

[⬅️ Back to README](./README.md)

</div>

---

## 📑 Table of Contents

1. [🌐 Overview](#-overview)
2. [📊 Global Response Format](#-global-response-format)
3. [🔐 Authentication](#-authentication)
4. [📥 Retrieval Endpoints](#-retrieval-endpoints)
5. [✍️ Creation Endpoint](#️-creation-endpoint)
6. [🔄 Update Endpoints](#-update-endpoints)
7. [🎛️ Status Management](#️-status-management)
8. [🗑️ Deletion Endpoint](#️-deletion-endpoint)
9. [📦 Data Models](#-data-models)
10. [✔️ Validation Rules](#️-validation-rules)
11. [⚠️ Error Handling](#️-error-handling)
12. [💡 Examples](#-examples)
13. [🗄️ Database Constraints](#️-database-constraints)
14. [📈 HTTP Status Codes](#-http-status-codes)
15. [🗂️ Changelog](#️-changelog)

---

## 🌐 Overview

**Imperial Pedia API** is a RESTful Spring Boot application for managing a comprehensive database of terms with support for categorization, relationships, and multiple status states.

| Property | Value |
|----------|-------|
| **Base URL** | `http://localhost:8080` |
| **API Version** | 1.0.0-SNAPSHOT |
| **Content Type** | `application/json` |
| **Authentication** | None (Public API) |
| **Rate Limiting** | None |

---

## 📊 Global Response Format

All endpoints return responses wrapped in the standard `ApiResponse<T>` envelope:

### Success Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Operation successful",
  "data": { 
    /* response payload - type depends on endpoint */
  },
  "timestamp": "2026-03-22T11:13:15"
}
```

### Error Response

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Error description",
  "timestamp": "2026-03-22T11:13:15"
}
```

---

## 🔐 Authentication

**Status:** ✅ **No Authentication Required**

All endpoints are publicly accessible. Authentication will be added in future versions using Spring Security.

---

## 📥 Retrieval Endpoints

### 1.1 Get Published Terms by Letter

Get all published terms that start with a specific letter.

```http
GET /terms/letter/{letter}
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `letter` | String | ✅ Yes | Single letter or word (e.g., "A", "Star", "Jedi") |
| | | | Case-insensitive, normalized to UPPERCASE |

#### ✅ Success Response (200)

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Terms retrieved successfully",
  "data": [
    {
      "title": "anakin skywalker",
      "slug": "anakin-skywalker",
      "content": "Jedi Knight who fell to the dark side...",
      "seoTitle": "Anakin Skywalker - Imperialpedia",
      "seoDescription": "Biography of Anakin Skywalker",
      "featuredImageUrl": "https://example.com/anakin.jpg",
      "categoryNames": ["characters", "jedi"],
      "relatedTerms": [
        {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "slug": "luke-skywalker",
          "title": "luke skywalker",
          "featuredImageUrl": "https://example.com/luke.jpg"
        }
      ]
    }
  ],
  "timestamp": "2026-03-22T11:13:15"
}
```

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `400` | Letter is null or blank |
| `404` | No published terms found for that letter |

---

### 1.2 Get Archived Terms (Optional Letter Filter)

Retrieve all archived terms, optionally filtered by letter.

```http
GET /terms/archived[?letter=X]
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `letter` | String | ❌ No | Optional letter filter (e.g., "?letter=A") |
| | | | If blank/missing, returns all archived terms |

#### ✅ Success Response (200)

Same structure as endpoint 1.1 but with archived terms.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | No archived terms found |

---

### 1.3 Get Draft Terms (Optional Letter Filter)

Retrieve all draft terms, optionally filtered by letter.

```http
GET /terms/draft[?letter=X]
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `letter` | String | ❌ No | Optional letter filter (e.g., "?letter=D") |

#### ✅ Success Response (200)

Same structure as endpoint 1.1 but with draft terms.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | No draft terms found |

---

### 1.4 Get Term by Slug

Retrieve a published term by its unique slug.

```http
GET /terms/slug/{slug}
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `slug` | String | ✅ Yes | Unique slug (lowercase, hyphen-separated) |
| | | | Case-insensitive; normalized to lowercase |

#### ✅ Success Response (200)

Full TermDetailResponse with categories and related terms.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `400` | Slug is null or blank |
| `404` | Term not found with this slug |

---

### 1.5 Get Term by UUID

Retrieve a published term by its unique UUID.

```http
GET /terms/{id}
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `id` | UUID | ✅ Yes | Term UUID (e.g., "550e8400-e29b-41d4-a716-446655440000") |

#### ✅ Success Response (200)

Full TermDetailResponse with categories and related terms.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `400` | Invalid UUID format |
| `404` | Term not found with this ID |

---

## ✍️ Creation Endpoint

### 2.1 Create New Term

Create a new term with complete metadata, categories, and related terms.

```http
POST /terms/create
Content-Type: application/json
```

#### 📥 Request Body

```json
{
  "title": "Darth Vader",
  "slug": "darth-vader",
  "content": "Anakin Skywalker turned to the dark side...",
  "seoTitle": "Darth Vader - Dark Lord of the Sith",
  "seoDescription": "Learn about Darth Vader, the Sith Lord",
  "featuredImageUrl": "https://example.com/darth-vader.jpg",
  "status": "DRAFT",
  "categoryNames": ["Characters", "Sith", "Villains"],
  "relatedTerms": ["anakin-skywalker", "luke-skywalker"],
  "relatedTermIds": ["uuid1", "uuid2"]
}
```

#### 📝 Field Specifications

| Field | Type | Required | Constraints |
|-------|------|----------|-----------|
| `title` | String | ✅ | Max 255 chars, cannot be blank |
| `slug` | String | ✅ | Max 255 chars, unique, lowercase hyphen-separated |
| `content` | String | ✅ | Max 255 chars, cannot be blank |
| `seoTitle` | String | ❌ | Max 255 chars, optional |
| `seoDescription` | String | ❌ | Max 500 chars, optional |
| `featuredImageUrl` | String | ❌ | Max 500 chars, URL format |
| `status` | Enum | ❌ | DRAFT, PUBLISHED, ARCHIVED (default: DRAFT) |
| `categoryNames` | String[] | ❌ | Max 20 items, auto-created if missing |
| `relatedTerms` | String[] | ❌ | Max 20 slugs, must exist in DB |
| `relatedTermIds` | UUID[] | ❌ | Max 20 UUIDs, must exist in DB |

#### ✅ Success Response (201)

```json
{
  "success": true,
  "statusCode": 201,
  "message": "Term created successfully",
  "data": {
    "title": "darth vader",
    "slug": "darth-vader",
    "content": "anakin skywalker turned to the dark side...",
    "seoTitle": "darth vader - dark lord of the sith",
    "seoDescription": "learn about darth vader, the sith lord",
    "featuredImageUrl": "https://example.com/darth-vader.jpg",
    "categoryNames": ["characters", "sith", "villains"],
    "relatedTerms": [
      {
        "id": "uuid",
        "slug": "anakin-skywalker",
        "title": "anakin skywalker",
        "featuredImageUrl": "https://..."
      }
    ]
  },
  "timestamp": "2026-03-22T11:13:15"
}
```

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `400` | Validation failed (blank fields, invalid format) |
| `409` | Slug already exists |
| `404` | Related term slug/ID not found |

---

## 🔄 Update Endpoints

### 3.1 Full Update (PUT)

Completely replace a term with new data (all required fields must be provided).

```http
PUT /terms/update/{id}
Content-Type: application/json
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `id` | UUID | ✅ Yes | UUID of the term to update |

#### 📥 Request Body

Same as Create endpoint (all required fields mandatory).

#### ✅ Success Response (200)

Full TermDetailResponse after update.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | Term not found |
| `409` | Slug already exists (for different term) |
| `400` | Validation failed |

---

### 3.2 Partial Update (PATCH)

Update only the fields provided (optional fields can be omitted).

```http
PATCH /terms/update/{id}
Content-Type: application/json
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `id` | UUID | ✅ Yes | UUID of the term to patch |

#### 📥 Request Body (All Optional)

```json
{
  "title": "Updated Title",
  "slug": "updated-slug",
  "content": "Updated content",
  "seoTitle": "Updated SEO Title",
  "seoDescription": "Updated SEO Description",
  "featuredImageUrl": "https://new-image-url.jpg",
  "status": "PUBLISHED",
  "categoryNames": ["NewCategory"],
  "relatedTerms": ["slug1"],
  "relatedTermIds": ["uuid1"]
}
```

#### 📝 Patchable Fields

| Field | Type | Effect |
|-------|------|--------|
| `title` | String | Updates term name |
| `slug` | String | Updates unique identifier |
| `content` | String | Updates description |
| `seoTitle` | String | Updates SEO title |
| `seoDescription` | String | Updates SEO description |
| `featuredImageUrl` | String | Updates image URL |
| `status` | Enum | Updates status |
| `categoryNames` | String[] | Replaces all categories |
| `relatedTerms` | String[] | Replaces all related terms |
| `relatedTermIds` | UUID[] | Replaces all related terms |

#### ✅ Success Response (200)

Full TermDetailResponse after patch.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | Term not found |
| `400` | Invalid field names or values |
| `409` | Slug already exists |

---

## 🎛️ Status Management

### 4.1 Publish Term

Change term status from DRAFT to PUBLISHED (makes it visible in public queries).

```http
PUT /terms/publish/{id}
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `id` | UUID | ✅ Yes | UUID of the term to publish |

#### ✅ Success Response (200)

Full TermDetailResponse with `status = "PUBLISHED"`.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | Term not found |

---

### 4.2 Move to Draft

Change term status to DRAFT (removes from published view).

```http
PUT /terms/draft/{id}
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `id` | UUID | ✅ Yes | UUID of the term |

#### ✅ Success Response (200)

Full TermDetailResponse with `status = "DRAFT"`.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | Term not found |

---

### 4.3 Archive Term

Change term status to ARCHIVED (preserves data but hides from public view).

```http
PUT /terms/archive/{id}
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `id` | UUID | ✅ Yes | UUID of the term |

#### ✅ Success Response (200)

Full TermDetailResponse with `status = "ARCHIVED"`.

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | Term not found |

---

## 🗑️ Deletion Endpoint

### 5.1 Delete Term

Permanently delete a term and all its associations from the database.

```http
DELETE /terms/delete/{id}
```

#### 📋 Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `id` | UUID | ✅ Yes | UUID of the term to delete |

#### ✅ Success Response (204)

```json
{
  "success": true,
  "statusCode": 204,
  "message": "Term deleted successfully",
  "timestamp": "2026-03-22T11:13:15"
}
```

#### ❌ Error Responses

| Code | Scenario |
|------|----------|
| `404` | Term not found |
| `400` | Invalid UUID |

---

## 📦 Data Models

### TermDetailResponse

Complete term information with relationships and categories.

```json
{
  "title": "string",
  "slug": "string",
  "content": "string",
  "seoTitle": "string | null",
  "seoDescription": "string | null",
  "featuredImageUrl": "string | null",
  "categoryNames": ["string"],
  "relatedTerms": [
    {
      "id": "uuid",
      "slug": "string",
      "title": "string",
      "featuredImageUrl": "string | null"
    }
  ]
}
```

#### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `title` | String | Term name (lowercase) |
| `slug` | String | Unique identifier (lowercase, hyphen-separated) |
| `content` | String | Term description (lowercase) |
| `seoTitle` | String\|null | SEO title tag |
| `seoDescription` | String\|null | SEO meta description |
| `featuredImageUrl` | String\|null | Featured image URL |
| `categoryNames` | String[] | Associated category names |
| `relatedTerms` | RelatedTermResponse[] | Related term references |

---

### RelatedTermResponse

Summary information of a related term.

```json
{
  "id": "uuid",
  "slug": "string",
  "title": "string",
  "featuredImageUrl": "string | null"
}
```

#### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Term identifier |
| `slug` | String | Term slug (lowercase, hyphen-separated) |
| `title` | String | Term name (lowercase) |
| `featuredImageUrl` | String\|null | Featured image URL |

---

## ✔️ Validation Rules

### Title
- **Required:** ✅ Yes
- **Type:** String
- **Max Length:** 255 characters
- **Constraints:** Cannot be blank
- **Normalization:** Trimmed, lowercase

### Slug
- **Required:** ✅ Yes
- **Type:** String
- **Max Length:** 255 characters
- **Constraints:** Must be unique, lowercase, hyphen-separated alphanumerics only
- **Pattern:** `^[a-z0-9]+(?:-[a-z0-9]+)*$`
- **Normalization:** Trimmed, lowercase

### Content
- **Required:** ✅ Yes
- **Type:** String
- **Max Length:** 255 characters
- **Constraints:** Cannot be blank
- **Normalization:** Trimmed, lowercase

### SEO Title & Description
- **Required:** ❌ No
- **Type:** String
- **Constraints:** seoTitle max 255 chars, seoDescription max 500 chars
- **Default:** null
- **Normalization:** Trimmed, lowercase

### Featured Image URL
- **Required:** ❌ No
- **Type:** String (URL)
- **Max Length:** 500 characters
- **Default:** null
- **Normalization:** Trimmed, lowercase

### Status
- **Required:** ❌ No (default: DRAFT)
- **Type:** Enum
- **Values:** `DRAFT`, `PUBLISHED`, `ARCHIVED`
- **Input:** Case-insensitive (e.g., "draft", "DRAFT", "Draft" all accepted)

### Categories
- **Required:** ❌ No
- **Type:** String[]
- **Max Items:** 20 per term
- **Constraints:** Auto-created if don't exist, case-insensitive deduplication
- **Normalization:** Trimmed, lowercase, space-collapsed

### Related Terms
- **Required:** ❌ No
- **Type:** String[] (slugs) or UUID[]
- **Max Items:** 20 per term
- **Constraints:** Must exist in database, no self-references
- **Normalization:** Deduplicated, normalized

---

## ⚠️ Error Handling

### Error Response Format

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Descriptive error message",
  "timestamp": "2026-03-22T11:13:15"
}
```

### Common Error Codes

| Code | HTTP Status | Type | Scenario |
|------|-------------|------|----------|
| 400 | Bad Request | Validation | Invalid input, blank required field, invalid format |
| 404 | Not Found | Resource | Term/slug/category doesn't exist |
| 409 | Conflict | Integrity | Slug already exists, duplicate entry |
| 500 | Server Error | System | Unexpected server error |

---

## 💡 Examples

### Example 1: Create a New Term

#### Request
```bash
curl -X POST http://localhost:8080/terms/create \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Luke Skywalker",
    "slug": "luke-skywalker",
    "content": "Jedi Knight, son of Anakin Skywalker",
    "seoTitle": "Luke Skywalker | Imperialpedia",
    "categoryNames": ["Characters", "Jedi"],
    "status": "PUBLISHED"
  }'
```

#### Response
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Term created successfully",
  "data": {
    "title": "luke skywalker",
    "slug": "luke-skywalker",
    "content": "jedi knight, son of anakin skywalker",
    "seoTitle": "luke skywalker | imperialpedia",
    "seoDescription": null,
    "featuredImageUrl": null,
    "categoryNames": ["characters", "jedi"],
    "relatedTerms": []
  },
  "timestamp": "2026-03-22T11:13:15"
}
```

---

### Example 2: Patch a Term (Update Only Title)

#### Request
```bash
curl -X PATCH http://localhost:8080/terms/update/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Luke Skywalker - Jedi Master"
  }'
```

#### Response
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Term patched successfully",
  "data": {
    "title": "luke skywalker - jedi master",
    "slug": "luke-skywalker",
    "content": "jedi knight, son of anakin skywalker",
    "categoryNames": ["characters", "jedi"],
    "relatedTerms": []
  },
  "timestamp": "2026-03-22T11:13:15"
}
```

---

### Example 3: Get Terms by Letter

#### Request
```bash
curl http://localhost:8080/terms/l
```

#### Response
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Terms retrieved successfully",
  "data": [
    {
      "title": "luke skywalker",
      "slug": "luke-skywalker",
      "content": "jedi knight, son of anakin skywalker",
      "categoryNames": ["characters", "jedi"],
      "relatedTerms": []
    },
    {
      "title": "leia organa",
      "slug": "leia-organa",
      "content": "princess, rebel leader, luke's sister",
      "categoryNames": ["characters", "rebellion"],
      "relatedTerms": []
    }
  ],
  "timestamp": "2026-03-22T11:13:15"
}
```

---

## 🗄️ Database Constraints

### Uniqueness Constraints

- **Slug:** Only one term per slug (case-insensitive comparison)
- **Category Name:** Only one category per name (case-insensitive comparison)
- **Related Terms:** Cannot have self-references (term cannot reference itself)

### Relationship Loading

- **EntityGraph:** Related terms and categories are eagerly loaded for detail endpoints
- **Performance:** Optimized for single-record retrieval with full relationship data
- **Lazy Loading:** List endpoints use basic projection for efficiency

---

## 📈 HTTP Status Codes

| HTTP Code | ApiResponse statusCode | Meaning | When Used |
|-----------|------------------------|---------|-----------|
| 200 | 200 | OK - Request successful | GET, PUT, PATCH success |
| 201 | 201 | Created - Resource created | POST new term success |
| 204 | 204 | No Content | DELETE success |
| 400 | 400 | Bad Request | Input validation failed |
| 404 | 404 | Not Found | Resource doesn't exist |
| 409 | 409 | Conflict | Duplicate slug/integrity violation |
| 500 | 500 | Server Error | Unexpected server error |

---

## 🗂️ Changelog

### Version 1.0.0 (Current)
**Date:** March 22, 2026

**Features:**
- ✅ Initial API implementation
- ✅ Full CRUD operations for terms
- ✅ Status management (DRAFT, PUBLISHED, ARCHIVED)
- ✅ Category association and auto-creation
- ✅ Related terms linking (by slug or UUID)
- ✅ Comprehensive input validation
- ✅ Automatic data normalization
- ✅ Consistent response format with ApiResponse wrapper
- ✅ Utility classes for input parsing
- ✅ Complete error handling

**Known Limitations:**
- ❌ No authentication (will be added in v2.0)
- ❌ No pagination (returns all results)
- ❌ No rate limiting
- ❌ No caching layer

---

<div align="center">

### 🔗 Related Documentation

[📖 Back to README](./README.md) | [📋 Project Summary](./PROJECT_STATUS.md)

---

**Last Updated:** March 22, 2026  
**API Version:** 1.0.0-SNAPSHOT  
**Status:** ✅ Production Ready

</div>

---