public class Constants {
    public static final class Main {
        public static final int FPS = 60;
        public static final int WINDOW_SIZE = 600;
    }

    public static final class Circles {
        public static final int START_RADIUS = 120;
        public static final int MAX_LIFESPAN = (int)(200 * (Constants.Main.FPS / 60.0));
        public static final float RADIUS_CHANGE = (float) 0.02;
    }

    public static final class Color {
        public static final double COLOR_RAMP = 0.00001;
    }

    public static final class Player {
        public static final double START_VELOCITY = 6.0;
        public static final int PLAYER_RADIUS = 20;
    }
}
