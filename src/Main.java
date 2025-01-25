import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


public class Main extends JPanel implements Runnable {
    /*
     * Copy into terminal : 
      
      javac -d out src/colors/*.java
      java -cp out colors.Main
     
     */


    // MAIN
    private Thread loopThread;

    private long gameStartTime = System.currentTimeMillis();
    private int millisecondsPassed = 0;
    
    // CIRCLES
    private double movingAngle = Math.PI * 2 * (Math.random() - 0.5);
    private double movingAngleChange = 0.015;
    private float lastAddedX, lastAddedY = 0;
    private float creationRadius = Constants.Circles.START_RADIUS;

    private Circle[] openCirclePoints;

    // COLORS 
    private float currentHue = 0;
    private Color currentColor;

    // MOUSE
    private int mouseX, mouseY = 0;
    private boolean clickedYet = false;

    // CHARACTER
    private double camX = 0;
    private double camY = 0;
    private double angleToMouse;
    private double characterVelocity = Constants.Player.START_VELOCITY;

    // SCORING
    private int score = 0;
    private boolean gameOver = false;
    private double scoreSizeMulti = 1.0;

    public Main() {
        openCirclePoints = new Circle[Constants.Circles.MAX_LIFESPAN + 10];
        openCirclePoints[0] = new Circle(0, 0, Constants.Circles.START_RADIUS, Constants.Circles.MAX_LIFESPAN);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                click();
            }
        });
    }

    public void click() {
        if (!clickedYet) {
            if (gameOver) {
                gameOver = false;
                camX = 0;
                camY = 0;
                characterVelocity = Constants.Player.START_VELOCITY;
                score = 0;
                scoreSizeMulti = 1.0;
                openCirclePoints = new Circle[Constants.Circles.MAX_LIFESPAN + 10];
                openCirclePoints[0] = new Circle(0, 0, Constants.Circles.START_RADIUS, Constants.Circles.MAX_LIFESPAN);
                movingAngle = Math.PI * 2 * (Math.random() - 0.5);
            } else {
                movingAngleChange = 0.04 * (Math.random() - 0.5);
                lastAddedX = 0;
                lastAddedY = 0;
                creationRadius = Constants.Circles.START_RADIUS;

                clickedYet = true;
                gameOver = false;
                gameStartTime = System.currentTimeMillis();
            }
        }
    }

    public void gameOver() {
        gameOver = true;
        clickedYet = false;
    }
    
    // copied from chatgpt
    public Color hsvToRgb(float hue, float saturation, float value) {
        int h = (int) (hue / 60) % 6;
        float f = (hue / 60) - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        float r = 0, g = 0, b = 0;

        switch (h) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            case 5 -> { r = value; g = p; b = q; }
        }

        // Convert to 0-255 range
        int red = Math.min(255, Math.max(0, Math.round(r * 255)));
        int green = Math.min(255, Math.max(0, Math.round(g * 255)));
        int blue = Math.min(255, Math.max(0, Math.round(b * 255)));

        return new Color(red, green, blue);
    }

    public void update() {
        // System.out.println(movingAngle);
        // System.out.println(camX + " " + camY);
        currentColor = hsvToRgb(currentHue, (float) 0.78, (float) 0.92);
        currentHue += Math.max(0.2, score * Constants.Color.COLOR_RAMP);
        currentHue = currentHue % 360;
        if (clickedYet) {
            millisecondsPassed = (int)(System.currentTimeMillis() - gameStartTime);
            score = (int)(30 * Math.pow(millisecondsPassed/1000.0, 2));
            scoreSizeMulti = Math.sqrt(score)/250;
            // System.out.println(scoreSizeMulti);
            scoreSizeMulti = Math.max(Math.min(scoreSizeMulti, 2.0), 1.0);

            if (Math.random() < 0.5 / Constants.Main.FPS) {
                movingAngleChange = 0.04* (Math.random() - 0.5);
            }
            movingAngle += movingAngleChange;
            
            characterVelocity *= 1.0001;
            angleToMouse = Math.atan2((double) - (mouseY - Constants.Main.WINDOW_SIZE / 2), (double) (mouseX - Constants.Main.WINDOW_SIZE / 2));
            camX += characterVelocity * Math.cos(angleToMouse);
            camY += characterVelocity * -Math.sin(angleToMouse);
    
            if (creationRadius > Constants.Player.PLAYER_RADIUS + 14) {
                creationRadius -= Constants.Circles.RADIUS_CHANGE;
            }

            addNewCircle(new Circle(
            lastAddedX + (float)(characterVelocity * Math.cos(movingAngle)),
            lastAddedY + (float)(characterVelocity * Math.sin(movingAngle)),
            creationRadius,
            Constants.Circles.MAX_LIFESPAN
            ));

            for (int i = 0; i < openCirclePoints.length; i++) {
                Circle circle = openCirclePoints[i];
                
                if (circle != null) {
                    circle.decreaseLifespan();
                    if (circle.getLifespan() <= 0) {
                        openCirclePoints[i] = null;
                        i--;
                    }
                }
            }

            if (!isInBounds()) {
                gameOver();
            }

        }
    }
    
    public void addNewCircle(Circle circ) {
        for (int i = 0; i < openCirclePoints.length; i++) {
            if (openCirclePoints[i] == null){
                openCirclePoints[i] = circ;
                lastAddedX = circ.getX();
                lastAddedY = circ.getY();
                break;
            }
        }
    }

    public boolean isInBounds() {
        // to PASS collisions, the character must be inside at least one circle
        // if it is, return true; iterate until true or until exhausted, then =false
        
        // **** May need to add character radius to camX and camY because not sure if top left calculations...
        for (int i = 0; i < openCirclePoints.length; i++) {
            Circle circle = openCirclePoints[i];

            if (circle != null) {
                double distToCenter = Math.sqrt(Math.pow(circle.getPhysicalX() - camX, 2) + Math.pow(circle.getPhysicalY() - camY, 2));
                if (distToCenter < circle.getPhysicalRadius() - Constants.Player.PLAYER_RADIUS) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void paintComponent(Graphics gOld){
        super.paintComponent(gOld);

        Graphics2D g2d = (Graphics2D) gOld;

        //draw background color
        g2d.setColor(currentColor);
        g2d.fillRect(0, 0, Constants.Main.WINDOW_SIZE, Constants.Main.WINDOW_SIZE);

        //draw circles
        g2d.setColor(Color.BLACK);
        for (Circle circle : openCirclePoints) {
            if (circle != null) {
                int radius = circle.getPhysicalRadius();

                //should add optimizations for circles off screen that dont need to be rendered
                g2d.fillOval(Constants.Main.WINDOW_SIZE / 2 - (int) camX + (circle.getPhysicalX() - radius), Constants.Main.WINDOW_SIZE / 2 - (int) camY + (circle.getPhysicalY() - radius), radius * 2, radius * 2);
            }
        }

        //draw character (position in the world is just camX) at center
        g2d.setColor(Color.WHITE);
        g2d.fillOval(Constants.Main.WINDOW_SIZE / 2 - Constants.Player.PLAYER_RADIUS, Constants.Main.WINDOW_SIZE / 2 - Constants.Player.PLAYER_RADIUS, 2 * Constants.Player.PLAYER_RADIUS, 2 * Constants.Player.PLAYER_RADIUS);

        //draw start arrow
        if (!clickedYet && !gameOver) {
            int off = 30;
            int line = 20;

            g2d.setStroke(new BasicStroke(5));
            
            int tipPointX = (int) (Math.cos(movingAngle) * (Constants.Circles.START_RADIUS + off)) + Constants.Main.WINDOW_SIZE / 2;
            int tipPointY = (int) (Math.sin(movingAngle) * (Constants.Circles.START_RADIUS + off)) + Constants.Main.WINDOW_SIZE / 2;

            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.drawLine(tipPointX, tipPointY + 5, tipPointX + (int)((line) * -Math.cos(movingAngle + Math.PI / 4)), tipPointY + 5 + (int)((line) * -Math.sin(movingAngle + Math.PI / 4)));
            g2d.drawLine(tipPointX, tipPointY + 5, tipPointX + (int)((line) * -Math.cos(movingAngle - Math.PI / 4)), tipPointY + 5 + (int)((line) * -Math.sin(movingAngle - Math.PI / 4)));
            
            g2d.setColor(Color.WHITE);
            g2d.drawLine(tipPointX, tipPointY, tipPointX + (int)((line) * -Math.cos(movingAngle + Math.PI / 4)), tipPointY + (int)((line) * -Math.sin(movingAngle + Math.PI / 4)));
            g2d.drawLine(tipPointX, tipPointY, tipPointX + (int)((line) * -Math.cos(movingAngle - Math.PI / 4)), tipPointY + (int)((line) * -Math.sin(movingAngle - Math.PI / 4)));
        }

        //draw top of screen text
        Font font = new Font("SansSerif", Font.BOLD, (int)(40 * scoreSizeMulti));
        g2d.setFont(font);
        String str;

        if (!clickedYet && !gameOver) {
            str = "CLICK TO PLAY";
        } else {
            str = "" + score;
        }
        
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(str);
        int xText = (Constants.Main.WINDOW_SIZE - textWidth) / 2;
        int yText = 10 + font.getSize();
        var preTransform = g2d.getTransform();
        if (clickedYet || gameOver) {
            double theta = 0.2 * Math.min(Math.max(Math.sqrt(score) / 125 - 2, 0), 2.5) * Math.sin(millisecondsPassed / 1000.0); //temp
            g2d.rotate(theta, Constants.Main.WINDOW_SIZE / 2, 4 + yText - font.getSize() / 2);
        }

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(str, xText, yText + 5);
        g2d.setColor(new Color(255, 255, 255));
        g2d.drawString(str, xText, yText);
        
        g2d.setTransform(preTransform);

        if (gameOver) {
            Font overFont = new Font("SansSerif", Font.BOLD, 50);
            g2d.setFont(overFont);            
            str = "GAME OVER";
            metrics = g2d.getFontMetrics(overFont);
            textWidth = metrics.stringWidth(str);
            xText = (Constants.Main.WINDOW_SIZE - textWidth) / 2;
            yText = Constants.Main.WINDOW_SIZE / 2 - 50;

            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.drawString(str, xText, yText + 5);
            g2d.setColor(new Color(255, 255, 255));
            g2d.drawString(str, xText, yText);
        }

        g2d.dispose();
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(Constants.Main.WINDOW_SIZE, Constants.Main.WINDOW_SIZE)); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        Main panel = new Main();
        panel.setPreferredSize(new Dimension(Constants.Main.WINDOW_SIZE, Constants.Main.WINDOW_SIZE));
        panel.startLoop();

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public void startLoop(){
        loopThread = new Thread(this);
        loopThread.start();
    }

    @Override
    public void run() {

        int time = 1000/Constants.Main.FPS;

        while (loopThread != null) {
            update();
            repaint();

            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
