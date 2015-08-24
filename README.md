doxdb
=====

This is *structured* document oriented data management module that runs on top
of an RDBMS and file system.  It keeps the notion of document data away
from the storage facility, but still provides robust ACID transactions using
standard RDBMS transactions.


### Roadmap

Completed:

   * JSON validation
   * Distributed Search (using ElasticSearch REST API via Jest)
   * CRUD
   * Event handling
   * REST API
   * Angular JS sample
   * Event notification using WebSockets
   * Allow for retrieving large collections
   * Schema retrieval
   * Example with [Angular Schema Form][1]
   * Migrate from JEST to JAX-RS Client

Remaining:

   * Access control
   * OOB
   * Automatic Schema migration
   * Import/Export
   * Temporal data
   * Alternate search
   * Extra operations
   * Corrupted index repair
   * Off-line sync
   * Data anonymization
   * Create a data dictionary and rename the methods and fields to correspond to the data dictionary.

[1]: http://schemaform.io/
