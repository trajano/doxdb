doxdb
=====

This is *structured* document oriented data management module that runs on top
of an RDBMS and file system.  It keeps the notion of document data away
from the storage facility, but still provides robust ACID transactions using
standard RDBMS transactions.

Use cases
---------
* Data archiving and retention - to conform with data retention laws of a country,
  and to reduce the amount of documents being indexed, specific data may need 
  to be removed from the system.
 
* Business data structure changes over time - as business users may not get
  insight of all the possibile things that they would need in the beginning,
  it is important that the system be flexible to allow changes to the data
  structures over time.

* Documents have a workflow to support changes over time - data that is
  entered into the documents may need to be corrected over time or made
  effective at a later time.

* Temporal data support is not always required - so don't force the previous
  use case.

* Easy structural changes - as an application grows so does the need for
  additional data fields and perhaps removal of data fields.  Since doxdb
  stores complex structured data in one LOB no database schema changes are
  required, only a migration of one document structure to another.

  This migration is performed using structured transformation systems such
  as XSLT.

  This way product owners need not get full settlement from the business
  owners on what the data should be because migrations are cheaper.

* Data archiving - as the application life grows the amount of data grows with
  it.  Although adding more capacity may be an option, if some business
  data can be archived so that 

Architecture
------------
The document is a first class citizen and the underlying database is secondary
and can be rebuilt using the documents.  This is how applications such as
Mail.app and WinAMP function, where an internal database is created out of
meta-data that can be gathered from the documents.

The documents for doxdb are structured XML files conforming to an XML schema.
XML document was chosen over JSON in that there are established standards for
schema and transformation (XSLT).  Should mapping be needed.

Storage files are kept in MIME multipart message format.  This allows adding
additional meta data without changing the structure and allows for the document
content to be stored as is.

Since JDBC is used and byte arrays are avoided in the implementation, the
[Out of band data][1] such as embedded images or file attachments are stored
in the database rather than being managed outside the database for extra
performance.  This simplifies the architecture implementations.


Comparison with other data stores
---------------------------------
* Unlike most other systems that try to *own* your data store such as MongoDB,
HBase or Lotus Notes, this system relies on JDBC to perform operations with
existing established RDBMS database infrastructures.  For most larger
enterprises, database management procedures would've been established long
ago and would be cost prohibitive to move or train to migrate data over to
other systems.

* Since this is running on top of JDBC, it will allow itself to partake in
existing ACID transactions including XA support for more complex applications.

Comparison with ORM frameworks
------------------------------
* doxdb is *not* an ORM framework.  An ORM framework by definition maps an
object to a relational database structure.  doxdb does not perform any mapping
and in fact prescribes a specific schema for the tables.

* doxdb does *not* provide any query capability, everything is looked up by
keys, but does interface with ElasticSearch as a search provider until a Java
EE standard has added support for doing search and indexing.

* Not everything is in the database.   Databases are not well known for fast
retrieval of large LOB data and doxdb allows the use of a file system to store
[Out of Band Data][1] retrieval.

Security
--------
There is an implicit trust for the database and file system as such the data
stored is not encrypted by the module.

Primary keys are not exposed outside the internal implementation.  
Although everything is looked up by ID, the IDs are NOT exposed on the REST
API, though within the EJB API tier it is still exposed to allow interop
with other application components.

[1]: http://en.wikipedia.org/wiki/Out-of-band
