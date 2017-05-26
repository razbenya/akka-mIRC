*  Complete details of the server architecture:
    * Implemented Actors: 
		* UserHandler - server actor that's handling login messages and creating "RemoteUser" actors. 
		* ChannelHandler - server actor that's creating Channels.
		* Channel - an actor that represent Channel, handles all messages that should be route to all the users in the channel
					such as: user left, user joined, user got kicked, user got banned, disband and channel messages.
		* RemoteUser - represents a user in the server, only used for forwarding messages to its parallel LocalUser.
					   Required for keeping a unique name for each user, and make it easier to find a user on the server.
		* LocalUser - the user actor handling all the messages that come from the server, channel and other users.
					  such as: login success, private messages, channel titles, user left, join, kicked, banned etc.
					  also handling the UI.
					  		  
    * Implemented Message types:
		* BanMessage - 		Used for telling a user that he got banned from a channel.
		* BannedMessage - 	Used by the channel to tell all its members that someone got banned.
		* KickMessage - 	Used for telling a user that he got kicked from a channel.
		* KickedMessage - 	Used by the channel to tell all its members that someone got kicked.
		* JoinMessage - 	Used for telling a channel that a user wishes to join / tell the ChannelHandler that he need to create a new channel.
		* JoinedMessage - 	Used by the channel to tell all is members that someone joined the channel.
		* LeftMessage - 	Used by a channel to tell all its members that someone left the channel.
		* TitleMessage - 	Used for changing a channel title.
		* ChannelMessage - 	Used for sending a message to the channel chat.
		* PrivateMessage - 	Used for sending a private message to a user.
		* RequestNameMessage - Used for asking a channel for its user list (ui handling message)
							   also used by the channel to ask members for their names.
		* UserNameMessage - Used for sending user name to the requesting user.
		* ChangeModeMessage - Used for promoting/demoting a user in a specific channel.
		* LoginMessage - 	Used for login to the server (register a user name).
		* enum Messages:
			* LEAVE - 		Used for telling a channel that a user wishes to leave.
			* LOGINSUCC - 	Used by the server to notify a user that his login succeeded.
			* USERLIST - 	Used for requesting all the user names from a channel (ui handling message).
			* CHAN - 		Used for requesting all the channel names from the ChannelHandler (ui handling message).
			* BECOMEOWNER - used for alerting a user that he is now the owner of a channel.
			* DISBAND - 	used for disbanding a channel.
			
    * Communication between Actors: 
		* Communication between users such as: Ban, Kick and Private messages flows directly between users.
		* Communication from user to channel used only when routing is needed
		* The UserHandler used only for creating user actor for each client.
		* The ChannelHandler is used to creating new channels if the channel doesn't exists,
			otherwise the join message goes directly to the channel.
		* Requesting names: the UI maintains a user list for each channel and a channel list (sent when requested)
							to do so we use a requesting name system as following: 
							when a user wishes to retrieve the channel list he send a request to the channelHandler
							he then passes the request (by routing) to all his routees(channels), then the channels sends their names directly
  							to the requesting user. same mechanism work when a user wishes to retrieve a user list of a channel.
	
    * Routers used: 
		* Channel - 		the channel actor is a router used for routing messages to all his users (as explained above).
		* ChannelHandler - 	routing only for requesting channel names for ui purpose (as explained above). 