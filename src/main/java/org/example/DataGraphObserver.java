package org.example;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class DataGraphObserver implements Observer {
    private DataGraphPanel graphPanel;
    private ObjectController object; // Поле для хранения объекта ObjectController
    private List<Point> tracerPoints = new ArrayList<>();  // Поле для хранения списка точек tracerPoints

    public DataGraphObserver(DataGraphPanel graphPanel, ObjectController object) {
        this.graphPanel = graphPanel;
        this.object = object; // Инициализация объекта ObjectController
    }
    private Point2D.Double calculateGravity(ObjectController objectController, List<Point2D.Double> pointsOnGraph) {
        double totalForceX = 0;
        double totalForceY = 0;

        for (Point2D.Double point : pointsOnGraph) {
            double distanceX = point.getX() - objectController.getPosition().getX();
            double distanceY = point.getY() - objectController.getPosition().getY();

            // Расчет расстояния между точками
            double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

            // Вычисление гравитационной силы
            double force = 1 / (distance * distance);

            // Вычисление направления гравитационной силы
            double angle = Math.atan2(distanceY, distanceX);
            double forceX = force * Math.cos(angle);
            double forceY = force * Math.sin(angle);

            totalForceX += forceX;
            totalForceY += forceY;
        }

        return new Point2D.Double(totalForceX, totalForceY);
    }

    @Override
    public void updateGraph(double value, long timestamp) {
        // Гравитационное воздействие
        List<Point2D.Double> pointsOnGraph = graphPanel.getDataPoints(); // Получение списка точек графика
        Point2D.Double gravity = calculateGravity(object, pointsOnGraph);
        object.applyForce(gravity);

        if (!pointsOnGraph.isEmpty()) {
            // Получение последней точки на графике
            Point2D.Double lastPoint = pointsOnGraph.get(pointsOnGraph.size() - 1);

            // Применение гравитационной силы к объекту на основе последней точки
            double forceFactor = 0.1; // Коэффициент для управления силой гравитации
            double newX = object.getPosition().getX() + (lastPoint.getX() - object.getPosition().getX()) * forceFactor;
            double newY = object.getPosition().getY() + (lastPoint.getY() - object.getPosition().getY()) * forceFactor;
            // Обновление позиции объекта
            object.setPosition(newX, newY);
            // Добавление текущей позиции объекта в список точек трейсера
            tracerPoints.add(new Point((int) newX, (int) newY));
        }
        if (pointsOnGraph.isEmpty()) {
            // Вне зависимости от наличия стационарных точек, объект всегда двигается вперед
            double velocityFactor = 0.5; // Пример скорости движения объекта
            double newX = object.getPosition().getX() + object.getVelocity().getX() * velocityFactor;
            double newY = object.getPosition().getY() + object.getVelocity().getY() * velocityFactor;

            object.setPosition(newX, newY);
            tracerPoints.add(new Point((int) newX, (int) newY)); // Добавление текущей позиции в трейсер точек
        }
        // Обновление объекта и графика
        object.updateObject();
        graphPanel.repaint();
    }
}