# CrashCoin
A fully functional and inefficient cryptocurrency, written in Java.

![logo](./more/logo.png "CrashCoin™")

## Compile
Compile the project with maven:
```
mvn compile
```

## Test
To test the project, use maven:
```
mvn test
```

## Start script
The script `start.sh` can start and stop the different jar files.  To start a specific jar use
```
start.sh start <type>
```
Where `<type>` is the name of the jar file.  To start all jars, you can use the type `all`.            
             
You can stop and restart the instances with the same sementic: 
```
start.sh stop <type>
start.sh restart <type>
```
