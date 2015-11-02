# Scripting

The scripting engine runs on a JSR-223 compliant scripting language.  
JavaScript is already built in with Java so implementation is primarily
tested on that environment.

The scripting engine is simplistic in that it only executes the scripts
based on input that had been provided and the result data is expected to
be mappable to a `JsonObject`.

