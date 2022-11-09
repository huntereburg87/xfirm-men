#### TBIT: The best in the tests

Практическое задание на конференцию 2022 года - тестовый бой.

#### Видео

Перед прочтение **ОБЯЗАТЕЛЬНО** посмотрите видео анонса:

[![Анонс тестового боя 2022](http://img.youtube.com/vi/OjEWA1IDXg0/0.jpg)](http://www.youtube.com/watch?v=OjEWA1IDXg0)

#### Задача

1. Реализовать парсер текстовых описаний документов
2. Написать как можно больше тестов на проверку других участников. Ваш код **В ПЕРВУЮ ОЧЕРЕДЬ** должен удовлетворять
   этим
   тестам (иначе победные очки не начисляются)

#### Как выглядят описания тестов

Участники пишут свои тесты в локальном файле [local.csv](local.csv). Тест состоит их входной строки, ожидаемого
результата и (опционально) комментария. Класс, использующийся для описания
теста : [TestDesc.kt](src/main/kotlin/codes/spectrum/conf2022/input/TestDesc.kt). Пример строки, описывающей
тест (зеленым выделены опциональные поля): ![img_5.png](images/img_5.png)

1. `!` - флаг отключения теста. Если добавить его в начале строки - тест будет выключен. **Необязательное поле**
2. `Иванов И.И. 1234 567890` - входная строка. Именно она будет передаваться в парсеры документов. **Обязательное поле**
   .
3. `->` - символ разделитель входной строки и ожидаемого результата. Не может встречаться больше одного раза. **
   Обязательное поле**.
4. `==` - ограничение на вхождение в результат. Описывают, как будет проверяться наличие и порядок полученных
   документов. **Необязательное** поле - **ПО УМОЛЧАНИЮ** будет выставлено **==**. Бывают следующими:

| Ограничение на вхождение | Описание                                                                                           |
|--------------------------|----------------------------------------------------------------------------------------------------|
| **==**                   | **ТОЛЬКО** ожидаемые документы в **УСТАНОВЛЕННОМ** порядке                                         | 
| **~=**                   | **СОДЕРЖАТ** ожидаемые документы (но могут содержать документы помимо) в **УСТАНОВЛЕННОМ** порядке | 
| **=?**                   | **ТОЛЬКО** ожидаемые документы в **ЛЮБОМ** порядке                                                 | 
| **~?**                   | **СОДЕРЖАТ** ожидаемые документы (но могут содержать документы помимо) в **ЛЮБОМ** порядке         |

5. `PASSPORT_RF` - тип документа. Все возможные типы документов перечислены в
   классе [DocType.kt](src/main/kotlin/codes/spectrum/conf2022/doc_type/DocType.kt). Название типов документов **
   ДОЛЖНЫ** совпадать с названием enum констант! **Обязательное поле**.
6. `+` - знак валидации. Может быть три состояния - может `отсутствовать`, `-` - не валидный документ, `+` - валидный
   документ.
   Устанавливается только в том случае, если действительно проверяется **ВАЛИДНОСТЬ** нормализованного номера документа.
   Например - валидный документ - у которого сходится контрольная сумма, не валидный - у которого не сходится.        
   **Необязательное поле.**
7. `:1234567890` - ожидаемый **НОРМАЛИЗОВАННЫЙ** номер документ. Ожидаемый нормализованный номер должен соответствовать
   структуре нормализованного документа этого типа - при парсинге файлов с тестами это проверяется. Регулярки на
   нормализованные номер можно найти в файле с описаниями типом
   документа [DocType.kt](src/main/kotlin/codes/spectrum/conf2022/doc_type/DocType.kt). `:` - символ
   разделитель `тип документа + валидации` и `номера`. **Необязательное** поле.
8. `#комментарий` - поле с комментарием. Все, что идет после символа `#` - комментарий к тесту. Отображается при завале
   теста. Также символ `#` можно использовать для написания комментариев в файле [local.csv](local.csv).**Необязательное
   поле.**

На одну входную строку может быть несколько ожидаемых документов, например строка `1234567890 ->PASSPORT_RF, INN_UL` -
валидное описание теста. Символ `,` используется в качестве разделителя, когда ожидается более чем один документ.

#### Как будет происходить обмен тестами

Раз в 2 минут все файлы с локальными тестами (**local.csv**), **КОТОРЫЕ БЫЛИ ЗАПУШЕНЫ**, со всех репозиториев будут
объединяться в один общий
тестовый набор (**main.csv**).  
При запуске [MainTest](src/test/kotlin/codes/spectrum/conf2022/MainTest.kt) файл **main.csv**
будет подтянут из общего репозитория.

Таким образом:

1. При запуске [BaseTest](src/test/kotlin/codes/spectrum/conf2022/BaseTest.kt) - запускаются только базовые тесты
2. При запуске [LocalTest](src/test/kotlin/codes/spectrum/conf2022/LocalTest.kt) - запускаются базовые и Ваши локальные
   тесты
3. При запуске [MainTest](src/test/kotlin/codes/spectrum/conf2022/MainTest.kt) - запускаются базовые, Ваши локальные
   тесты и общие тесты (кроме Ваших). Также выдает отчет (в консоль, и в файл [report.md](report.md))

#### С чего начать

1. Сделать форк данного репозитория
2. Можно сделать приватным, главное, чтобы **Lokbugs** был в **Collaborators** - чтобы у CI была возможность
   собирать/возвращать объединенные файлы с тестами
   ![img.png](images/img.png)
3. Всю разработку вести в ветке **main**
4. Ознакомьтесь с основным интерфейсом парсера [IDocParser](src/main/kotlin/codes/spectrum/conf2022/input/IDocParser.kt)
   . Вам необходимо написать реализацию для
   этого интерфейса
5. Ознакомьтесь с основным классом тестов - [TestBase](src/test/kotlin/codes/spectrum/conf2022/TestBase.kt). Через этот
   класс будут запускаться **ВСЕ** тесты (после валидации входных файлов и их парсинга). **ЗАПОЛНИТЕ** переменную
   **MY_LOGIN**
   Вашим логином на GitHub. Также в [TestBase](src/test/kotlin/codes/spectrum/conf2022/TestBase.kt) задается парсер
   участника: ![img_1.png](images/img_1.png)
6. Изучить базовые тесты в файле **base.csv** и запустить их
   через [BaseTest](src/test/kotlin/codes/spectrum/conf2022/BaseTest.kt). Реализовать свой парсер так, чтобы базовые
   тесты прошли.
7. Изучить структуру файла с описаниями тестов (**local.csv**). Напишите свой первый (_тестовый_ тест) -
   запустите [LocalTest](src/test/kotlin/codes/spectrum/conf2022/LocalTest.kt), убедитесь, что ничего не работает. Не
   забудьте удалить этот тест!

#### Рабочий цикл отправки тестов

1. Пишите тесты в локальном файле [local.csv](local.csv)
2. Запускаете локальные тесты [LocalTest](src/test/kotlin/codes/spectrum/conf2022/LocalTest.kt)
3. Если валидация файла прошла успешна (нет ошибки о невалидном файле [local.csv](local.csv)) - коммит и пуш
   файла [local.csv](local.csv) в Ваш репозиторий

#### Советы и нюансы

1. Если не пройдены базовые тесты - участник исключается из общего зачета. Поэтому в **ПЕРВУЮ ОЧЕРЕДЬ** должны быть
   реализованы базовые тесты (благо они простые).
2. Баллы за успешные удары по противнику начисляются в зависимости от времени публикации теста. Чем раньше - тем выше
   балл. После первого часа коэффициент сильно падает, после второго - тесты вовсе не учитываются. Поэтому
   целесообразнее сначала писать тесты, в последний час - полтора доделывать реализацию парсера.
3. Баллы за тесты, которые Вы сами не выполнили - также не начисляются.
4. Не пытайтесь сделать парсер на все типы документов - Вы наверняка напропускаете ударов от участников, которые
   реализуют один документ, но качественно (много тестов, много фич проверяется)