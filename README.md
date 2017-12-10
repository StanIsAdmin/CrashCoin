# CrashCoin
A fully functional and inefficient cryptocurrency, written in Java.

![logo](./docs/logo.png "CrashCoin™")

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
             
You can also stop and restart the instances with the same sementic: 
```
start.sh stop <type>
start.sh restart <type>
```

## View console
To view and interact (only with "client") with an already launched program you can type
```
start.sh view <type>
```

## Make command without `start.sh`
You can ignore the file `start.sh` and use mainstream command, as follow:
```
java -jar dist/master.jar
java -jar dist/relay.jar [ip] [port]
java -jar dist/miner.jar <user> <password> [ip] [port]
java -jar dist/client.jar [ip] [port]
```
Where `[]` elements are optionnal and `<>` are mandatory.      

- *Relay*: _ip_ and _port_ of the master      
- *Client*: _ip_ and _port_ of the relay      
- *Miner*: _user_ and _password_ of the user who will recieved the reward.  You can also specify the _ip_ and _port_ of the relay.      
