package dev.kabin.spring;

import dev.kabin.util.geometry.FloatCoordinates;
import dev.kabin.util.geometry.points.PointFloat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SpringHelpers extends JPanel {

    private final List<? extends FloatCoordinates>[] polygons;
    private final AtomicInteger counter = new AtomicInteger(0);


    @SafeVarargs
    public SpringHelpers(List<? extends FloatCoordinates>... polygons) {
        this.polygons = polygons;
    }

    @SafeVarargs
    public static void makePolygons(List<? extends FloatCoordinates>... polygons) {
        JFrame frame = new JFrame();
        frame.setTitle("DrawPoly");
        frame.setLocation(500, 500);
        frame.setSize(500, 750);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Container contentPane = frame.getContentPane();
        contentPane.add(new SpringHelpers(polygons));
        frame.show();
    }

    public static void main(String[] args) {
        makePolygons(List.of(
                PointFloat.immutable(0, 0),
                PointFloat.immutable(1, 0),
                PointFloat.immutable(1, 1),
                PointFloat.immutable(0, 1)
        ));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Arrays.stream(polygons).forEach(p -> {
            var swingPolygon = new Polygon();
            p.forEach(xy -> swingPolygon.addPoint(Math.round(xy.x() * 50f), Math.round(xy.y() * 50f)));
            swingPolygon.translate(200, 100 * counter.getAndIncrement() - 200);
            g.setColor(Color.BLUE);
            g.drawPolygon(swingPolygon);
        });
    }


}
