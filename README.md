#  Bank Application
This is a simple in-memory bank application.

## setup
* Open project in intellij
* Verify that you have Java 8 installed and configured as SDK **1.8** in Intellij
* Verify that Annotation processing is enabled in Intellij, Settings -> Build -> Compiler -> Annotation Processor  

### Data encapsulation 
* Collections/Maps are only access inside the class, and copied if returned
* Parameters are validated before modifying state

### Thread safety 
* final variables are used for unmodifiable state
* volatile is used for (single field) modifiable state
* Synchronized is used for modifiable collections  

