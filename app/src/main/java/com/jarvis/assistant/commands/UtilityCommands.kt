package com.jarvis.assistant.commands

import android.content.Context
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand
import kotlin.random.Random

/** Evaluates simple arithmetic like "what's 45 times 12" or "calculate 200 divided by 5". */
class MathCommand(context: Context) : JarvisCommand {
    private val triggers = listOf("calculate ", "what's ", "what is ", "math ")
    private val opWords = mapOf(
        "plus" to "+", "add" to "+", "minus" to "-", "subtract" to "-",
        "times" to "*", "multiplied by" to "*", "divided by" to "/", "over" to "/",
    )

    override fun matches(input: String): Boolean {
        val hasTrigger = triggers.any { input.startsWith(it) }
        val hasOperator = opWords.keys.any { input.contains(it) } ||
            Regex("""\d+\s*[+\-*/]\s*\d+""").containsMatchIn(input)
        return hasTrigger && hasOperator
    }

    override suspend fun execute(input: String): CommandResult {
        var expr = input
        for (trigger in triggers) expr = expr.removePrefix(trigger)
        for ((word, symbol) in opWords) expr = expr.replace(word, " $symbol ")

        val match = Regex("""(-?\d+(\.\d+)?)\s*([+\-*/])\s*(-?\d+(\.\d+)?)""").find(expr)
            ?: return CommandResult.Unsupported("I couldn't parse that as math.")

        val (aStr, _, op, bStr) = match.destructured
        val a = aStr.toDouble()
        val b = bStr.toDouble()
        val result = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else return CommandResult.Error("Can't divide by zero.")
            else -> return CommandResult.Unsupported("Unsupported operation.")
        }
        val formatted = if (result == result.toLong().toDouble()) result.toLong().toString() else result.toString()
        return CommandResult.Success("That's $formatted.")
    }

    override fun describe(input: String): String = "Calculate"
}

class CoinFlipCommand(context: Context) : JarvisCommand {
    override fun matches(input: String): Boolean = input.contains("flip a coin") || input.contains("toss a coin") || input.contains("heads or tails")
    override suspend fun execute(input: String): CommandResult =
        CommandResult.Success(if (Random.nextBoolean()) "Heads." else "Tails.")
    override fun describe(input: String): String = "Flip a coin"
}

class DiceRollCommand(context: Context) : JarvisCommand {
    private val pattern = Regex("""roll a (\d+)[- ]sided die""")
    override fun matches(input: String): Boolean = input.contains("roll a die") || input.contains("roll a dice") || pattern.containsMatchIn(input)
    override suspend fun execute(input: String): CommandResult {
        val sides = pattern.find(input)?.groupValues?.get(1)?.toIntOrNull() ?: 6
        return CommandResult.Success("You rolled a ${Random.nextInt(1, sides + 1)}.")
    }
    override fun describe(input: String): String = "Roll a die"
}

class RandomNumberCommand(context: Context) : JarvisCommand {
    private val pattern = Regex("""random number between (\d+) and (\d+)""")
    override fun matches(input: String): Boolean = pattern.containsMatchIn(input) || input.contains("give me a random number")
    override suspend fun execute(input: String): CommandResult {
        val match = pattern.find(input)
        val min = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val max = match?.groupValues?.get(2)?.toIntOrNull() ?: 100
        return CommandResult.Success("Your number is ${Random.nextInt(min, max + 1)}.")
    }
    override fun describe(input: String): String = "Random number"
}

class JokeCommand(context: Context) : JarvisCommand {
    private val jokes = listOf(
        "Why don't scientists trust atoms? Because they make up everything.",
        "I told my computer I needed a break, and it said no problem — it'll go to sleep too.",
        "Why do programmers prefer dark mode? Because light attracts bugs.",
        "I would tell you a joke about UDP, but you might not get it.",
        "There are 10 types of people: those who understand binary, and those who don't.",
    )
    override fun matches(input: String): Boolean = input.contains("tell me a joke") || input.contains("make me laugh")
    override suspend fun execute(input: String): CommandResult = CommandResult.Success(jokes.random())
    override fun describe(input: String): String = "Tell a joke"
}

class QuoteCommand(context: Context) : JarvisCommand {
    private val quotes = listOf(
        "The only way to do great work is to love what you do.",
        "Simplicity is the ultimate sophistication.",
        "Stay hungry, stay foolish.",
        "Done is better than perfect.",
        "Small steps every day add up to big results.",
    )
    override fun matches(input: String): Boolean = input.contains("motivate me") || input.contains("inspire me") || input.contains("give me a quote")
    override suspend fun execute(input: String): CommandResult = CommandResult.Success(quotes.random())
    override fun describe(input: String): String = "Motivational quote"
}

class GreetingCommand(context: Context) : JarvisCommand {
    override fun matches(input: String): Boolean =
        input.contains("good morning") || input.contains("good night") || input.contains("good evening") || input.contains("how are you")
    override suspend fun execute(input: String): CommandResult = when {
        input.contains("good morning") -> CommandResult.Success("Good morning. Ready when you are.")
        input.contains("good night") -> CommandResult.Success("Good night. Sleep well.")
        input.contains("good evening") -> CommandResult.Success("Good evening.")
        else -> CommandResult.Success("Systems nominal. How can I help?")
    }
    override fun describe(input: String): String = "Greeting"
}

/** Converts between Celsius and Fahrenheit - "convert 30 celsius to fahrenheit". */
class TemperatureConvertCommand(context: Context) : JarvisCommand {
    private val toF = Regex("""convert (-?\d+(\.\d+)?)\s*c(elsius)? to f(ahrenheit)?""")
    private val toC = Regex("""convert (-?\d+(\.\d+)?)\s*f(ahrenheit)? to c(elsius)?""")

    override fun matches(input: String): Boolean = toF.containsMatchIn(input) || toC.containsMatchIn(input)

    override suspend fun execute(input: String): CommandResult {
        toF.find(input)?.let {
            val c = it.groupValues[1].toDouble()
            val f = c * 9 / 5 + 32
            return CommandResult.Success("$c Celsius is ${"%.1f".format(f)} Fahrenheit.")
        }
        toC.find(input)?.let {
            val f = it.groupValues[1].toDouble()
            val c = (f - 32) * 5 / 9
            return CommandResult.Success("$f Fahrenheit is ${"%.1f".format(c)} Celsius.")
        }
        return CommandResult.Unsupported("I couldn't parse that conversion.")
    }
    override fun describe(input: String): String = "Temperature conversion"
}
