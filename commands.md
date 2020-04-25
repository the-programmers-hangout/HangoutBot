# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Fun
| Commands | Arguments                   | Description                                                                                |
| -------- | --------------------------- | ------------------------------------------------------------------------------------------ |
| coin     | (Coins)                     | Flip a coin (or coins)                                                                     |
| cowsay   | (Cow), (Message)            | Displays a cowsay with a given message. Run with no arguments to get a list of valid cows. |
| flip     | Choice 1 \| Choice 2 \| ... | Choose one of                                                                              |
| roll     | (Min), (Max)                | Rolls a number in a range (default 1-100)                                                  |

## GuildConfiguration
| Commands           | Arguments | Description                                         |
| ------------------ | --------- | --------------------------------------------------- |
| resetconfig        | <none>    | Resets the guild configuration to its default state |
| setadminrole       | Role      | Sets the role that distinguishes an Administrator   |
| setprefix          | prefix    | Sets the prefix used by the bot in this guild       |
| setstaffrole       | Role      | Sets the role that distinguishes an Administrator   |
| togglebotreactions | <none>    | Sets the prefix used by the bot in this guild       |

## Roles
| Commands            | Arguments               | Description                                           |
| ------------------- | ----------------------- | ----------------------------------------------------- |
| addgrantablerole    | Category, Role          | Adds a role to the list of grantable roles.           |
| grant               | (Member), GrantableRole | Grants a role to a lower ranked member or yourself    |
| listgrantableroles  | <none>                  | Lists the available grantable roles.                  |
| removegrantablerole | Category, Role          | Removes a role to the list of grantable roles.        |
| revoke              | (Member), GrantableRole | Revokes a role from a lower ranked member or yourself |

## StaffUtility
| Commands | Arguments              | Description                                                                       |
| -------- | ---------------------- | --------------------------------------------------------------------------------- |
| echo     | (TextChannel), Text    | Echo a message to a channel.                                                      |
| nuke     | (TextChannel), Integer | Delete 2 - 99 past messages in the given channel (default is the invoked channel) |

## Utility
| Commands         | Arguments | Description                                       |
| ---------------- | --------- | ------------------------------------------------- |
| Help             | (Command) | Display a help menu.                              |
| avatar           | User      | Gets the avatar from the given user               |
| ping             | <none>    | pong                                              |
| serverinfo       | <none>    | Display a message giving basic server information |
| viewcreationdate | User      | Displays when a user was created                  |
| viewjoindate     | Member    | Displays when a user joined the guild             |

## WelcomeEmbeds
| Commands          | Arguments | Description                                                   |
| ----------------- | --------- | ------------------------------------------------------------- |
| getwelcomechannel | <none>    | Gets the channel used for welcome embeds.                     |
| setwelcomechannel | Channel   | Sets the channel used for welcome embeds.                     |
| togglewelcome     | <none>    | Toggles the display of welcome messages upon guild user join. |

