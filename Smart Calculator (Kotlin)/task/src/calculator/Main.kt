package calculator

import java.math.BigInteger
import kotlin.math.pow
import kotlin.system.exitProcess

object InputType {
    val ONE_VARIABLE = "^([a-z-AZ]+)$".toRegex()
    val EXIT = "^/exit$".toRegex()
    val VARIABLES = "^/variables$".toRegex()
    val HELP = "^/help$".toRegex()
    val COMMAND = "^/.*$".toRegex()
    val DECLARED_VARIABLE_NBR = "([a-z-AZ]+)\\s*=\\s*([+-]?\\d+)".toRegex()
    val DECLARED_VARIABLE_VAR = "([a-z-AZ]+)\\s*=\\s*([+-]?[a-zA-Z]+)".toRegex()

    // not used but nice
    const val HELP_TEXT = "The program calculates the expression of numbers.\nFor example:\n2 + 2 - 1\n2 -- 1\n1 +-+ 3"
    const val EXIT_TEXT = "Bye!"
    const val BAD_OUTPUT_TEXT = "Invalid expression"
    const val BAD_COMMAND_TEXT = "Unknown command"
}


fun sanitizeNumber(input: String): String {
    var output = "+$input"
    output = output.replace("\\s+".toRegex(), " ")
    output = output.replace("++", "+")
    output = output.replace("--", "+")
    output = output.replace("+-", "-")
    output = output.replace("-+", "-")
    output = output.replace("++", "+")
    output = output.replace("+-", "-")
    output = output.replace("([-+])\\s+(\\d+)".toRegex(), " \$1\$2")
    return output
}

fun sanitizeVariable(input: String): String {
    var output = "+$input"
    output = output.replace("\\s+".toRegex(), " ")
    output = output.replace("++", "+")
    output = output.replace("--", "+")
    output = output.replace("+-", "-")
    output = output.replace("-+", "-")
    output = output.replace("++", "+")
    output = output.replace("+-", "-")
    output = output.replace("([-+])\\s+([a-zA-Z])".toRegex(), " \$1\$2")
    return output
}

val variablesMap = mutableMapOf<String, String>()

//----------------------------------------------------------------------------------------------------
fun infixToPostfix(input: String): String {
    val output = StringBuilder()
    val stack = ArrayDeque<Char>()
    val precedence = mapOf('+' to 1, '-' to 1, '*' to 2, '/' to 2, '^' to 3)

    var lastChar: Char? = null
    var isUnary = true  // Předpokládáme, že první operátor může být unární

    for (char in input.filterNot { it.isWhitespace() }) {
        when {
            char.isDigit() -> {
                output.append(char)
                isUnary = false  // Po čísle nemůže následovat unární operátor
            }
            char == '(' -> {
                stack.addFirst(char)
                isUnary = true   // Za otevírací závorkou může být unární operátor
            }
            char == ')' -> {
                while (stack.isNotEmpty() && stack.first() != '(') {
                    output.append(' ').append(stack.removeFirst())
                }
                stack.removeFirstOrNull() // Remove '('
                isUnary = false  // Za uzavírací závorkou nemůže být unární operátor
            }
            char in precedence.keys -> {
                output.append(' ')
                while (stack.isNotEmpty() && precedence[stack.first()] ?: 0 >= precedence[char]!!) {
                    output.append(stack.removeFirst()).append(' ')
                }
                if (char == '-' && isUnary) {
                    output.append("0 ")  // Přidáváme 0 pro unární mínus
                }
                stack.addFirst(char)
                isUnary = char != '^'  // Exponentiace může být následována unárním operátorem
            }
        }
        lastChar = char
    }

    while (stack.isNotEmpty()) {
        output.append(' ').append(stack.removeFirst())
    }

    return output.toString().trim()
}


fun addBracketsToUnaryMinus(input: String): String {
    // Regex pro nalezení záporných čísel
    val regex = "-\\d+".toRegex()

    // Nahrazení všech výskytů záporných čísel tak, aby byla uzavřena v závorkách
    return regex.replace(input) { matchResult ->
        "(${matchResult.value})"
    }
}

