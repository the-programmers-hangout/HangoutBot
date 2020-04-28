# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Fun
| Commands | Arguments                   | Description                                                                                |
| -------- | --------------------------- | ------------------------------------------------------------------------------------------ |
| coin     | (Coins)                     | Flip a coin (or coins).                                                                    |
| cowsay   | (Cow), (Message)            | Displays a cowsay with a given message. Run with no arguments to get a list of valid cows. |
| flip     | Choice 1 \| Choice 2 \| ... | Choose one of the given choices.                                                           |
| roll     | (Min), (Max)                | Rolls a number in a range (default 1-100)                                                  |

## GuildConfiguration
| Commands          | Arguments | Description                                                   |
| ----------------- | --------- | ------------------------------------------------------------- |
| getwelcomechannel | <none>    | Gets the channel used for welcome embeds.                     |
| setadminrole      | Role      | Sets the role that distinguishes an Administrator             |
| setmuterole       | Role      | Sets the role used to mute an user                            |
| setstaffrole      | Role      | Sets the role that distinguishes an Administrator             |
| setwelcomechannel | Channel   | Sets the channel used for welcome embeds.                     |
| togglewelcome     | <none>    | Toggles the display of welcome messages upon guild user join. |

## Information
| Commands   | Arguments | Description                                       |
| ---------- | --------- | ------------------------------------------------- |
| ping       | <none>    | pong                                              |
| serverinfo | <none>    | Display a message giving basic server information |

## Roles
| Commands            | Arguments               | Description                                           |
| ------------------- | ----------------------- | ----------------------------------------------------- |
| grant               | (Member), GrantableRole | Grants a role to a lower ranked member or yourself    |
| listgrantableroles  | <none>                  | Lists the available grantable roles.                  |
| makerolegrantable   | Role, Category          | Adds a role to the list of grantable roles.           |
| removegrantablerole | Role                    | Removes a role to the list of grantable roles.        |
| revoke              | (Member), GrantableRole | Revokes a role from a lower ranked member or yourself |

## StaffUtility
| Commands | Arguments              | Description                                                                       |
| -------- | ---------------------- | --------------------------------------------------------------------------------- |
| echo     | (TextChannel), Text    | Echo a message to a channel.                                                      |
| nuke     | (TextChannel), Integer | Delete 2 - 99 past messages in the given channel (default is the invoked channel) |

## Utility
| Commands         | Arguments | Description                                                             |
| ---------------- | --------- | ----------------------------------------------------------------------- |
| Help             | (Command) | Display a help menu.                                                    |
| avatar           | User      | Gets the avatar from the given user                                     |
| selfmute         | (Time)    | Mute yourself for an amout of time. Default is 1 hour. Max is 24 hours. |
| viewcreationdate | User      | Displays when a user was created                                        |
| viewjoindate     | Member    | Displays when a user joined the guild                                   |

