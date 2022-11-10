package codes.spectrum.conf2022

import codes.spectrum.conf2022.doc_type.DocType
import codes.spectrum.conf2022.input.IDocParser
import codes.spectrum.conf2022.output.ExtractedDocument
import kotlin.random.Random

/**
 * Вот собственно и класс, который как участник вы должны реализовать
 *
 * контракт один - пустой конструктор и реализация [IDocParser]
 */
class UserDocParser : IDocParser {
    override fun parse(input: String): List<ExtractedDocument> {
        /**
         * Это пример чтобы пройти совсем первый базовый тест, хардкод, но понятно API,
         * просто посмотрите preparedSampleTests для примера
         */
        if (input.startsWith("BASE_SAMPLE1.")) {
            return preparedSampleTests(input)
        }
        /**
         * Это раздел квалификации - все инпуты начинаются с `@ `
         * призываем Вас НЕ хардкодить!!! хардкод проверим просто на ревью по этой функции,
         * надо честно реализовать спеки по DocType.T1 и DocType.T2
         * мы их будем проверять секретными тестами!!!
         */
        if (input.startsWith("@ ")) {
            return qualificationTests(input)
        }

        if (input.matches(DocType.INN_FL.normaliseRegex)) {
            val inn = validatePersonInn(input)
            return listOf(ExtractedDocument(
                docType = DocType.INN_FL,
                isValid = !inn.startsWith("!"),
                value = input,
                isValidSetup = true,
            ))
        }

        if (input.matches(DocType.INN_UL.normaliseRegex)) {
            val inn = validateCompanyInn(input)
            return listOf(ExtractedDocument(
                docType = DocType.INN_UL,
                isValid = !inn.startsWith("!"),
                value = input,
                isValidSetup = true,
            ))
        }

        if (input.matches(DocType.GRZ.normaliseRegex)) {
            return listOf(ExtractedDocument(
                docType = DocType.GRZ,
                isValid = input.substring(6).toInt() > 0,
                value = input,
                isValidSetup = true,
            ))
        }

        if (input.matches(DocType.OGRN.normaliseRegex)) {
            val inn = validateOgrn(input)
            return listOf(ExtractedDocument(
                docType = DocType.OGRN,
                isValid = !inn.startsWith("!"),
                value = input,
                isValidSetup = true,
            ))
        }


        val trydriver = input.replace(" ", "")
        if (trydriver.matches(DocType.DRIVER_LICENSE.normaliseRegex) && trydriver.startsWith("9")) {
            return listOf(ExtractedDocument(
                docType = DocType.DRIVER_LICENSE,
                isValid = false,
                value = trydriver,
                isValidSetup = true,
            ))
        }

        val trypass = input.replace(" ", "")
        if (trypass.matches(DocType.PASSPORT_RF.normaliseRegex)) {
            return listOf(ExtractedDocument(
                docType = DocType.PASSPORT_RF,
                isValid = false,
                value = trypass,
                isValidSetup = true,
            ))
        }

        val trysnils = input.replace(" ", "")
        if (trysnils.matches(DocType.SNILS.normaliseRegex)) {
            return listOf(ExtractedDocument(
                docType = DocType.SNILS,
                isValid = true,
                value = trysnils,
                isValidSetup = true,
            ))
        }

//        if (digitsOnly(input).matches(DocType.PASSPORT_RF.normaliseRegex)) {
//            val ii = digitsOnly(input)
//
//            val f = ii.substring(2, 4).toInt()
//            val valid = f < 23
//
//
//            return listOf(ExtractedDocument(
//                docType = DocType.PASSPORT_RF,
//                value = ii,
//                isValidSetup = true,
//                isValid = valid,
//            ))
//        }


        val kirillic = listOf("А","В","Е","К","М","Н","О","Р","С","Т","У","Х")
        val latin = listOf("A","B","E","K","M","H","O","P","C","T","Y","X")
        var input2 = input

        latin.forEachIndexed { index, s ->
            input2 = input2.replace(s, kirillic[index])
        }

        /**
         * Вот тут уже можете начинать свою реализацию боевого кода
         */
        return listOf(getExactlyByType(input2))
    }


    fun validatePersonInn(value: String): String {
        val normalized = digitsOnly(value)
        return when {
            calcControlValue(normalized, listOf(7, 2, 4, 10, 3, 5, 9, 4, 6, 8), this::sumModifier) != normalized[10].numericValue() ||
                calcControlValue(normalized, listOf(3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8), this::sumModifier) != normalized[11].numericValue() -> "!" + value

            else -> value
        }
    }

    fun validateOgrn(value: String): String {
        val normalized = digitsOnly(value)
        return when {
            normalized.take(12).toLong() % 11 % 10 != normalized[12].numericValue().toLong() -> "!" + value
            else -> value
        }
    }

    private fun sumModifier(sum: Int) = sum % 11 % 10
    private fun digitsOnly(src: String? = null, minLen: Int? = null, filler: Char = '0'): String {
        val cleaned = (src ?: "").replace("""\D""".toRegex(), "")
        val result = StringBuffer()
        minLen?.let {
            if (cleaned.length < minLen) {
                repeat(minLen - cleaned.length) {
                    result.append(filler)
                }
            }
        }
        result.append(cleaned)
        return result.toString()
    }

