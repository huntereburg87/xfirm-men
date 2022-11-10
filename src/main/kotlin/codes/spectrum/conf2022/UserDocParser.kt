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

        /**
         * Вот тут уже можете начинать свою реализацию боевого кода
         */


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
