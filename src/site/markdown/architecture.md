Architecture
============
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

JPA vs JDBC
-----------
The implementation of this has jumped from JPA to JDBC back to JPA.  Although
JDBC provides a lot of low level functionality and provides us with more
performant functions, it does not work when working with different database
vendors.

In addition, although JPA can still function with JDBC, by leveraging the JPA
entity caches we can get some performance gain without additional code.

From searching the web for information on whether several small tables are
better vs. one large table with a discriminator field, it appears that 
the larger majority of articles I see indicate one large table that is properly
indexed would yield a better job. 

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

Search
------
*Originally* Lucene was chosen as the search engine over Solr or 
[Elasticsearch][2] primarily because of its low memory footprint.  The primary 
use case for doxdb is millions of documents rather than billions.  That being
said, the API is built so that search can be plugged into a different
technology if needed until a Java EE standard for search comes up.

However, Lucene on its own does not support a distributed environment and
was causing locking issues under load.  As such [ElasticSearch][2] was chosen
to handle the searching.

[1]: http://en.wikipedia.org/wiki/Out-of-band
[2]: https://www.elastic.co/products/elasticsearch
