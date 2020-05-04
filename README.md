# Automatic static routing table configuration project

Suggest you have a network (e.g. in the picture below) and you need to configure routes from each node to any other node.
Setting up all routes manually could be a time consuming task. There is a way to automatize this procedure.

## Protocol

The approach is based on the following protocol.

- Each node starts to _beep_(i.e. broadcast messages of **type B**) through all accessible interfaces.
- When a node recieves such a _beep_ it already knows the _address_ it was sent from. It replies _Hello_ the _address_ with a message of **type H**('H#_address_') so that after recieveing this message the _beeper_ will know it's own address in the subnet, the interface it corresponds to and will make acquaintance to it's neighbour.

- Once these pieces of information about one-step connections are established they need to be transmitted throught the network so that each node knows about all availible connections in the graph. This is done using a **type T** messages. The informations about connection between _from_ and _to_ addresses is transmitted via a message 'T#_from_#_to_'.
