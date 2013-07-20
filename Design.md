#Design
The whole program is a plugin based system. Except the core syncing functionality every thing is a plugin.

A plugin can be of two types,

1. A plugin to support a new storage service. This could be an entirely new cloud service, or a new way to store and retrive files and related data in a supported service. The local file system should also be also such a plugin. To be such a plugin you will have to implement an interface.

2. A plugin that does some thing else. Like a GUI interface, of connects to a web app. A app that does some thing using all the events.