fun hasBalancedBrackets(input: String): Boolean {
    val stack = ArrayDeque<Char>()

    for (char in input) {
        when (char) {
            '(' -> stack.addFirst(char)
            ')' -> {
                if (stack.isEmpty()) {
                    return false  // Nenalezena odpovídající otevírací závorka
                }
                stack.removeFirst()
            }
        }
    }

    return stack.isEmpty()  // Zásobník by měl být prázdný, pokud jsou závorky správně uzavřeny
}
//-----------------------------------------------------------------------------------------------------

// Changed to from Int to Long
fun evaluatePostfixLong(postfix: String): Long {
    val stack = ArrayDeque<Long>()
    try {
        postfix.split(" ").forEach { token ->
            when {
                token.toLongOrNull() != null -> stack.addLast(token.toLong())
                token in setOf("+", "-", "*", "/", "^") -> {
                    val right = stack.removeLast()
                    val left = stack.removeLast()
                    val result = when (token) {
                        "+" -> left + right
                        "-" -> left - right
                        "*" -> left * right
                        "/" -> left / right
                        "^" -> left.toDouble().pow(right.toInt()).toLong()
                        else -> throw IllegalArgumentException("Unsupported operator: $token")
                    }
                    stack.addLast(result)
                }
            }
        }
    } catch(e : NoSuchElementException) {
        throw IllegalArgumentException("Invalid expression")
    }

    return stack.last()
}

fun evaluatePostfixBigInteger(postfix: String): BigInteger {
    val stack = ArrayDeque<BigInteger>()
    try {
        postfix.split(" ").forEach { token ->
            when {
                token.toBigIntegerOrNull() != null -> stack.addLast(token.toBigInteger())
                token in setOf("+", "-", "*", "/", "^") -> {
                    val right = stack.removeLast()
                    val left = stack.removeLast()
                    val result = when (token) {
                        "+" -> left + right
                        "-" -> left - right
                        "*" -> left * right
                        "/" -> left / right
                        "^" -> left.toDouble().pow(right.toInt()).toBigDecimal().toBigInteger()
                        else -> throw IllegalArgumentException("Unsupported operator: $token")
                    }
                    stack.addLast(result)
                }
            }
        }
    } catch(e : NoSuchElementException) {
        throw IllegalArgumentException("Invalid expression")
    }

    return stack.last()
}


fun sanitizeOperators(input: String): String {
    var output = input
    output = output.replace("\\s+".toRegex(), " ")
    repeat(3) { output = output.replace("+++", "+") }
    repeat(3) { output = output.replace("---", "-") }
    repeat(3) { output = output.replace("++", "+") }
    repeat(3) { output = output.replace("--", "+") }
    repeat(3) { output = output.replace("+-", "-") }
    repeat(3) { output = output.replace("-+", "-") }

    repeat(3) { output = output.replace("--", "+") }
    repeat(3) { output = output.replace("++", "+") }
    repeat(3) { output = output.replace("+-", "-") }
    repeat(3) { output = output.replace("-+", "-") }
    output = output.replace("\\s+".toRegex(), " ")
    return output
}

//-----------------------------------------------------------------------------------------------------



