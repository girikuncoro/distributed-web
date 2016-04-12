#CS5300-Project1b: Scalable and Available Website
## Giri Kuncoro (gk256), Yihui Fu (yf263), Shibo Zang(sz428)

### 1. Overall Structure
There are four parts of our solution: 

- Remote Procedure Call (RPC)
- SSM protocol
- Installation script
- Reboot script.  

The core of our solution is SSM protocol. It defines how our application works, such as how to store user session in the server side, how to replicate and backup sessions in the distributed system and make the system K-resilient. 

The communication between different nodes are through RPC call. There are two types of RPC call: session read and session write. Session read call is used for retrieving session information from a particular node, and session write call is used for updating session information in different nodes and determining which nodes store the particular session. 

The installation script is used to do preparation jobs in each EC2 instance like installing dependencies, set up configuration files etc., and the reboot script handles the situation when one server crashes.

### 2. Formats of cookies and RPC messages
For cookies, the delimeter is #, and the format is: `SessionID#versionNumber#locations `

More specifically, for session ID, the delimiter is also #, and the format is: `serverID#rebootNum#sessionNum`

For RPC messages, the delimiter is `_`, and the format is:  
`CallID\_OperationCode\_SerializedSession`. 
The serialized session object is just concating everything in session by #.


### 3. Explanation of source files
- RPCClient.java: Perform session read and session write operations.
- RPCServer.java: Listen on the requests sent by rpc client.
- RPCConfig.java: Configuration about rpc clients and rpc servers.
- RPCStream.java: Marshall and unmarshall serialized session objects.
- BackgroundThread.java: Another thread running RPC server.
- proj1bServlet.java: The controller that handles the logic of application.

### 4. Changes for exra credit

##### 4.1 Supporting F > 1 Failures