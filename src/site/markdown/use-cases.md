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
