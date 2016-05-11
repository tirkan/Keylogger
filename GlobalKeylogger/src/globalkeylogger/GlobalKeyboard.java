package globalkeylogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class GlobalKeyboard {
    private boolean isRunning = false;
    private GlobalKeyboardHook keyboardHook;
    private final Map<String, List<LocalDateTime>> pressedKeys;
    private FileWriter writer;
    private String filename;
    private LocalDateTime localDateTimeAtStart;
    private final DateTimeFormatter dateTimeFormatter;
    private final DateTimeFormatter filenameFormatter;
     
    public GlobalKeyboard() {   
        pressedKeys = new HashMap<>();
        dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy H:mm:ss");
        filenameFormatter = DateTimeFormatter.ofPattern("d.M.yyyy H-mm-ss");
    }
    
    public void start() {
        localDateTimeAtStart = LocalDateTime.now();
        
        keyboardHook = new GlobalKeyboardHook();
        isRunning = true;

        keyboardHook.addKeyListener(new GlobalKeyAdapter() {
            @Override
            public void keyPressed(GlobalKeyEvent event) {
            }
            
            @Override
            public void keyReleased(GlobalKeyEvent event) {
                addKeyEvent(event);
            }
        });
    }
    
    public void stop() throws IOException{
        filename = filenameFormatter.format(LocalDateTime.now());
        keyboardHook.shutdownHook();
        isRunning = false;   
        writeStatistics();
    }
    
    public void addKeyEvent(GlobalKeyEvent event){
        String key = String.valueOf(event.getKeyChar());

        if (pressedKeys.containsKey(key)) {
            pressedKeys.get(key).add(LocalDateTime.now());
            return;
        } else if (key.trim().isEmpty()){
            switch (event.getVirtualKeyCode()) {
                case 13: key = "Enter"; break;
                case 32: key = "Space"; break;
                case 8: key = "Backspace"; break;
                case 162: key = "L Control"; break;
                case 163: key = "R Control"; break;
                case 164: key = "L Alt"; break;
                case 9: key = "Tab"; break;
                case 160: key = "L Shift"; break;
                case 161: key = "R Shift"; break;
                case 27: key = "R Shift"; break;
                case 20: key = "Caps Lock"; break;
                case 91: key = "Win Key"; break;
                case 38: key = "UP"; break;
                case 40: key = "DOWN"; break;
                case 37: key = "LEFT"; break;
                case 39: key = "RIGHT"; break;
                case 45: key = "Insert"; break;
                case 36: key = "Home"; break;
                case 33: key = "Page Up"; break;
                case 34: key = "Page Down"; break;
                case 35: key = "End"; break;
                case 46: key = "Delete"; break;
            }
            if (pressedKeys.containsKey(key)) {
                pressedKeys.get(key).add(LocalDateTime.now());
                return;
            }
        }
        
        List<LocalDateTime> localDateTimes = new ArrayList<>();
        localDateTimes.add(LocalDateTime.now());
        pressedKeys.put(key, localDateTimes);
    }
    
    public void writeStatistics() throws IOException{
        String dateTimeAtStart = dateTimeFormatter.format(localDateTimeAtStart);
        String dateTimeAtEnd = dateTimeFormatter.format(LocalDateTime.now());
        Duration duration = Duration.between(localDateTimeAtStart, LocalDateTime.now());
        
        File file = new File("logs/" + filename + ".txt");
        file.getParentFile().mkdir();

        writer = new FileWriter(file);
        writer.write(dateTimeAtStart + " - " + dateTimeAtEnd +  "\r\n"
                + formatDuration(duration) + "\r\n\r\n");

        for (Map.Entry<String, List<LocalDateTime>> entrySet : sortMapByMostKeyPresses(pressedKeys).entrySet()) {
            String key = entrySet.getKey();
            Integer value = entrySet.getValue().size();
            writer.write(key + ": " + value + "\r\n");
        }
        
        writer.close();
    }
    
    private String formatDuration(Duration duration){
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
            "%d:%02d:%02d",
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        ); 
        return seconds < 0 ? "-" + positive : positive;
    }
    
    private static TreeMap<String, List<LocalDateTime>> 
        sortMapByMostKeyPresses(Map<String, List<LocalDateTime>> map){
        
        Comparator<String> comparator = new ValueComparator(map);
        TreeMap<String, List<LocalDateTime>> result = new TreeMap<>(comparator);
        result.putAll(map);
        return result;
    }
    
    public boolean isRunning(){
        return isRunning;
    }
    
    public String timeListened(){
        Duration timeListened = Duration.between(localDateTimeAtStart, 
                LocalDateTime.now());
        return formatDuration(timeListened);
    }
}
