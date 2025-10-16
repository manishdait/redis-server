# RedisServer - A Java Implementation for lite Redis Server
A lightweigh  implementation of a Redis-like in-memory data store written entirely in Java.  
It supports the **RESP (Redis Serialization Protocol)** and can communicate directly with the official `redis-cli`.  


## Quick Start

### Clone and Build
```bash
git clone https://github.com/manishdait/redis-server.git
cd redis-server
mvn clean package
```

### Run the Server
```bash
mvn exec:java -Dexec.mainClass="io.github.manishdait.redis_server.Main"
```

The server starts on port `6379` (the default Redis port).

### Connect with redis-cli
```bash
redis-cli -p 6379
```

## Supported Commands
| Command | Description |
| :------- | :------ |
| PING | 	Checks server availability |
| ECHO `<msg>`|	Returns the same message |
| SET `<key>` `<value>` `<options>` | Set value for given key with options [NX	XX EX PX EXAT PXAT] |
| GET `<key>` |	Retrieves value for a key |
| INCR / DECR `<key>` |	Atomically increments/decrements integer values |
| EXISTS `<key>...`	| Checks if keys exist |
| DEL `<key>...` | Deletes one or more keys |
| LPUSH / RPUSH `<key>` `<value>...` |	Pushes values into lists (left/right) |
| LINDEX `<key>` `<index>` |	Retrieves list item at index |
| LRANGE `<key>` `<start>` `<end>` | Gets a slice of list values |

