#Welcome to the Netcrypt README:

NetCrypt is a secure network FTP program implemented in Java. It uses RSA for an asymmetric key exchange, and then AES for securly transfering the file. It also provides data integrity on the file with SHA-256 digital signature authentication 

#WORK LOG


12/1/19 - Netcrypt is currently under development in its early stages. It is currently planned to be a command line tool for secure network transmission of data. It will use RSA for key exchange over a socket connection to pass AES symmetric encryption keys. It will then send encrypted data over the connection and decrypt on the client side. 

12/2/19 - NetCrypt has local symmetric encryption and decryption functioning. Plan is to setup socket connection within the next few commits.

12/4/19 - Socket connection is setup and working. Planning to implement RSA for key exchange and SHA-256 for authentication.

12/9/19 - RSA implemented for symmetric key exchange. SHA-256 implemented for authentication. Digest is calculated and attached at the end of file, then encrypted. This provides valid digital signature. Encrypted file is being transmitted across socket.

12/10/19 - Digest is locally computed on server side after decrypting file. Everything seems to be working for small files. Planning on streamlining the output and adding error checking for misuse. 

12/11/19 - Error handling is getting fixed. Output is nearly finalized. Large files are not able to be sent accross the socket currently because of buffering issue with transmission. Thinking about refactoring the DataOutputStream into a BufferedOutputStream.


