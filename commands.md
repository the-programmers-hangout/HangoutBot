# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Fun
| Commands    | Arguments                   | Description                                                                                |
| ----------- | --------------------------- | ------------------------------------------------------------------------------------------ |
| coin        | (Coins)                     | Flip a coin (or coins).                                                                    |
| cowsay      | (Cow), (Message)            | Displays a cowsay with a given message. Run with no arguments to get a list of valid cows. |
| flip        | Choice 1 \| Choice 2 \| ... | Choose one of the given choices.                                                           |
| roll        | (Min), (Max)                | Rolls a number in a range (default 1-100)                                                  |
| xkcd        | (Comic Number)              | Returns the XKCD comic number specified, or a random comic if you don't supply a number.   |
| xkcd-latest | <none>                      | Grabs the latest XKCD comic.                                                               |
| xkcd-search | Query                       | Returns a XKCD comic that most closely matches your query.                                 |

## Guild
| Commands            | Arguments      | Description                                                   |
| ------------------- | -------------- | ------------------------------------------------------------- |
| getwelcomechannel   | <none>         | Gets the channel used for welcome embeds.                     |
| listgrantableroles  | <none>         | Lists the available grantable roles.                          |
| makerolegrantable   | Role, Category | Adds a role to the list of grantable roles.                   |
| removegrantablerole | Role           | Removes a role to the list of grantable roles.                |
| setadminrole        | Role           | Sets the role that distinguishes an Administrator             |
| setmuterole         | Role           | Sets the role used to mute an user                            |
| setstaffrole        | Role           | Sets the role that distinguishes an Administrator             |
| setwelcomechannel   | Channel        | Sets the channel used for welcome embeds.                     |
| togglewelcome       | <none>         | Toggles the display of welcome messages upon guild user join. |

## Information
| Commands   | Arguments | Description                                        |
| ---------- | --------- | -------------------------------------------------- |
| ping       | <none>    | pong.                                              |
| roleinfo   | Role      | Displays information about the given role.         |
| serverinfo | <none>    | Display a message giving basic server information. |
| source     | <none>    | Get the url for the bot source code.               |
| uptime     | <none>    | Displays how long the bot has been running for.    |
| userinfo   | User      | Displays information about the given user.         |

## Utility
| Commands         | Arguments               | Description                                                                       |
| ---------------- | ----------------------- | --------------------------------------------------------------------------------- |
| Help             | (Command)               | Display a help menu.                                                              |
| avatar           | User                    | Gets the avatar from the given user                                               |
| echo             | (TextChannel), Text     | Echo a message to a channel.                                                      |
| getpermission    | Command                 | Returns the required permission level for the given command                       |
| grant            | (Member), GrantableRole | Grants a role to a lower ranked member or yourself                                |
| nuke             | (TextChannel), Integer  | Delete 2 - 99 past messages in the given channel (default is the invoked channel) |
| revoke           | (Member), GrantableRole | Revokes a role from a lower ranked member or yourself                             |
| selfmute         | (Time)                  | Mute yourself for an amout of time. Default is 1 hour. Max is 24 hours.           |
| viewcreationdate | User                    | Displays when a user was created                                                  |
| viewjoindate     | Member                  | Displays when a user joined the guild                                             |

