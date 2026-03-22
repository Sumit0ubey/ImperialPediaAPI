# ImperialPedia API - Complete API Documentation

## Overview
Imperial Pedia API is a RESTful Spring Boot application for managing a comprehensive database of terms with support for categorization, relationships, and multiple status states (DRAFT, PUBLISHED, ARCHIVED).

**Base URL:** `http://localhost:8080`

**API Version:** 0.0.1-SNAPSHOT

---

## Global Response Format

All responses follow the standard `ApiResponse<T>` wrapper:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Success message",
  "data": { /* response data */ },
  "timestamp": "2026-03-22T11:13:15"
}
```

---

## Authentication
Currently, no authentication is required. All endpoints are publicly accessible.

---

## Endpoints

### 1. TERM RETRIEVAL ENDPOINTS

#### 1.1 Get Published Terms by Letter
**Endpoint:** `GET /terms/{letter}`  
**Description:** Retrieve all published terms that start with a specific letter.

**Parameters:**
- `letter` (path, required): Single letter or word (e.g., "A", "Star", "Jedi")
- Letter is case-insensitive and will be normalized to UPPERCASE

**Response:**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Terms retrieved successfully",
  "data": [
    {
      "title": "Anakin Skywalker",
      "slug": "anakin-skywalker",
      "content": "Jedi Knight description...",
      "seoTitle": "Anakin Skywalker - Imperialpedia",
      "seoDescription": "Biography of Anakin Skywalker",
      "featuredImageUrl": "https://...",
      "categoryNames": ["Characters", "Jedi"],
      "relatedTerms": [
        {
          "id": "uuid",
          "slug": "luke-skywalker",
          "title": "Luke Skywalker",
          "featuredImageUrl": "https://..."
        }
      ]
    }
  ]
}
```

**Errors:**
- `400 Bad Request` - Letter is null or blank
- `404 Not Found` - No published terms found for that letter

---

#### 1.2 Get Archived Terms (Optional Letter Filter)
**Endpoint:** `GET /terms/archived`  
**Description:** Retrieve all archived terms, optionally filtered by letter.

**Parameters:**
- `letter` (query, optional): Single letter filter (e.g., "?letter=A")
- If letter is blank/missing, returns all archived terms
- Letter is case-insensitive and normalized to UPPERCASE

**Response:** Same structure as endpoint 1.1 but with archived terms

**Errors:**
- `404 Not Found` - No archived terms found (with or without letter)

---

#### 1.3 Get Draft Terms (Optional Letter Filter)
**Endpoint:** `GET /terms/draft`  
**Description:** Retrieve all draft terms, optionally filtered by letter.

**Parameters:**
- `letter` (query, optional): Single letter filter (e.g., "?letter=D")
- If letter is blank/missing, returns all draft terms
- Letter is case-insensitive and normalized to UPPERCASE

**Response:** Same structure as endpoint 1.1 but with draft terms

**Errors:**
- `404 Not Found` - No draft terms found (with or without letter)

---

#### 1.4 Get Term by Slug
**Endpoint:** `GET /terms/slug/{slug}`  
**Description:** Retrieve a published term by its unique slug.

**Parameters:**
- `slug` (path, required): Unique slug identifier (lowercase, hyphen-separated, e.g., "luke-skywalker")
- Slug is case-insensitive; input is normalized to lowercase

**Response:** Full TermDetailResponse with categories and related terms

**Errors:**
- `400 Bad Request` - Slug is null or blank
- `404 Not Found` - Term not found with this slug

---

#### 1.5 Get Term by UUID
**Endpoint:** `GET /terms/{id}`  
**Description:** Retrieve a published term by its unique UUID.

**Parameters:**
- `id` (path, required): UUID of the term (e.g., "550e8400-e29b-41d4-a716-446655440000")

**Response:** Full TermDetailResponse with categories and related terms

**Errors:**
- `400 Bad Request` - Invalid UUID format
- `404 Not Found` - Term not found with this ID

---

### 2. TERM CREATION ENDPOINT

#### 2.1 Create New Term
**Endpoint:** `POST /terms/create`  
**Description:** Create a new term with complete metadata, categories, and related terms.

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
  "title": "Darth Vader",
  "slug": "darth-vader",
  "content": "Anakin Skywalker turned Sith Lord...",
  "seoTitle": "Darth Vader - Dark Lord of the Sith",
  "seoDescription": "Learn about Darth Vader, the Sith Lord",
  "featuredImageUrl": "https://example.com/darth-vader.jpg",
  "status": "DRAFT",
  "categoryNames": ["Characters", "Sith", "Villains"],
  "relatedTerms": ["anakin-skywalker", "luke-skywalker"],
  "relatedTermIds": ["uuid1", "uuid2"]
}
```

**Field Descriptions:**
- `title` (string, required, max 255): Term name
- `slug` (string, required, max 255): Unique identifier (lowercase, hyphen-separated only)
- `content` (string, required, max 255): Term description
- `seoTitle` (string, optional, max 255): SEO title tag
- `seoDescription` (string, optional, max 500): SEO meta description
- `featuredImageUrl` (string, optional, max 500): Featured image URL
- `status` (enum, optional, default: DRAFT): DRAFT, PUBLISHED, or ARCHIVED
- `categoryNames` (string array, optional, max 20 items): Category names (auto-created if not exist)
- `relatedTerms` (string array, optional, max 20 items): Related term slugs (must exist)
- `relatedTermIds` (UUID array, optional, max 20 items): Related term UUIDs (must exist)

**Response:**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Term created successfully",
  "data": { /* full TermDetailResponse */ }
}
```

