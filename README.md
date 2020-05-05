# Automatic static routing table configuration project

Suggest you have a network (e.g. in the picture below) and you need to configure routes from each node to any other node.
Setting up all routes manually could be a time consuming task. There is a way to automatize this procedure.

![Scheme](/images/scheme.jpg)

## Protocol

The approach is based on the following protocol.

- Each node starts to _beep_ (i.e. broadcast messages of **type B**) through all accessible interfaces.
- When a node recieves such a _beep_ it already knows the _address_ it was sent from. It replies _Hello_ the _address_ with a message of **type H**('H#_address_') so that after recieveing this message the _beeper_ will know it's own address in the subnet, the interface it corresponds to and will make acquaintance to it's neighbour.

- Once these pieces of information about one-step connections are established they need to be transmitted throught the network so that each node knows about all availible connections in the graph. This is done using a **type T** messages. The informations about connection between _from_ and _to_ addresses is transmitted via message 'T#_from_#_to_'.

- When a node recieves 'T#_from_#_to_' it has to transmit it to **all neighbours**. To prevent overflooding the transmission will be cancelled when either _to_ or _from_ is one of the node's addresses. 

- Each node also sends **type T** messages containing pairs of it's own addresses because they are evidently connected.

## Routing

Once the entire graph is constructed using the transmitted pieces of information each node performs **Dijkstra algorithm** to establish optimal routes from itself to all other nodes. Here the _optimal subroute_ principle is applied: **a subroute of an optimal route is also an optimal route**.

## Testing

Probably the easiest way to test the described approach is to use Linux Network Namespaces instead of physical connections. In order to create the network from the picture above one may use commands from file **create-ns**:

```
sudo bash create-ns
```
An to destroy it:
```
sudo bash destroy-ns
```
The code can be compiled with Java 1.8.0. One may also create an executable .jar as follows:
```
javac Hop.java Message.java Netint.java Pair.java SubnetUtils.java
jar cvmf MANIFEST.MF Protocol.jar Hop.class Message.class Netint.class Pair.class SubnetUtils.class
```
For simplicity it's possible to recreate the network and recompile the code using the following script contained in **compile**. So now
```
javac Hop.java Message.java Netint.java Pair.java SubnetUtils.java
jar cvmf MANIFEST.MF Protocol.jar Hop.class Message.class Netint.class Pair.class SubnetUtils.class
bash destroy-ns
bash create-ns
```
is the same as: 
```
sudo bash compile
```
## Project structure

