package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;


public class DataGraphInterface extends JFrame{
    private DataGraphPanel graphPanel;
    private Timer dataTimer;
    private int dataCounter;
    long previousTimestamp;
    long timeDiff;
    private boolean disableButtons;
    private ObjectController objectController;
    private List<Point> tracerPoints = new ArrayList<Point>();
    private static List<DataGraphInterface> simulationWindows = new ArrayList<>();
    public DataGraphInterface(ObjectController objectController, boolean disableButtons) {
        setTitle("Data Graph");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        //setResizable(false);
        //ObjectController objectController = new ObjectController(0, 300);
        this.objectController = objectController;
        this.disableButtons = disableButtons;
        graphPanel = new DataGraphPanel(40); // Число показываемых точек
        // Создаем DataGraphObserver и передаем его в DataGraphPanel
        DataGraphObserver dataGraphObserver = new DataGraphObserver(graphPanel, objectController);
        // Подписка GUI на обновления от панели графика
        graphPanel.addObserver(dataGraphObserver);
        // Установка количества точек для отображения на графике
        graphPanel.setObjectToDisplay(objectController); // Установка объекта для отображения на панели
        add(graphPanel, BorderLayout.CENTER);
        // Симуляция с обновлением каждые 100 миллисекунд
        dataCounter = 0;
        previousTimestamp = System.currentTimeMillis();
        simulationWindows.add(this);
        dataTimer = new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                double minX = 1; // Минимальное значение по оси X
                double minY = 1; // Минимальное значение по оси Y
                double maxX = 100; // Максимальное значение по оси X
                double maxY = 100; // Максимальное значение по оси Y

                double randomX = Math.random() * (maxX - minX) + minX; // Генерация случайной координаты X в пределах от 1 до 100
                double randomY = Math.random() * (maxY - minY) + minY; // Генерация случайной координаты Y в пределах от 1 до 100

                graphPanel.addDataPoint(randomX, randomY);
                dataCounter++;

                if (dataCounter >= 40) { // После 40 апдейтов останавливаемся
                    dataTimer.stop();
                    graphPanel.removeObserver(dataGraphObserver);
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // При закрытии удаляем GUI из списка наблюдателей
                graphPanel.removeObserver(dataGraphObserver);
            }
        });

        JButton showDataButton = new JButton("Show Last N Data");
        showDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayLastNDataPoints(5); // Вызов метода для отображения информации о последних 5 точках
            }

        });
        JButton accelerateButton = new JButton("Accelerate");
        accelerateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Вызов метода accelerate
                objectController.accelerate(0.1, Math.PI / 4); // Ускорение по углу 45 градусов
                dataTimer.start(); // Запуск таймера для начала обновления объекта и его отображения
            }
        });

        JButton decelerateButton = new JButton("Decelerate");
        decelerateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Вызов метода decelerate
                objectController.decelerate(0.1, Math.PI / 4); // Торможение по углу 45 градусов
                dataTimer.start(); // Запуск таймера для начала обновления объекта и его отображения
            }
        });
        JButton openSimulationButton = new JButton("Open New Simulation Window");
        openSimulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewSimulationWindow();
            }
        });
        JButton closeSimulationButton = new JButton("Close Simulation Windows");
        closeSimulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createCloseSimulationMenu(closeSimulationButton);
            }
        });


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openSimulationButton);
        buttonPanel.add(closeSimulationButton);
        if (disableButtons) {
            // Если кнопки нужно отключить, то удаляем их из панели
            buttonPanel.remove(openSimulationButton);
            buttonPanel.remove(closeSimulationButton);
        }
        buttonPanel.add(showDataButton);
        buttonPanel.add(accelerateButton);
        buttonPanel.add(decelerateButton);
        add(buttonPanel, BorderLayout.NORTH);
    }
    // Открытие нового окна с симуляцией
    public void openNewSimulationWindow() {
        DataGraphInterface newWindow = new DataGraphInterface(objectController, true);
        newWindow.start(); // Начать симуляцию в новом окне
    }

    public void displayLastNDataPoints(int n) {
        String dataInfo = graphPanel.getLastNDataInfo(n);

        // Отображение информации о последних N записанных точках в диалоговом окне
        JOptionPane.showMessageDialog(null, dataInfo, "Last N Data Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void start() {
        dataTimer.start();
        setVisible(true);
    }
    private void createCloseSimulationMenu(Component component) {
        int windowIndex = 1; // Индекс окна, начиная с единицы
        List<DataGraphInterface> windowsToClose = new ArrayList<>(); // Создаем список окон для закрытия

        for (DataGraphInterface window : simulationWindows) {
            if (window != this) { // Исключаем текущее окно из списка
                windowsToClose.add(window);
                windowIndex++; // Увеличиваем индекс для следующего окна
            }
        }

        if (!windowsToClose.isEmpty()) {
            String[] options = new String[windowsToClose.size()];
            for (int i = 0; i < windowsToClose.size(); i++) {
                options[i] = "Window " + (i + 1);
            }

            int choice = JOptionPane.showOptionDialog(
                    component,
                    "Select Window to Close:",
                    "Close Simulation Window",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice != JOptionPane.CLOSED_OPTION) {
                // Закрываем выбранное окно
                windowsToClose.get(choice).dispose();
                simulationWindows.remove(windowsToClose.get(choice));
            }
        } else {
            JOptionPane.showMessageDialog(
                    component,
                    "No simulation windows available to close.",
                    "Close Simulation Window",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ObjectController objectController = new ObjectController(0, 300);
                DataGraphInterface dataGraphInterface = new DataGraphInterface(objectController,false);
                dataGraphInterface.start();
            }
        });
    }
}