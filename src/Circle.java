public class Circle {
    private float x;
    private float y;
    private float radius;
    private int life;

    public Circle(float x, float y, float radius, int life) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.life = life;
    }

    public float getX() {
        return x;
    }

    public int getPhysicalX() {
        return (int) x;
    }

    public float getY() {
        return y;
    }

    public int getPhysicalY() {
        return (int) y;
    }

    public float getRadius() {
        return radius;
    }

    public int getPhysicalRadius() {
        return (int) radius;
    }

    public int getLifespan() {
        return life;
    }

    public void decreaseLifespan() {
        if (life > 0) {
            life--;
        }
    }
}
