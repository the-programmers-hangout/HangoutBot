# tphbot (Name TBD)

Works in progress bot for TheProgrammersHangout Discord server.

## Installation

The bot makes use of Docker and Docker Compose to setup the required environment. It makes use of a MySQL database image for persistent data.

```console
$ git clone https://github.com/MarkHC/tphbot
$ cd tphbot

# Create the configuration file
$ cp config/config.json.example config/config.json
$ vim config/config.json

# Launch the bot
$ docker-compose build
$ docker-compose up
``` 

The bot will look for a configuration file at `config/config.json`. This file is used to provide the bot token and the id of the bot owner.
