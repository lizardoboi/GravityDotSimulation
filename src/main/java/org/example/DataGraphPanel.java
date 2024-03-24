package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class DataGraphPanel extends JPanel {
    private List<Observer> observers;
    private PhysicalObject objectToDisplay;
    private List<Double> dataPoints;
    private List<Long> timestamps;
    private List<Point> tracerPoints;
    private int maxDataPointsToShow;
    private int startIndex; // Начальный индекс отображаемых точек
    private JScrollBar scrollBar;
    private static final int GRAPH_WIDTH = 600; // Фиксированная ширина панели графика
    private static final int GRAPH_HEIGHT = 400; // Фиксированная высота панели графика
    private static final int MAX_POINTS_TO_DISPLAY = 1000; // Максимальное количество точек для отображения
    private int startIndexToDraw; // Начальный индекс отрисовки
    private int endIndexToDraw; // Конечный индекс отрисовки

    public DataGraphPanel(int maxDataPointsToShow) {
        this.maxDataPointsToShow = maxDataPointsToShow;
        this.dataPoints = new ArrayList<>();
        this.timestamps = new ArrayList<>();
        this.objectToDisplay = new PhysicalObject(0.0,0.0);
        tracerPoints = new ArrayList<>();
        this.startIndex = 0; // Изначально начинаем с первой точки
        this.startIndexToDraw = 0;
        this.endIndexToDraw = 0;
        this.observers = new ArrayList<>();

        // Создание и настройка ползунка для прокрутки
        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        scrollBar.setMinimum(0);
        scrollBar.setMaximum(0);
        scrollBar.setVisibleAmount(MAX_POINTS_TO_DISPLAY);
        scrollBar.addAdjustmentListener(e -> {
            int value = scrollBar.getValue();
            startIndexToDraw = value;
            endIndexToDraw = Math.min(dataPoints.size() - 1, value + maxDataPointsToShow - 1);
            repaint();
        });

        setLayout(new BorderLayout());
        add(scrollBar, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(GRAPH_WIDTH, GRAPH_HEIGHT));
    }
    public String getLastNDataInfo(int n) {
        List<Double> lastNData = getLastNDataPoints(n);
        List<Long> lastNTimestamps = getLastNTimestamps(n);

        StringBuilder info = new StringBuilder();
        for (int i = 0; i < lastNData.size(); i++) {
            double value = lastNData.get(i);
            long timestamp = lastNTimestamps.get(i);

            // Округляем значение до 3 знаков после запятой
            String formattedValue = String.format("%.3f", value);

            // Переводим timestamp в секунды и округляем до 3 знаков после запятой
            double seconds = timestamp / 1000.0;
            String formattedTimestamp = String.format("%.3f", seconds);

            info.append("Time: ").append(formattedTimestamp).append(" sec, Value: ").append(formattedValue).append("\n");
        }
        return info.toString();
    }
    public void addDataPoint(double x, double y) {
        dataPoints.add(y);
        timestamps.add(System.currentTimeMillis());

        if (dataPoints.size() > maxDataPointsToShow) {
            dataPoints.remove(0);
            timestamps.remove(0);
        }

        updateScrollBar();
        scrollBar.setMaximum(Math.max(0, dataPoints.size() - maxDataPointsToShow));
        scrollBar.setValue(scrollBar.getMaximum());
        repaint();
        notifyObservers(y, System.currentTimeMillis());
    }

    // Метод для добавления наблюдателя
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    // Метод для удаления наблюдателя
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    // Метод для уведомления всех наблюдателей
    private void notifyObservers(double value, long timestamp) {
        for (Observer observer : observers) {
            observer.updateGraph(value, timestamp);
        }
    }

    private void updateScrollBar() {
        int dataSize = dataPoints.size();
        scrollBar.setMaximum(Math.max(dataSize - maxDataPointsToShow, 0));
        scrollBar.setVisibleAmount(maxDataPointsToShow);
    }

    public List<Double> getLastNDataPoints(int n) {
        int size = dataPoints.size();
        if (n >= size) {
            return new ArrayList<>(dataPoints);
        } else {
            return new ArrayList<>(dataPoints.subList(size - n, size));
        }
    }

    public List<Long> getLastNTimestamps(int n) {
        int size = timestamps.size();
        if (n >= size) {
            return new ArrayList<>(timestamps);
        } else {
            return new ArrayList<>(timestamps.subList(size - n, size));
        }
    }
    public void setObjectToDisplay(ObjectController objectController) {
        this.objectToDisplay = objectController.getObject();
    }

    private void drawGrid(Graphics2D g2d, int width, int height, List<Double> dataPoints) {
        g2d.setColor(Color.BLACK);

        int numHorizontalLines = 10; // Количество горизонтальных линий сетки
        int numVerticalLines = 10; // Количество вертикальных линий сетки

        int stepX = width / numHorizontalLines;
        int stepY = height / numVerticalLines;

        // Вертикальные линии
        for (int i = 1; i < numVerticalLines; i++) {
            int y = height - i * stepY;
            g2d.drawLine(0, y, width, y);

            // Отображение значений на оси Y
            double value = ((double) i / numVerticalLines) * (getMaxValue(dataPoints) - getMinValue(dataPoints)) + getMinValue(dataPoints);
            String formattedValue = String.format("%.2f", value);
            g2d.drawString(formattedValue, 5, y-10);
        }

        // Горизонтальные линии
        for (int i = 1; i < numVerticalLines; i++) {
            int x = i * stepX;
            g2d.drawLine(x, 0, x, height);

            // Отображение значений на оси X
            int dataSize = dataPoints.size();
            int valueIndex = (i * dataSize) / numHorizontalLines;
            if (valueIndex < dataSize) {
                String formattedIndex = String.valueOf(valueIndex);
                FontMetrics fm = g2d.getFontMetrics();
                int strWidth = fm.stringWidth(formattedIndex);
                g2d.drawString(formattedIndex, x + 10, height - 25);
            }
        }
    }

    private double getMaxValue(List<Double> dataPoints) {
        return dataPoints.stream().mapToDouble(v -> v).max().orElse(0);
    }

    private double getMinValue(List<Double> dataPoints) {
        return dataPoints.stream().mapToDouble(v -> v).min().orElse(0);
    }
    public List<Point2D.Double> getDataPoints() {
        List<Point2D.Double> points = new ArrayList<>();

        int width = getWidth();
        int height = getHeight();

        int numPoints = dataPoints.size();
        int maxPoints = Math.min(numPoints, maxDataPointsToShow);

        // Преобразование данных в список точек
        for (int i = startIndexToDraw; i < startIndexToDraw + maxPoints - 1; i++) {
            int x = (int) ((double) (i - startIndexToDraw) / (maxPoints - 1) * width);
            int y = (int) ((1 - dataPoints.get(i) / 100.0) * height);

            points.add(new Point2D.Double(x, y));
        }

        return points;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.BLUE);

        int numPoints = dataPoints.size();
        int stepX = 0;

        if (Math.min(numPoints, maxDataPointsToShow) != 0) {
            stepX = width / Math.min(numPoints, maxDataPointsToShow);
        }

        startIndexToDraw = Math.max(0, numPoints - maxDataPointsToShow);
        endIndexToDraw = Math.min(numPoints - 1, startIndexToDraw + maxDataPointsToShow - 1);

        for (int i = startIndexToDraw; i < endIndexToDraw; i++) {
            int x1 = (i - startIndexToDraw) * stepX;
            int y1 = (int) ((1 - dataPoints.get(i) / 100.0) * height);
            int x2 = (i + 1 - startIndexToDraw) * stepX;
            int y2;
            if (height > 0) {
                y2 = (int) ((1 - dataPoints.get(i + 1) / 100) * height);
            } else {
                y2 = 1; //Чтобы избежать деления на ноль
            }
            g2d.fillOval(x1 - 2, y1 - 2, 15, 15);
            g2d.fillOval(x2 - 2, y2 - 2, 15, 15);
        }
        drawGrid(g2d, width, height, dataPoints);
        if (objectToDisplay != null) {
            Point2D.Double position = objectToDisplay.getPosition();
            int x = (int) position.getX();
            int y = (int) position.getY();
            // Нарисовать объект на панели
            g2d.setColor(Color.RED);
            g2d.fillOval(x, y, 10, 10); // Отображение объекта как красного круга
        }
        // Отрисовка трассировки
        g2d.setColor(new Color(255, 0, 0, 100)); // Прозрачный красный цвет для трассера
        // Добавление текущей позиции объекта в трассер
        Point2D.Double position = objectToDisplay.getPosition();
        double normalizedX = position.getX() / (double) getWidth();
        double normalizedY = position.getY() / (double) getHeight();

        int x = (int) (normalizedX * getWidth());
        int y = (int) (normalizedY * getHeight());

        tracerPoints.add(new Point(x, y));
        for (int i = 1; i < tracerPoints.size(); i++) {
            Point p1 = tracerPoints.get(i - 1);
            Point p2 = tracerPoints.get(i);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }
}