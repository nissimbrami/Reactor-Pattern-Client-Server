# TFTP Server & Client Implementation

An extended implementation of the Trivial File Transfer Protocol (TFTP) that includes both server and client components. This version extends the standard TFTP protocol with features like user authentication and file system notifications.

## Project Overview

This project implements a custom TFTP protocol with enhanced functionality:
- File upload and download operations
- User authentication system
- Real-time file system notifications
- Directory listing capabilities
- Multi-client support

## Setup & Installation

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Maven
- Git

### Installation Steps

1. Clone the repository:
```bash
git clone https://github.com/nissimbrami/TFTP-Implementation.git
cd TFTP-Implementation
```

2. Build the server:
```bash
cd server
mvn clean install
```

3. Build the client:
```bash
cd ../client
mvn clean install
```

## Running the Application

### Starting the Server

1. Navigate to the server directory:
```bash
cd server
```

2. Create a 'Files' directory (if it doesn't exist):
```bash
mkdir Files
```

3. Run the server (replace 7777 with your desired port):
```bash
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpServer" -Dexec.args="7777"
```

### Starting the Client

1. Open a new terminal and navigate to the client directory:
```bash
cd client
```

2. Run the client (replace localhost and 7777 with your server's IP and port):
```bash
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpClient" -Dexec.args="localhost 7777"
```

## Using the Client

Once connected, you can use the following commands:

1. Login to the server:
```
LOGRQ <username>
```

2. Download a file:
```
RRQ <filename>
```

3. Upload a file:
```
WRQ <filename>
```

4. List server files:
```
DIRQ
```

5. Delete a file:
```
DEL