fun main() {

//    val input = "2 *  (3 + 4) + 1"
    //val input = "2*(3+4)+1"
    //val input = "-2*-1*(30+4)+12"
    //val input = "2*-1"
    //val input = "2*(-1)"
//    val input = "8 * 3 + 12 * (4 - 2)"
//    println("Infix: $input")
//    val inputSanitized = addBracketsToUnaryMinus(sanitizeOperators(input))
//    println("Infix Sanitized: $inputSanitized")
//    val output = infixToPostfix(inputSanitized)
//    println("Postfix: $output")
//    val result = evaluatePostfix(output)
//    println("Result of '$output' =  $result")

    //println("Kotlin version: ${KotlinVersion.CURRENT}")


    while (true) {
        val inputLine = readln().trim()
        //println(inputLine)


        //--------------------------------------
        //          Variable assigment
        //--------------------------------------
        if (InputType.ONE_VARIABLE.matches(inputLine)) {
            val matchResult = InputType.ONE_VARIABLE.find(inputLine)
            val variableKey = matchResult!!.groupValues[1]
            if (!variablesMap.containsKey(variableKey)) {
                println("Invalid identifier")
            } else {
                println(variablesMap[variableKey]?.replace("+", ""))
            }
            continue
        }

        if (InputType.DECLARED_VARIABLE_NBR.matches(inputLine)) {
            val matchResult = InputType.DECLARED_VARIABLE_NBR.find(inputLine)
            val variable = matchResult!!.groupValues[1]
            val value = matchResult!!.groupValues[2]
            val sanitizedValue = sanitizeNumber(value)
            //println("Declared $variable = '$sanitizedValue'")
            variablesMap[variable] = "$sanitizedValue"
            //println(variablesMap)
            continue
        }
        if (InputType.DECLARED_VARIABLE_VAR.matches(inputLine)) {
            val matchResult = InputType.DECLARED_VARIABLE_VAR.find(inputLine)
            val variable = matchResult!!.groupValues[1]
            val value = matchResult!!.groupValues[2]
            val sanitizedValue = sanitizeVariable(value)
            val variableKey = sanitizedValue.replace("+", "").replace("-", "")
            if (!variablesMap.containsKey(variableKey)) {
                println("Unknown variable")
                continue
            }
            //println("Declared $variable = '$sanitizedValue'")

            val sanitizedValue2Value = sanitizedValue.replace(variableKey, variablesMap[variableKey].toString())
            val sanitizedValue2Value2 = sanitizeNumber(sanitizedValue2Value)
            //println("Declared $variable = '$sanitizedValue2Value2'")
            variablesMap[variable] = "$sanitizedValue2Value2"
            //println(variablesMap)
            continue;
        }
        if (inputLine.contains("=")) {
            println("Invalid assignment")
            continue
        }
        //--------------------------------------


        when {
            inputLine.isEmpty() -> {}

            //--------------------------------------
            //             Commands
            //--------------------------------------
            InputType.COMMAND.matches(inputLine) -> when {
                InputType.EXIT.matches(inputLine) -> {
                    println("Bye!")
                    exitProcess(0)
                }

                InputType.HELP.matches(inputLine) -> {
                    println("The program calculates the sum of numbers")
                }

                InputType.VARIABLES.matches(inputLine) -> {
                    println(variablesMap)
                }

                else -> println("Unknown command")
            }
            //--------------------------------------

            true -> {
                try {

                if (!hasBalancedBrackets(inputLine)) throw IllegalArgumentException("Invalid expression")

                //println("[DEBUG] Infix: $inputLine")
                var inputSanitized = addBracketsToUnaryMinus(sanitizeOperators(inputLine))
                //println("[DEBUG] Infix Sanitized: $inputSanitized")


                // substitute variables
                    "[a-zA-Z]+\\b".toRegex().findAll(inputSanitized).onEach {
                        //println("x--> " + it.value)
                        var varName = it.value
                        if (!variablesMap.containsKey(varName)) {
                            throw IllegalArgumentException("Unknown variable")
                        } else {
                            var number = variablesMap[varName]
                            inputSanitized = inputSanitized.replace(varName,"("+number.toString().replace("+","")+")")
                        }
                    }.toList()

                    //println("[DEBUG] Infix Sanitized no Variable: $inputSanitized")

                    val output = infixToPostfix(inputSanitized)
                //println("[DEBUG] Postfix: $output")
                    val result = evaluatePostfixBigInteger(output)
                //println("[DEBUG] Result of '$output' =  $result")
                    println(result)
                } catch(e : IllegalArgumentException) {
                    println(e.message)
                }
            }
            else -> println("Invalid expression")
        }
    }

}
