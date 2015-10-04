DoxDB
=====

> TL;DR : MongoDB on SQL

This is *structured* document oriented data management module that runs on top
of an RDBMS and file system.  It keeps the notion of document data away
from the storage facility, but still provides robust ACID transactions using
standard RDBMS transactions.

[![Build Status](https://drone.io/github.com/trajano/doxdb/status.png)](https://drone.io/github.com/trajano/doxdb/latest)

### Roadmap

Completed:

   * JSON validation
   * Distributed Search (using [ElasticSearch][] using REST API via JAX-RS)
   * CRUD
   * Event handling
   * REST API
   * Angular JS sample
   * Event notification using WebSockets
   * Allow for retrieving large collections
   * [Import/Export][4]
   * Schema retrieval
   * Example with [Angular Schema Form][1]
   * AngularJS Module Generation
   * [Change database schema][2]
   * Advanced [ElasticSearch][] queries
   * Unique lookup queries
   * Non-unique lookup queries
   * Defining lookups via json-path  

Remaining:

   * Access control
   * OOB
   * Automatic Schema migration
   * Temporal data
   * Corrupted index repair
   * Off-line sync
   * [Data anonymization][5]
   * Create a [data dictionary][3] and rename the methods and fields
     to correspond to the data dictionary.

Out of scope:

   * Alternate search (not going to be in scope until a better alternative to ElasticSearch is found)
   * Extra operations are out of scope.  Instead the application will provide
     their own REST API.  However, DoxDB provides a Local EJB API that the
     REST API may invoke. 
   
Notes:

   * Jest is no longer used to avoid having too many transitive dependencies.

[1]: http://schemaform.io/
[2]: http://stackoverflow.com/questions/32205381/how-do-i-override-the-schema-for-a-jpa-app-inside-a-web-fragment-from-a-web-app
[3]: https://github.com/trajano/doxdb/wiki/Data-Dictionary
[4]: https://github.com/trajano/doxdb/wiki/Import-Export-functionality
[5]: https://github.com/trajano/doxdb/wiki/Data-Anonymization
[ElasticSearch]: https://www.elastic.co/products/elasticsearch
