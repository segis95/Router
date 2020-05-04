# Automatic static routing table configuration project

Suggest you have a network (e.g. in the picture below) and you need to configure routes from each node to any other node.
Setting up all routes manually could be a time consuming task. There is a way to automatize this procedure.

## Protocol

The approach is based on the following protocol.

- Each node starts to _ _beep_ _ (i.e. broadcast messages of **type B**) through all accessible interfaces.
- When a node recieves such a _ _beep_ _ it already knows the _address_ it was sent from. It replies _ _ Hello_ _ the _ _ address_ _ with a message of **type H**('H#_address_')
