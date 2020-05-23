# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Bot Information
| Commands | Arguments | Description                                       |
| -------- | --------- | ------------------------------------------------- |
| botstats | <none>    | Displays miscellaneous information about the bot. |
| source   | <none>    | Get the url for the bot source code.              |

## BotConfiguration
| Commands     | Arguments     | Description                                                                                                     |
| ------------ | ------------- | --------------------------------------------------------------------------------------------------------------- |
| botchannel   | (TextChannel) | Sets the bot channel. If set, the bot channel will be the only channel where the bot will accept commands from. |
| greetchannel | (TextChannel) | Gets or sets the channel used for welcome greetings.                                                            |
| logchannel   | (TextChannel) | Sets the channel used to log executed commands                                                                  |
| muterole     | (Role)        | Gets or sets the role used to mute an user.                                                                     |
| softmuterole | (Role)        | Gets or sets the role used to soft mute an user                                                                 |

## Colors
| Commands   | Arguments            | Description                                                              |
| ---------- | -------------------- | ------------------------------------------------------------------------ |
| clearcolor | <none>               | Clears the current color role.                                           |
| listcolors | <none>               | Creates a role with the given name and color and assigns it to the user. |
| setcolor   | (HexColor), RoleName | Creates a role with the given name and color and assigns it to the user. |

## Fun
| Commands | Arguments                                  | Description                                                                                |
| -------- | ------------------------------------------ | ------------------------------------------------------------------------------------------ |
| coin     | (Coins)                                    | Flip a coin (or coins).                                                                    |
| cowsay   | (Cow), (Message)                           | Displays a cowsay with a given message. Run with no arguments to get a list of valid cows. |
| dadjoke  | <none>                                     | Returns a random dad joke.                                                                 |
| flip     | (SeparatedChoice 1 \| Choice 2 \| ...Text) | Choose one of the given choices.                                                           |
| roll     | (Min), (Max)                               | Rolls a number in a range (default 1-100)                                                  |

## Greetings
| Commands         | Arguments        | Description                                                   |
| ---------------- | ---------------- | ------------------------------------------------------------- |
| chnlgreetings    | enable/disable   | Whether to send  greetings in the configured greeting channel |
| dmgreetings      | enable/disable   | Whether to send  greetings through DMs                        |
| greetingcontents | <none>           | Configure the contents of the greeting message                |
| greetings        | (enable/disable) | Enables or disables the greetings on member join.             |

## Information
| Commands   | Arguments | Description                                        |
| ---------- | --------- | -------------------------------------------------- |
| avatar     | (User)    | Gets the avatar from the given user                |
| help       | (Command) | Display help information.                          |
| invite     | <none>    | Generates an invite link to this server.           |
| roleinfo   | Role      | Displays information about the given role.         |
| serverinfo | <none>    | Display a message giving basic server information. |
| userinfo   | (User)    | Displays information about the given user.         |

## Moderation
| Commands | Arguments              | Description                                                                       |
| -------- | ---------------------- | --------------------------------------------------------------------------------- |
| echo     | (TextChannel), Text    | Echo a message to a channel.                                                      |
| nuke     | (TextChannel), Integer | Delete 2 - 99 past messages in the given channel (default is the invoked channel) |

## Permissions
| Commands   | Arguments                                   | Description                                                 |
| ---------- | ------------------------------------------- | ----------------------------------------------------------- |
| permission | set/get/list, (Command), (Permission Level) | Returns the required permission level for the given command |
| roleperms  | Role, (Permission Level)                    | Gets or sets the permission level of the given role         |

## Prefix
| Commands  | Arguments | Description          |
| --------- | --------- | -------------------- |
| setprefix | Prefix    | Sets the bot prefix. |

## Reminders
| Commands      | Arguments  | Description                                                            |
| ------------- | ---------- | ---------------------------------------------------------------------- |
| listreminders | <none>     | List your active reminders                                             |
| remindme      | Time, Text | A command that'll remind you about something after the specified time. |

## Roles
| Commands      | Arguments                        | Description                                           |
| ------------- | -------------------------------- | ----------------------------------------------------- |
| createrole    | <none>                           | Creates a role.                                       |
| deleterole    | Role...                          | Deletes the given role or roles.                      |
| grant         | (Member), GrantableRole          | Grants a role to a lower ranked member or yourself    |
| grantablerole | add/rem/list, (Role), (Category) | Adds, removes or lists grantble roles.                |
| listroles     | (GrepRegex)                      | List all the roles available in the guild.            |
| revoke        | (Member), GrantableRole          | Revokes a role from a lower ranked member or yourself |

## Selfmute
| Commands       | Arguments | Description                                                                                                                                                                                                                                |
| -------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| productivemute | (Time)    | Trying to be productive? Mute yourself for the specified amount of time. A productive mute will prevent you from talking in the social channels while still allowing the use of the language channels. Default is 1 hour. Max is 24 hours. |
| selfmute       | (Time)    | Mute yourself for the given amount of time. A mute will stop you from talking in any channel. Default is 1 hour. Max is 24 hours.                                                                                                          |

## Slowmode
| Commands    | Arguments         | Description                |
| ----------- | ----------------- | -------------------------- |
| setslowmode | TextChannel, Time | Set slowmode in a channel. |

## XKCD
| Commands    | Arguments      | Description                                                                              |
| ----------- | -------------- | ---------------------------------------------------------------------------------------- |
| xkcd        | (Comic Number) | Returns the XKCD comic number specified, or a random comic if you don't supply a number. |
| xkcd-latest | <none>         | Grabs the latest XKCD comic.                                                             |
| xkcd-search | Query          | Returns a XKCD comic that most closely matches your query.                               |

