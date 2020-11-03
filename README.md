![CI](https://github.com/the-programmers-hangout/HangoutBot/workflows/CI/badge.svg)

# HangoutBot

A bot for miscellaneous commands and functionality required in the TheProgrammersHangout Discord server.

Join us at https://discord.gg/programming

<a href="https://discord.gg/programming">
<img src="https://img.shields.io/discord/244230771232079873?label=The%20Programmers%20Hangout&logo=discord" alt="The Programmers Hangout">
</a>

## Installation

The bot makes use of Docker and Docker Compose to setup the required environment. 

```console
$ git clone https://github.com/the-programmers-hangout/HangoutBot.git
$ cd HangoutBot

# Setup the environment file with the necessary fields
$ cp .env.example .env
$ vim .env

# Launch the bot
$ docker-compose up --build --detach
``` 

The .env file is used to configure the bot token, prefix and owner id, in the following format: 

```
BOT_TOKEN=<insert-bot-token>
BOT_PREFIX=++
BOT_OWNER=<insert-owner-id>
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
