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


### How to run

#### Launching instances
1. Configure AWS credentials file located in `~/.aws/credentials` using following format:
```
[default]
aws_access_key_id = YOUR_AWS_ACCESS_KEY_ID
aws_secret_access_key = YOUR_AWS_SECRET_ACCESS_KEY
```

2. Open `launch.sh` and configure the parameters on top of the file as below:
```
# number of instances to launch, default is 3
N=3

# resiliency to maintain, default is 1, but F>1 is supported
F=1

# S3 bucket name to bring war file and other stuffs in
S3_BUCKET="S3_BUCKET_NAME"

# keypair to ssh instance, important for reboot process
# .pem extension not required
KEYPAIR="proj1bfinal"
```
Make sure to provide the keypair name without `.pem` extension and the S3 bucket name. Default keypair is also provided in the project, but please handle with care. N and F have to be valid numbers, i.e. N = 2F + 1.

3. Open `install.sh`, then configure AWS credentials and S3 bucket name on top of file as below:
```
# AWS credentials to connect with aws cli
AWS_KEY="YOUR_AWS_ACCESS_KEY_ID"
AWS_SECRET="YOUR_AWS_SECRET_ACCESS_KEY"

# S3 bucket name to bring war file and other stuffs in
S3_BUCKET="S3_BUCKET_NAME""
```
Make sure credentials provided here are same as the one in `~/.aws/credentials`. This is important since simpleDB manages the tables for same AWS access key. Also, provide S3 bucket name as the one configured in `launch.sh`.

4. Configure the `default` security group on AWS management console, to open all necessary ports, as below:
- Port 80: for TCP
- Port 8080: for Tomcat
- Port 5300: for RPC communication
- Port 22: for SSH
- Port 443: for HTTPS
- All ports for UDP traffic

5. Configure the bucket policy on the provided S3 bucket name, allow everyone to download/upload files. The cofiguration shouldn't matter as long as the provided AWS credentials own the bucket and have S3 full access policy.

6. Execute the `launch.sh` to launch instances by typing below:
```
./launch.sh
```
Make sure `launch.sh` is executable by typing `chmod +x launch.sh` if the script is not.

7. Go to AWS EC2 console and N numbers of EC2 instances should start initializing. Find the public DNS of each intance and register the domain at `bigdata.systems`.

8. Once EC2 instances are running and completed the checks, open up browser and locate one of the instances domain at `proj1b` url, e.g.:
```
http://server0.sz428.bigdata.systems:8080/proj1b
```
The project's main page should be opened, enjoy!







