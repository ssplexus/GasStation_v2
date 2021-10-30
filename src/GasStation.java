import java.io.*;

/**
 * Класс автоколонки
 */
public class GasStation
{
    // Константы марок топлива
    public static final String FUEL_AI_92 = "Аи-92";
    public static final String FUEL_AI_95 = "Аи-95";

    // Константа имени файла журнала
    private static final String LOG_FILE_NAME = "log.txt";

    // Константа лимит резервуара
    private static final int TANK_LIMIT = 1000;
    // Журнал действий
    private static StringBuilder gasStationLog;
    // Число колонок
    private static int stationsCnt = 0;

    // Количество топлива в резервуарах
    private static int tank_ai_92 = 1000;
    private static int tank_ai_95 = 1000;

    // Номер колонки
    private final int stationNum;
    // Лимит колонки
    private int stationLimit;

    GasStation()
    {
        this(100);
    }
    GasStation(int limit)
    {
        this.stationLimit = limit;
        stationNum = ++stationsCnt;
        // Если объект журнала не создан, то создаём
        if(gasStationLog == null) gasStationLog = new StringBuilder();
    }

    /**
     *  Метод получения номера колонки
     * @return - возвращает номер колонки
     */
    public int getStationNum()
    {
        return stationNum;
    }

    /**
     * Статический метод вывода информации об остатках топлива в резервуарах
     */
    public static void getTanksRem()
    {
        System.out.println("В резервуарах осталось:");
        System.out.println(String.format("%s - %d литров", FUEL_AI_92, tank_ai_92));
        System.out.println(String.format("%s - %d литров", FUEL_AI_95, tank_ai_95));
    }

    /**
     * Метод для осуществления заправки автомобиля
     *
     * @param _args - массив входных аргументов:
     *              1 - марка топлива (92 или 95)
     *              2 - номер колонки
     *              3 - количество топлива
     * @return - результат отработки метода
     */
    public String gasFilling(int _args[]) throws GasStationException
    {
        // Если какой-либо из аргументов = 0, то выход с отрицательным результатом
        if(_args[0] <= 0 || _args[1] <= 0 || _args[2] <= 0)
            throw new GasStationException("Неверные аргументы!");

        // Флаг марки топлива Аи-92
        boolean isAi92;
        // Если запрос не для этой колонки, то выход с отрицательным результатом
        if(_args[1] != stationNum)
            throw new GasStationException("Запрос не для этой колонки");

        // Если марка топлива не Аи-92, значит считаем, что это Аи-95
        isAi92 = _args[0] == 92;

        StringBuilder msg = new StringBuilder();
        int val = _args[2];

        // Получение значения резервуара запрашиваемой марки топлива во временную переменную
        int tank = (isAi92?tank_ai_92:tank_ai_95);

        // Если запрошено больше чем установленный порог для колонки
        if(val > stationLimit)
        {
            val = stationLimit;
            msg.append(String.format("На %d колонке установлен лимит в %d литров\n",stationNum, stationLimit));
        }

        // Если запрошено больше чем есть в резервуаре
        if(val > tank)
            throw new GasStationException(msg.append(String.format("Невозможно осуществить заправку, в резервуаре %s нет топлива\n", (isAi92) ? FUEL_AI_92 : FUEL_AI_95)).toString());
        else
            tank -= val;

        // Обновление значения количества топлива в резервуаре
        if(isAi92)
            tank_ai_92 = tank;
        else
            tank_ai_95 = tank;

        if (tank == 0)         // Если запрошено ровно столько сколько было в резервуаре
            msg.append(String.format("На %d колонке заправлено %d литров топлива %s\n резервуар пуст\n", getStationNum(), val, (isAi92)?FUEL_AI_92:FUEL_AI_95));
        else
            msg.append(String.format("На %d колонке заправлено %d литров топлива %s\nв резервуаре осталось %d литров\n", getStationNum(), val, (isAi92)?FUEL_AI_92:FUEL_AI_95, tank));

        return msg.toString();
    }

    /**
     * Статический метод для осуществления пополнения резервуара
     *
     * @param _args - массив входных аргументов:
     *              1 - марка топлива (92 или 95)
     *              2 - не используется
     *              3 - количество топлива
     * @return - результат отработки метода
     */
    public static String tankFilling(int _args[]) throws GasStationException {
        // Если какой-либо из аргументов = 0, то выход с отрицательным результатом
        if(_args[0] <= 0 || _args[2] <= 0) throw new GasStationException("Неверные аргументы!");
        // Флаг марки топлива Аи-92
        boolean isAi92;
        // Если марка топлива не Аи-92, значит считаем, что это Аи-95
        isAi92 = _args[0] == 92;

        StringBuilder msg = new StringBuilder();
        int val = _args[2];

        // Получение значения резервуара запрашиваемой марки топлива во временную переменную
        int tank = (isAi92?tank_ai_92:tank_ai_95);

        try
        {
            // Если превышение лимита резервуара
            if(val + tank > TANK_LIMIT)
            {
                val = TANK_LIMIT - tank;
                tank = TANK_LIMIT;

                // Обновление значения количества топлива в резервуаре
                if(isAi92)
                    tank_ai_92 = tank;
                else
                    tank_ai_95 = tank;

                throw new GasStationException(String.format("Лимит резервуара %s - %d, будет залито %d литров из %d\n", isAi92?FUEL_AI_92:FUEL_AI_95, TANK_LIMIT, val, _args[2]));
            }
            else
                tank += val;
        }
        catch (GasStationException ex)
        {
            msg.append(ex.getMessage());
        }
        finally
        {
            // Обновление значения количества топлива в резервуаре
            if(isAi92)
                tank_ai_92 = tank;
            else
                tank_ai_95 = tank;
        }

        msg.append(String.format("Резервуар %s пополнен на %d литров\n", isAi92?FUEL_AI_92:FUEL_AI_95, val));
        return msg.toString();
    }

    /**
     *  Статический метод для записи в журнал действий
     * @param _msg - записываемое действие
     */
    public static void stationLogger(String _msg) throws GasStationException
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(LOG_FILE_NAME),true)))
        {
            bw.write(_msg);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Статический метод для вывода журнала
     */
    public static void printLog()
    {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(LOG_FILE_NAME))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Ошибка: Файл журнала не найден!");
        } catch (IOException e) {
            System.out.println("Ошибка: Невозможно прочитать файл журнала!");
        }
    }
}