    private fun calcControlValue(value: String, coefficients: List<Int>, sumModifier: (Int) -> Int = { it }): Int =
        coefficients.mapIndexed { i, c -> value[i].numericValue() * c }
            .sum()
            .let(sumModifier)


    private fun Char.numericValue() = Character.getNumericValue(this)
    private fun getExactlyByType(input: String): ExtractedDocument {
        DocType.values().forEach {
            if (input.matches(it.normaliseRegex)) {
                return@getExactlyByType ExtractedDocument(
                    docType = it,
                    isValid = true,
                    value = input,
                    isValidSetup = true,
                )
            }

        }
        return ExtractedDocument(
            docType = DocType.NOT_FOUND,
            isValid = true,
            value = input,
            isValidSetup = true,
        )
    }

    fun validateCompanyInn(value: String): String {
        val normalized = digitsOnly(value)
        return when {
            calcControlValue(normalized, listOf(2, 4, 10, 3, 5, 9, 4, 6, 8), this::sumModifier) != normalized[9].numericValue() -> "!" + value
            else -> value
        }
    }


    private fun qualificationTests(input: String): List<ExtractedDocument> {
        val normalized = input.trim('@').trim().replace("_", "").replace("-", "")

        if (normalized.startsWith("BTT0")) {
            return t0(normalized)
        }

        if (normalized.startsWith("BTT1")) {
            return t1(normalized)
        }

        if (normalized.startsWith("BTT2")) {
            return t2(normalized)
        }


        return emptyList()
    }

    private fun t2(input: String): List<ExtractedDocument> {
        if (input.matches("^BTT2\\d{4}$".toRegex())) {
            val valid = input.indexOf('5') != -1

            return listOf(
                ExtractedDocument(
                    DocType.T2,
                    value = input,
                    isValidSetup = true,
                    isValid = valid
                )
            )
        }

        return emptyList()
    }

    private fun t1(input: String): List<ExtractedDocument> {
        if (input.matches("^BTT1\\d{5}$".toRegex())) {
            return listOf(
                ExtractedDocument(
                    DocType.T1,
                    value = input,
                    isValidSetup = true,
                    isValid = true
                )
            )
        }

        if (input.matches("^BTT1\\d{4}$".toRegex())) {
            val f = input.substring(4)

            val valid = f[0] == '5' && f.last() == '7'

            return listOf(
                ExtractedDocument(
                    DocType.T1,
                    value = input,
                    isValidSetup = true,
                    isValid = valid
                )
            )
        }

        return emptyList()
    }

    private fun t0(input: String): List<ExtractedDocument> {
        if (input.matches("^BTT0\\d{5}$".toRegex())) {
            return listOf(
                ExtractedDocument(
                    DocType.T1,
                    value = input,
                    isValidSetup = true,
                    isValid = true
                )
            )
        }

        if (input.matches("^BTT0\\d{4}$".toRegex())) {
            val f = input.substring(4)

            val isFiveExist = f.indexOf('5') != -1

            val validT1 = f[0] == '5' && f.last() == '7'

            if (isFiveExist) {
                if (validT1) {
                    return listOf(
                        ExtractedDocument(
                            DocType.T1,
                            value = input,
                            isValidSetup = true,
                            isValid = true
                        ),
                        ExtractedDocument(
                            DocType.T2,
                            value = input,
                            isValidSetup = true,
                            isValid = true
                        )
                    )
                } else {
                    return listOf(
                        ExtractedDocument(
                            DocType.T2,
                            value = input,
                            isValidSetup = true,
                            isValid = true
                        ),
                        ExtractedDocument(
                            DocType.T1,
                            value = input,
                            isValidSetup = true,
                            isValid = false
                        )
                    )
                }
            }
        }

        return emptyList()
    }

    private fun preparedSampleTests(input: String): List<ExtractedDocument> {
        return when (input.split("BASE_SAMPLE1.")[1]) {
            "1" -> return listOf(ExtractedDocument(DocType.NOT_FOUND))
            "2" -> return listOf(
                // рандомы демонстрируют, что при условии INN_FL, PASSPORT_RF - проверяются только типы
                ExtractedDocument(
                    DocType.INN_FL,
                    isValidSetup = Random.nextBoolean(),
                    isValid = Random.nextBoolean(),
                    value = Random.nextInt().toString()
                ),
                ExtractedDocument(
                    DocType.PASSPORT_RF,
                    isValidSetup = Random.nextBoolean(),
                    isValid = Random.nextBoolean(),
                    value = Random.nextInt().toString()
                )
            )

            "3" -> return listOf(
                ExtractedDocument(
                    DocType.GRZ,
                    isValidSetup = true,
                    isValid = true,
                    value = Random.nextInt().toString()
                )
            )

            "4" -> return listOf(ExtractedDocument(DocType.INN_UL, value = "3456709873"))
            else -> emptyList()
        }
    }
}
