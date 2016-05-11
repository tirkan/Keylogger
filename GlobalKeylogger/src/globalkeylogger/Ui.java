package globalkeylogger;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class Ui{
    
    private JFrame frame;
    private GlobalKeyboard gk;
    
    public Ui(GlobalKeyboard gk){
        this.gk = gk;
        
        frame = new JFrame();
        
        frame.setPreferredSize(new Dimension(240, 120));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        createComponents(frame.getContentPane());

        frame.pack();
        frame.setVisible(true);
        
        addSysTrayIcon();
    }
    
    private void createComponents(Container container){
        container.setLayout(new BorderLayout());
        
        JLabel statusLabel = new JLabel("Listening: " + gk.isRunning());
        container.add(statusLabel, BorderLayout.NORTH);
        
        JLabel runningTimeLabel = new JLabel("Time listened: ");
        container.add(runningTimeLabel, BorderLayout.CENTER);
        
        JButton startButton = new JButton("Start listening");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(gk.isRunning()){
                    try {
                        startButton.setText("Start listening");
                        gk.stop();
                    } catch (IOException ex) {
                    }
                } else {
                    startButton.setText("Stop listening and save log");
                    gk.start();  
                    updateTimeListened(runningTimeLabel);
                }
                statusLabel.setText("Listening: " + gk.isRunning()); 
            }
        });
        container.add(startButton, BorderLayout.SOUTH);
        
        frame.addWindowStateListener(windowStateListener());
    }
    
    private void updateTimeListened(JLabel runningTimeLabel){
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(gk.isRunning())
                    runningTimeLabel.setText("Time listened: " + gk.timeListened());
            }
        });
        timer.start();
    }
    
    private WindowStateListener windowStateListener(){
        WindowStateListener wsl = new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if(e.getNewState() == 1){
                    frame.setVisible(false);
                } 
            }
        };
                
        return wsl;    
    }

    private void addSysTrayIcon(){
        if(SystemTray.isSupported()){
            final PopupMenu popup = new PopupMenu();
            
            SystemTray systemTray = SystemTray.getSystemTray();
            TrayIcon trayIcon = new TrayIcon(new ImageIcon(
                    "assets/trayIcon.png", "omt").getImage(), "Keylogger");
            trayIcon.setImageAutoSize(true);

            MenuItem showItem = new MenuItem("Show");
            MenuItem exitItem = new MenuItem("Exit");

            popup.add(showItem);
            popup.addSeparator();
            popup.add(exitItem);
       
            showItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(true);  
                }
            });
            
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gk.isRunning()) {
                        int confirm = JOptionPane.showOptionDialog(frame,
                                "Listening in progress - exit without saving?",
                                "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, null, null);

                        if (confirm == JOptionPane.YES_OPTION) 
                            System.exit(0);
                    } else 
                        System.exit(0);
                }
            });

            trayIcon.setPopupMenu(popup);

            try {
                systemTray.add(trayIcon);
            } catch (Exception e) {
            }
        }
    }
}