import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PixelPlayer {
    private static final int TARGET_WIDTH = 80;
    private static final int TARGET_HEIGHT = 40;
    private static final char[] CHARS = {'@', '%', '#', '*', '+', '=', '-', '.'};
    private static Process audioProcess;
    
    public static void main(String[] args) {
        System.out.println("Dev by Xiaobocm");
        System.out.println("Are you ready?(yes)");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String input = reader.readLine();
            if (!input.trim().equalsIgnoreCase("yes")) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        clearScreen();
        System.out.println("Dev by Xiaobocm");
        
        playAudioAsync();
        
        long frameDelay = 100;
        long startTime = System.currentTimeMillis();
        
        for (int i = 1; i <= 940; i++) {
            File pngFile = new File("resources/png/" + i + ".png");
            if (pngFile.exists()) {
                String ascii = convertToAscii(pngFile);
                moveCursor(0, 1);
                System.out.print(ascii);
                
                long elapsed = System.currentTimeMillis() - startTime;
                long targetTime = i * frameDelay;
                long sleepTime = targetTime - elapsed;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        stopAudio();
        clearScreen();
        System.out.println("Dev by Xiaobocm");
    }
    
    private static void playAudioAsync() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                audioProcess = new ProcessBuilder("cmd", "/c", "start", "/min", "wmplayer", new File("resources/mp3/awa.wav").getAbsolutePath()).start();
            } else {
                audioProcess = new ProcessBuilder("ffplay", "-nodisp", "-autoexit", "resources/mp3/awa.wav").start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void stopAudio() {
        if (audioProcess != null && audioProcess.isAlive()) {
            audioProcess.destroyForcibly();
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("taskkill /f /im wmplayer.exe");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String convertToAscii(File imageFile) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedImage img = ImageIO.read(imageFile);
            int w = img.getWidth();
            int h = img.getHeight();
            double ratio = (double) w / h;
            int nw, nh;
            if (ratio > 2) {
                nw = TARGET_WIDTH;
                nh = (int) (TARGET_WIDTH / ratio);
            } else {
                nh = TARGET_HEIGHT;
                nw = (int) (TARGET_HEIGHT * ratio);
            }
            
            BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, nw, nh, null);
            g.dispose();
            
            for (int y = 0; y < nh; y++) {
                for (int x = 0; x < nw; x++) {
                    int rgb = scaled.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int gVal = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int gray = (r + gVal + b) / 3;
                    int idx = Math.min(7, gray / 32);
                    sb.append(CHARS[idx]);
                }
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    private static void moveCursor(int x, int y) {
        System.out.print("\033[" + (y + 1) + ";" + (x + 1) + "H");
        System.out.flush();
    }
}
