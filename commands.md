# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Anime
| Commands      | Arguments    | Description                                                |
| ------------- | ------------ | ---------------------------------------------------------- |
| anime         | Search terms | Searches for an anime on Anilist based on the given terms. |
| anime-suggest | Criteria     | Suggests an anime based on the given criteria.             |
| manga         | Search terms | Searches for a manga on Anilist based on the given terms.  |

## Bot Information
| Commands       | Arguments | Description                                       |
| -------------- | --------- | ------------------------------------------------- |
| botstats, ping | <none>    | Displays miscellaneous information about the bot. |
| debugstats     | <none>    | Displays some debugging information               |
| source         | <none>    | Get the url for the bot source code.              |

## Channel
| Commands  | Arguments           | Description                          |
| --------- | ------------------- | ------------------------------------ |
| chnltopic | TextChannel, (Text) | Gets or sets the topic of a channel. |
| slowmode  | TextChannel, Time   | Set the slowmode in a channel.       |

## Colors
| Commands   | Arguments            | Description                                                              |
| ---------- | -------------------- | ------------------------------------------------------------------------ |
| clearcolor | <none>               | Clears the current color role.                                           |
| listcolors | <none>               | Creates a role with the given name and color and assigns it to the user. |
| setcolor   | (HexColor), RoleName | Creates a role with the given name and color and assigns it to the user. |

## Configuration
| Commands     | Arguments     | Description                                                                                                     |
| ------------ | ------------- | --------------------------------------------------------------------------------------------------------------- |
| botchannel   | (TextChannel) | Sets the bot channel. If set, the bot channel will be the only channel where the bot will accept commands from. |
| greetchannel | (TextChannel) | Gets or sets the channel used for welcome greetings.                                                            |
| logchannel   | (TextChannel) | Sets the channel used to log executed commands                                                                  |
| muterole     | (Role)        | Gets or sets the role used to mute an user.                                                                     |
| softmuterole | (Role)        | Gets or sets the role used to soft mute an user                                                                 |

## Fun
| Commands | Arguments             | Description                                                                                |
| -------- | --------------------- | ------------------------------------------------------------------------------------------ |
| coin     | (Coins)               | Flip a coin (or coins).                                                                    |
| cowsay   | (Cow), (Message)      | Displays a cowsay with a given message. Run with no arguments to get a list of valid cows. |
| dadjoke  | <none>                | Returns a random dad joke.                                                                 |
| flip     | Choice 1;Choice 2;... | Choose one of the given choices.                                                           |
| roll     | (Min), (Max)          | Rolls a number in a range (default 1-100)                                                  |

## Greetings
| Commands  | Arguments        | Description                                       |
| --------- | ---------------- | ------------------------------------------------- |
| greetings | (enable/disable) | Enables or disables the greetings on member join. |

## Information
| Commands   | Arguments | Description                                        |
| ---------- | --------- | -------------------------------------------------- |
| avatar     | (user)    | Gets the avatar from the given user                |
| help       | (Command) | Display help information.                          |
| invite     | <none>    | Generates an invite link to this server.           |
| roleinfo   | Role      | Displays information about the given role.         |
| serverinfo | <none>    | Display a message giving basic server information. |
| userinfo   | (user)    | Displays information about the given user.         |

## Moderation
| Commands | Arguments              | Description                                                                       |
| -------- | ---------------------- | --------------------------------------------------------------------------------- |
| echo     | (TextChannel), Text    | Echo a message to a channel.                                                      |
| nuke     | (TextChannel), Integer | Delete 2 - 99 past messages in the given channel (default is the invoked channel) |

## Owner Commands
| Commands  | Arguments | Description                                                                                                  |
| --------- | --------- | ------------------------------------------------------------------------------------------------------------ |
| cooldown  | (Integer) | Gets or sets the cooldown (in seconds) after a user executes a command before he is able to execute another. |
| setprefix | Prefix    | Sets the bot prefix.                                                                                         |

## Permissions
| Commands                | Arguments                                     | Description                                                                    |
| ----------------------- | --------------------------------------------- | ------------------------------------------------------------------------------ |
| permission, permissions | (set/get/list), (Command), (Permission Level) | Gets or sets the permissions for a command. Use `list` to view all permissions |
| roleperms               | Role, (Permission Level)                      | Gets or sets the permission level of the given role                            |

## Reminders
| Commands      | Arguments  | Description                                                            |
| ------------- | ---------- | ---------------------------------------------------------------------- |
| listreminders | <none>     | List your active reminders                                             |
| remindme      | Time, Text | A command that'll remind you about something after the specified time. |

## Roles
| Commands      | Arguments                        | Description                                           |
| ------------- | -------------------------------- | ----------------------------------------------------- |
| grant         | (Member), GrantableRole          | Grants a role to a lower ranked member or yourself    |
| grantablerole | add/rem/list, (Role), (Category) | Adds, removes or lists grantble roles.                |
| listroles     | (GrepRegex)                      | List all the roles available in the guild.            |
| revoke        | (Member), GrantableRole          | Revokes a role from a lower ranked member or yourself |

## Selfmute
| Commands       | Arguments | Description                                                                                                                                                                                                                                |
| -------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| productivemute | (Time)    | Trying to be productive? Mute yourself for the specified amount of time. A productive mute will prevent you from talking in the social channels while still allowing the use of the language channels. Default is 1 hour. Max is 24 hours. |
| selfmute       | (Time)    | Mute yourself for the given amount of time. A mute will stop you from talking in any channel. Default is 1 hour. Max is 24 hours.                                                                                                          |

## XKCD
| Commands    | Arguments      | Description                                                                              |
| ----------- | -------------- | ---------------------------------------------------------------------------------------- |
| xkcd        | (Comic Number) | Returns the XKCD comic number specified, or a random comic if you don't supply a number. |
| xkcd-latest | <none>         | Grabs the latest XKCD comic.                                                             |
| xkcd-search | Query          | Returns a XKCD comic that most closely matches your query.                               |