**Errors:**
- `400 Bad Request` - Validation failed (blank fields, invalid format)
- `409 Conflict` - Slug already exists
- `404 Not Found` - Related term slug/ID not found

---

### 3. TERM UPDATE ENDPOINTS

#### 3.1 Full Update (PUT)
**Endpoint:** `PUT /terms/update/{id}`  
**Description:** Completely replace a term with new data (all required fields must be provided).

**Parameters:**
- `id` (path, required): UUID of the term to update

**Request Body:** Same as endpoint 2.1 (all required fields)

**Response:** Full TermDetailResponse after update

**Errors:**
- `404 Not Found` - Term not found
- `409 Conflict` - Slug already exists (for different term)
- `400 Bad Request` - Validation failed

---

#### 3.2 Partial Update (PATCH)
**Endpoint:** `PATCH /terms/update/{id}`  
**Description:** Update only the fields provided (optional fields can be omitted).

**Parameters:**
- `id` (path, required): UUID of the term to patch

**Request Body (all fields optional):**
```json
{
  "title": "Updated Title",
  "slug": "updated-slug",
  "content": "Updated content",
  "seoTitle": "Updated SEO Title",
  "seoDescription": "Updated SEO Description",
  "featuredImageUrl": "https://new-image-url.jpg",
  "status": "PUBLISHED",
  "categoryNames": ["NewCategory", "AnotherCategory"],
  "relatedTerms": ["slug1", "slug2"],
  "relatedTermIds": ["uuid1", "uuid2"]
}
```

**Supported Fields:**
- `title` - Term name
- `slug` - Unique identifier
- `content` - Description
- `seoTitle` - SEO title
- `seoDescription` - SEO description
- `featuredImageUrl` - Image URL
- `status` - Term status
- `categoryNames` - Categories (replaces existing)
- `relatedTerms` - Related term slugs (replaces existing)
- `relatedTermIds` - Related term UUIDs (replaces existing)

**Response:** Full TermDetailResponse after patch

**Errors:**
- `404 Not Found` - Term not found
- `400 Bad Request` - Invalid field names or values
- `409 Conflict` - Slug already exists

---

### 4. TERM STATUS MANAGEMENT ENDPOINTS

#### 4.1 Publish Term
**Endpoint:** `PUT /terms/publish/{id}`  
**Description:** Change term status from DRAFT to PUBLISHED (makes it visible in public queries).

**Parameters:**
- `id` (path, required): UUID of the term to publish

**Request Body:** Empty (no body required)

**Response:** Full TermDetailResponse with status = PUBLISHED

**Errors:**
- `404 Not Found` - Term not found

---

#### 4.2 Move to Draft
**Endpoint:** `PUT /terms/draft/{id}`  
**Description:** Change term status to DRAFT (removes from published view).

**Parameters:**
- `id` (path, required): UUID of the term

**Request Body:** Empty (no body required)

**Response:** Full TermDetailResponse with status = DRAFT

**Errors:**
- `404 Not Found` - Term not found

---

#### 4.3 Archive Term
**Endpoint:** `PUT /terms/archive/{id}`  
**Description:** Change term status to ARCHIVED (preserves data but hides from public view).

**Parameters:**
- `id` (path, required): UUID of the term

**Request Body:** Empty (no body required)

**Response:** Full TermDetailResponse with status = ARCHIVED

**Errors:**
- `404 Not Found` - Term not found

---

### 5. TERM DELETION ENDPOINT

#### 5.1 Delete Term
**Endpoint:** `DELETE /terms/delete/{id}`  
**Description:** Permanently delete a term and all its associations from the database.

**Parameters:**
- `id` (path, required): UUID of the term to delete

**Request Body:** Empty (no body required)

**Response:**
```json
{
  "success": true,
  "statusCode": 204,
  "message": "Term deleted successfully"
}
```

**Errors:**
- `404 Not Found` - Term not found
- `400 Bad Request` - Invalid UUID

---

## Data Models

### TermDetailResponse
Complete term information with relationships.

