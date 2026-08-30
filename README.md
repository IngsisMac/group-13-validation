# PrintScript Test Compatibility Kit

This repository provides a way for any PrintScript interpreter to test its compliance with the expected functionality.
This includes a collection of PrintScript files with their expected print results, as well as the validation result (valid or invalid).

### How to validate your implementation?
- Fork this repository and create a new branch with the following naming convention: ```group-[group number]-validation```.
- Modify **build.gradle** to include your implementation as a dependency in the project.
- Return your implementation in [CustomImplementationFactory](src/main/java/implementation/CustomImplementationFactory.java). 
  Make sure to define a adapter of your interpreter to comply with the [PrintScriptInterpreter](src/main/java/interpreter/PrintScriptInterpreter.java) interface.
- When all tests cases run correctly using ```./gradlew build```, create a pull request to the original repository for a final correction.  
