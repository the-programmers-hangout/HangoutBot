package me.markhc.hangoutbot.locale

import kotlin.random.Random

object Messages {
    const val INSUFFICIENT_PERMS = "Sorry, you do not have the required permissions."
    private val FLIP_RESPONSE_CHOICES = listOf(
        "Hmm, I'd say %choice%.",
        "%choice%, no doubt.",
        "Perhaps... %choice%.",
        "%choice% sounds good to me.",
        "If it were up to me, I'd go with %choice%"
    )

    private val JOIN_MESSAGE_CHOICES = listOf(
        "Aww yeeee it\u0027s %name%",
        "Is that a person! Hey, %name% !",
        "Doug Dimmadome, owner of the Dimmsdale Dimmadome? Oh... no, it\u0027s just %name% :|",
        "It\u0027s dangerous to go alone %name%! Take this.",
        "ლ(｀ー´ლ) you wanna go %name% ?!",
        "(╬ ಠ益ಠ) %name%, take your shoes off dammit.",
        "Ayy lmao it\u0027s %name%... Say Hi!",
        "%name% I\u0027m going to have to ask you a very serious question... do you pronounce C# " +
            "like C-hashtag... DO YOU!?! Because we do NOT need another one of them guys.",
        "%name% has come to save the day!",
        "%name% joined the party.",
        "%name% joined the guild.",
        "Quick everyone! %name% has bepis!",
        "Initializing welcome system for %name%...",
        "Hide! %name% uses tabs!",
        "Hide! %name% uses spaces!",
        "Is that? No, it can\u0027t be... Are you %name%?!?!?",
        "01010111 01100101 01101100 01100011 01101111 01101101 01100101 %name%",
        "Hold on %name%, your welcome message is compiling...",
        "Here\u0027s your sword, shield, helmet, now go %name%, and vanquish those bugs! 🗡️",
        "%name%, I\u0027ve got a feeling we're not in Kansas anymore...",
        "Don\u0027t throw away your shot, %name%!",
        "One, two, three, four, five, six, seven, eight, nine. It\u0027s %name%!",
        "Raise a glass to %name%!",
        "A toast to %name%!",
        "The world turned upside down, %name%!",
        "The world will never be the same, %name%!",
        "Uncaught Exception: TemplateRenderError(\"no user %name% found in cache\")",
        "Tell the story of tonight, %name%!",
        "Stay alive, %name%!",
        "History has its eyes on you, %name%!",
        "The world\u0027s gonna know your name, %name%!",
        "We are waiting in the wings for you, %name%!",
        "%name% is never late. They arrive precisely when they mean to.",
        "I am %name%. And I come back to you now... at the turn of the tide",
        "Oh, it\u0027s quite simple %name%. If you are a friend, you speak the password, and the doors will open.",
        "%name% has come back now... at the turn of the tide",
        "%name% uses light theme. There is no curse in Elvish, Entish, or the tongues of Men for this treachery",
        "Never gonna give %name% up, never gonna let %name% down",
        "You are strong and wise, %name%, and I am very proud of you.",
        "He wore the crown of the Night Sentinels, and those that tasted the bite of his sword named them... %name%.",
        "Help us, %name%. You are our only hope.",
        "Look around, look around at how lucky we are to be alive right now, %name%!",
        "%name%, you\u0027ll blow us all away! Someday, someday.",
        "Oh, %name% you outshine the morning sun!",
        "Live long and prosper, %name%!",
        "%name%! Look into your eyes, and the sky\u0027s the limit",
        "You\u0027ll build palaces and cathedrals out of code, %name%!",
        "Arise, arise Coders of TPH! Fell deeds awake: bugs and crashes! Mice shall be shaken, keyboards be splintered, a code day, a red day, ere the sun rises! Ride now, ride now! Ride to %name%!",
        "Open the door, get on the floor, everyone welcome %name%.",
        "We are outgunned, outmanned, outnumbered, outplanned! We gotta make an all out stand. Ayo we\u0027re gonna need %name%!",
        "Code for the code god! Tests for the test throne! Glorious programming awaits us %name%!",
        "%name%, you are the stuff that dreams are made of.",
        "The name\u0027s %name%. %name% Bond.",
        "We're no strangers to code\nYou read the rules and so will I\nA git commit is what I'm thinking of\nYou wouldn't get this from %name%",
        "%name% has made the wise decision to hide behind a bush. However, they have also made the common mistake of hiding in the only bush in the area. This is how not to be seen.",
        "May I have your attention, please?\nWill the real %name% please stand up?\nI repeat, will the real %name% please stand up?\nWe're gonna have a problem here",
        "My name is %name%, Commander of the coders of the North, General of the IDE legions, loyal servant to the true emperor, Open Source. Father to an unfinished project, husband to an idea waiting to be started. And I will have my vengeance, in this life or the next.",
        "git blame %name%",
        "Oh, %name%, don't ask for the moon. We have the stars.",
        "%name%, you can't fight in here! This is the War Room!",
        "Open the pod bay doors, %name%.",
        "That'll do, %name%. That'll do.",
       "%name%, the question isn\u0027t how... but when...",
        "%name%, excited to time travel? Here, take this yellow raincoat to be known as the \u0027unchanged\u0027",
        "We will watch your career with great interest, %name%.",
        "Hello there %name%. Bold of you to join.",
        "Sorry we don't have cookies %name%.",
        "Make way for %name%.",
        "Welcome %name%, to uh... What is this place again?",
        "A %name% has appeared.",
        "The one and only %name% has arrived, make way.",
        "%name%, did you bring the pizza, or was that someone else.",
        "Lord %name% has entered.",
        "I don't know who %name% is but he sounds like a good guy."
    )

    const val welcomeDescription: String = "Hey there, be sure to check out the info section at the top!"

    fun getRandomFlipMessage(choice: String) =
        FLIP_RESPONSE_CHOICES[Random.nextInt(FLIP_RESPONSE_CHOICES.size)].replace("%choice%", choice)

    fun getRandomJoinMessage(name: String) =
        JOIN_MESSAGE_CHOICES[Random.nextInt(JOIN_MESSAGE_CHOICES.size)].replace("%name%", name)
}