```json
{
  "title": "string",
  "slug": "string",
  "content": "string",
  "seoTitle": "string",
  "seoDescription": "string",
  "featuredImageUrl": "string",
  "categoryNames": ["string"],
  "relatedTerms": [
    {
      "id": "uuid",
      "slug": "string",
      "title": "string",
      "featuredImageUrl": "string"
    }
  ]
}
```

### RelatedTermResponse
Summary of a related term.

```json
{
  "id": "uuid",
  "slug": "string (lowercase, hyphen-separated)",
  "title": "string",
  "featuredImageUrl": "string (nullable)"
}
```

---

## Validation Rules

### Title
- Required
- Max 255 characters
- Cannot be blank
- Stored and normalized to lowercase

### Slug
- Required
- Max 255 characters
- Must be lowercase, hyphen-separated alphanumerics only
- Must be unique
- Input is normalized to lowercase
- Regex: `^[a-z0-9]+(?:-[a-z0-9]+)*$`

### Content
- Required
- Max 255 characters
- Cannot be blank
- Stored and normalized to lowercase

### SEO Fields (seoTitle, seoDescription)
- Optional
- seoTitle: max 255 characters
- seoDescription: max 500 characters
- Can be null or empty string (treated as null)

### Status
- Optional (default: DRAFT)
- Values: DRAFT, PUBLISHED, ARCHIVED
- Case-insensitive input (e.g., "draft", "DRAFT", "Draft" all accepted)

### Categories
- Optional (default: empty list)
- Max 20 categories per term
- Names are normalized to lowercase and deduplicated
- Auto-created if don't exist
- Spaces are collapsed (multiple spaces → single space)

### Related Terms
- Optional (default: empty list)
- Max 20 related terms per term
- Can be specified by slug OR UUID
- Cannot reference the same term (self-reference check)
- Related terms must already exist in database
- Slugs are normalized and deduplicated

---

## Error Responses

All errors follow this format:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Error description",
  "timestamp": "2026-03-22T11:13:15"
}
```

### Common Error Codes

| Code | Scenario |
|------|----------|
| 400 | Bad Request (validation failed, invalid input) |
| 404 | Not Found (term/slug/category doesn't exist) |
| 409 | Conflict (slug already exists) |
| 500 | Internal Server Error |

---

## Request/Response Examples

### Example 1: Create a New Term

**Request:**
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

**Response:**
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

**Request:**
```bash
curl -X PATCH http://localhost:8080/terms/update/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Luke Skywalker - Jedi Master"
  }'
```

**Response:**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Term patched successfully",
  "data": {
    "title": "luke skywalker - jedi master",
    "slug": "luke-skywalker",
    ...existing data...
  }
}
```

---

### Example 3: Get Terms by Letter

**Request:**
```bash
curl http://localhost:8080/terms/l
```

**Response:**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Terms retrieved successfully",
  "data": [
    {
      "title": "luke skywalker",
      "slug": "luke-skywalker",
      ...
    },
    {
      "title": "leia organa",
      "slug": "leia-organa",
      ...
    }
  ]
}
```

---

## Field Normalization

All text fields (title, content, slug, category names, etc.) are automatically:
1. **Trimmed** - Leading/trailing whitespace removed
2. **Lowercased** - Converted to lowercase
3. **Deduplicated** - Duplicate entries removed

This ensures consistency and prevents case-variant duplicates.

---

## Database Constraints

- **Slug Uniqueness:** Only one term per slug (case-insensitive)
- **Category Uniqueness:** Only one category per name (case-insensitive)
- **Related Terms:** Cannot have self-references
- **Lazy Loading:** Related terms and categories are eagerly loaded for detail endpoints

---

## Status Codes Summary

| HTTP | ApiResponse statusCode | Meaning |
|------|------------------------|---------|
| 200  | 200 | Success (GET, PUT, PATCH, DELETE) |
| 201  | 201 | Created (POST new term) |
| 204  | 204 | No Content (successful DELETE) |
| 400  | 400 | Bad Request (validation error) |
| 404  | 404 | Not Found (resource missing) |
| 409  | 409 | Conflict (duplicate slug) |
| 500  | 500 | Internal Server Error |

---

## Pagination
Currently not implemented. All list endpoints return complete results.

---

## Rate Limiting
Currently not implemented. No rate limits on API calls.

---

## Changelog

### Version 0.0.1 (Current)
- Initial API implementation
- Full CRUD operations for terms
- Status management (DRAFT, PUBLISHED, ARCHIVED)
- Category association
- Related terms linking
- Comprehensive input validation
- Centralized response format with ApiResponse wrapper
- Utility classes for input normalization

---

## Support

For issues or questions about the API, please refer to the source code documentation or contact the development team.

**Last Updated:** March 22, 2026  
**API Version:** 1.0.0-SNAPSHOT

