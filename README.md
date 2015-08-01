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

Remaining:

   * Access control
   * Remove hard coded elasticsearch URL.
   * OOB
   * Automatic Schema migration
   * Import/Export
   * Temporal data
   * Alternate search
   * Extra operations
   * Corrupted index repair
   * Separate Lucene Jpa Directory implementation to it's own artifact.
   * More robust Angular JS sample
   * Off-line sync
