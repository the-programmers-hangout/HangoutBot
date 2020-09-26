![CI](https://github.com/the-programmers-hangout/HangoutBot/workflows/CI/badge.svg)

# HangoutBot

A bot to manage utility commands and funcionaility that does not warrant its own bot in the TheProgrammersHangout Discord Server.

Join our server at https://discord.gg/programming

<a href="https://discord.gg/programming">
<img src="https://img.shields.io/discord/244230771232079873?label=The%20Programmers%20Hangout&logo=discord" alt="The Programmers Hangout">
</a>

## Installation

The bot makes use of Docker and Docker Compose to setup the required environment. 

```console
$ git clone https://github.com/the-programmers-hangout/HangoutBot.git
$ cd HangoutBot

# Create the configuration file
$ cp config/config.json.example config/config.json
$ vim config/config.json

# Launch the bot
$ docker-compose build
$ docker-compose up
``` 

The bot will look for a configuration file at `config/config.json`. This file is used to provide the bot token and the id of the bot owner. 

```json
{
  "token" : "<insert-token>",
  "ownerId": "<insert-bot-owner-id>",
  "prefix": "!!"
}
```

## License

```
MIT License

Copyright (c) 2020 The Programmers Hangout

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
