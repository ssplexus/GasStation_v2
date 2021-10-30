
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Основной класс
 */

public class Main
{
    public static void main(String[] args)
    {
        // Создание массива колонок
        GasStation []station = new GasStation[4];
        station[0] = new GasStation();
        station[1] = new GasStation();
        station[2] = new GasStation();
        station[3] = new GasStation();

        // Флаг выхода из программы
        boolean isExitFlag;

        Scanner scanner = new Scanner(System.in);
        System.out.println("Добро пожаловать на нашу АЗС!");
        printHelp();

        // Цикл запросов
        do
        {
            isExitFlag = false;
            System.out.println("Введите запрос:");
            String request = scanner.nextLine() + "\n";

            // Определние команд в запросе
            switch(commandParser(request))
            {
                // вывод помощи
                case 3:
                    printHelp();
                    break;
                // Вывод остатка топлива в резервуарах
                case 2:
                    GasStation.getTanksRem();
                    break;
                // Вывод журнала
                case 1:
                    GasStation.printLog();
                    break;
                //Выход из программы
                case 0:
                    isExitFlag = true;
                    break;
                // Заправка или пополнение резервуара
                default:

                    // Регистрация запроса в журнале действий
                    try
                    {
                        GasStation.stationLogger(gasStationRefill(station, request));
                    }
                    catch (GasStationException ex)
                    {
                        System.out.println(ex.getMessage());
                    }
            }
        }
        while (!isExitFlag);
    }

    /**
     * Метот определения команд пользователя
     *
     * @param _request - запрос пользователя
     * @return - возвращается номер команды:
     * 0 - выход из программы
     * 1 - вывести журнал
     * 2 - вывести остаток топлива в резервуарах
     * 3 - вывести помощь
     */
    private static int commandParser(String _request)
    {
        Pattern pattern = Pattern.compile("\\b[Вв]{1}ыход\\b");
        Matcher matcher = pattern.matcher(_request);
        if (matcher.find() || _request.charAt(0) == '0') return 0;

        pattern = Pattern.compile("\\b[Лл]{1}ог\\b");
        matcher = pattern.matcher(_request);
        if (matcher.find()) return 1;

        pattern = Pattern.compile("\\b[Оо]{1}статок\\b");
        matcher = pattern.matcher(_request);
        if (matcher.find()) return 2;

        pattern = Pattern.compile("\\b[Hh]{1}elp\\b");
        matcher = pattern.matcher(_request);
        if (matcher.find()) return 3;

        return -1;
    }

    /**
     * Метот разбора запроса к заправочной станции
     *
     * @param _request - запрос пользователя
     * @return - возвращается массив из 3-х аргументов:
     * 1 - марка топлива (95 или 92)
     * 2 - номер колонки, если 0 то считается что это команда на пополнение резервуара
     * 3 - количество топлива
     */
    private static int[] stationRequestParser(String _request)
    {
        int[] resultArr = new int[]{0, 0, 0};
        int resNum = 0;
        // Флаг запроса пополнения резервуара
        boolean isRefillTank = false;

        String stationRequest = _request;

        Pattern pattern = Pattern.compile("\\b[Аа]и-{0,1}9([25])\\b");
        Matcher matcher = pattern.matcher(stationRequest);

        if (matcher.find())
        {
            resultArr[resNum++] = (stationRequest.charAt(matcher.end() - 1) == '2') ? 92 : 95;
            stationRequest = matcher.replaceAll("");
        }
        else
            return resultArr;

        pattern = Pattern.compile("\\b[Пп]{1}ополнение\\b");
        matcher = pattern.matcher(_request);

        if (matcher.find()) isRefillTank = true;

        pattern = Pattern.compile("[0-9]+");
        matcher = pattern.matcher(stationRequest);

        if (isRefillTank)
            resultArr[++resNum] = (matcher.find()) ? Integer.parseInt(stationRequest.substring(matcher.start(), matcher.end())) : 0;
        else
        {
            while (matcher.find() && resNum < 3)
            {
                resultArr[resNum] = Integer.parseInt(stationRequest.substring(matcher.start(), matcher.end()));
                resNum++;
            }

            if (resNum == 2)
            {
                pattern = Pattern.compile("\\b[Дд]о\\sполного\\b");
                matcher = pattern.matcher(stationRequest);
                if (matcher.find())
                {
                    Random random = new Random();
                    resultArr[resNum] = random.nextInt(101) + 1;
                }
            }

        }
        return resultArr;
    }

    /**
     * Метод работы заправки
     * @param station - массив колонок
     * @param request - запрос к заправке
     * @return - сообщение о результате
     */
    private static String gasStationRefill(GasStation[]station, String request)
    {
        String errMsg = "";
        StringBuilder logMsg = new StringBuilder();
        logMsg.append(request);
        // Разбор запроса и получение аргументов
        int[] resArr = stationRequestParser(request);
        // Если номер колонки не определён значит это пополнение резервуара
        if(resArr[1] == 0)
        {
            try
            {
                logMsg.append(GasStation.tankFilling(resArr));
            }
            catch (GasStationException ex)
            {
                errMsg = ex.getMessage();
            }
        }
        else
        {
            // Заправка
            try
            {
                logMsg.append(station[resArr[1] - 1].gasFilling(resArr));
            }
            catch (GasStationException ex)
            {
                errMsg = ex.getMessage();
            }
            catch (NullPointerException ex)
            {
                errMsg = String.format("Колонка %d недоступна", resArr[1]);
            }
            catch (IndexOutOfBoundsException ex)
            {
                errMsg = String.format("Нет колонки с номером %d", resArr[1]);
            }
        }
        logMsg.append(errMsg.isEmpty()? "" : errMsg + "\nНеверный запрос!");
        System.out.println(logMsg);
        return logMsg.toString();
    }

    /**
     * Метод вывода помощи
     */
    private static void printHelp()
    {
        System.out.println(new StringBuilder().
                append("1) Для заправки введите запрос вида: \"<n>-я колонка <марка топлива> <n> литров\";\n").
                append("2) Для пополнения резервуара задайте запрос вида: \"Пополнение резервуара <марка топлива> на <n> литров\";\n").
                append("Доступные марки топлива - \"Аи-92\" и \"Аи-95\";\n").
                append("Количество колонок - 4;\n").
                append("3) Для вывода остатков топлива в резервуарах введите \"Остаток\";\n").
                append("4) Для вывода лога введите \"Лог\";\n").
                append("5) Для помощи введите \"Help\";\n").
                append("6) Для выхода введите \"Выход\" или \"0\"\n"));

    }
}
