DoxDB
=====

> TL;DR : MongoDB on SQL

This is *structured* document oriented data management module that runs on top
of an RDBMS and file system.  It keeps the notion of document data away
from the storage facility, but still provides robust ACID transactions using
standard RDBMS transactions.

### Roadmap

Completed:

   * JSON validation
   * Distributed Search (using [ElasticSearch][] REST API via Jest)
   * CRUD
   * Event handling
   * REST API
   * Angular JS sample
   * Event notification using WebSockets
   * Allow for retrieving large collections
   * Schema retrieval
   * Example with [Angular Schema Form][1]
   * Migrate from JEST to JAX-RS Client
   * AngularJS Module Generation
   * Change database schema
   * Andvanced [ElasticSearch][] queries

Remaining:

   * Access control
   * OOB
   * Automatic Schema migration
   * Import/Export
   * Temporal data
   * Extra operations
   * Corrupted index repair
   * Off-line sync
   * Data anonymization
   * Create a data dictionary and rename the methods and fields
     to correspond to the data dictionary.

Out of scope:

   * Alternate search (not going to be in scope until a better alternative to ElasticSearch is found)

[1]: http://schemaform.io/
[2]: http://stackoverflow.com/questions/32205381/how-do-i-override-the-schema-for-a-jpa-app-inside-a-web-fragment-from-a-web-app
[ElasticSearch]: https://www.elastic.co/products/elasticsearch
