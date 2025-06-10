package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class SystemTrayManager {
    private final UI.TransparentGoldWindow window;
    private TrayIcon trayIcon;

    public SystemTrayManager(UI.TransparentGoldWindow window) {
        this.window = window;
        initTray();
    }

    private void initTray() {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null, "当前系统不支持托盘功能");
            return;
        }

        // 托盘图标（可替换为自定义ico）
        Image image = Toolkit.getDefaultToolkit().getImage("tray_icon.png");
        trayIcon = new TrayIcon(image, "金价监控");
        trayIcon.setImageAutoSize(true);

        // 左键：显示/隐藏窗口
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    window.setVisible(!window.isVisible());
                }
            }
        });

        // 右键菜单
        PopupMenu popupMenu = new PopupMenu();
        MenuItem settingItem = new MenuItem("打开监控配置");
        settingItem.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File("monitor.json"));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "打开配置文件失败: " + ex.getMessage());
            }
        });

        MenuItem exitItem = new MenuItem("关闭");
        exitItem.addActionListener(e -> System.exit(0));

        popupMenu.add(settingItem);
        popupMenu.add(exitItem);
        trayIcon.setPopupMenu(popupMenu);

        // 添加到系统托盘
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(null, "托盘初始化失败: " + e.getMessage());
        }
    }
}