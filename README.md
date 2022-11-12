# A simple Java messenger application

Author: Esa Karjalainen
- E-Mail: ekarjt@gmail.com
- Github: sdeska

This repository contains the client- and serverside implementations of a simple messenger app.

Currently the implementation allows for multiple clients connecting to the server. The server also updates 
other clients whenever a client connects or disconnects, allowing clients to display the currently connected ones. 
Error situations are handled accordingly and the user is informed.
Messaging other clients is currently implemented partially. The messages received from any given client are
saved into a chat with said client. The messages are displayed on the GUI, and chats between different clients
can be viewed as seen fit, without losing message history. 

Currently notifications about a received message have not been implemented, and the GUI automatically displays 
the chat in which a new message was received in. This is very bad UX :(

The purpose of the project is general programming practice in Java, but the will to try some 
basic socket programming definitely influenced the decision to specifically make a messenger app.

The application can be run using Maven.

Run server: 'mvn exec:java'

Run client: 'mvn javafx:run'

The server should be run and left waiting for connections first. This can be done with 'mvn exec:java'
on the command line, while in the root directory of the Maven project. Then, the clients can be run with
'mvn javafx:run' on a separate terminals.
