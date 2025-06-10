package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TransparentGoldWindow extends JFrame {
    private final JLabel priceLabel = new JLabel();  // 当前金价（纯数值）
    private final JLabel changeLabel = new JLabel(); // 涨跌幅（纯数值）
    private final JLabel alertLabel = new JLabel();  // 预警信息
    private Point startPoint; // 用于窗口拖动的坐标

    public TransparentGoldWindow() {
        // 窗口基础设置（保持不变）
        setTitle("实时金价监控");
        setSize(200, 80);
        setAlwaysOnTop(true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // 字体设置（保持不变）
        Font mainFont = new Font("微软雅黑", Font.PLAIN, 14);
        priceLabel.setFont(mainFont);
        priceLabel.setForeground(Color.BLACK);
        changeLabel.setFont(mainFont);
        alertLabel.setFont(mainFont);
        alertLabel.setForeground(Color.RED);

        // 布局设置（保持不变）
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        pricePanel.setOpaque(false);
        pricePanel.add(priceLabel);
        pricePanel.add(changeLabel);

        JPanel contentPane = new JPanel(new GridLayout(2, 1, 0, 5));
        contentPane.setOpaque(false);
        contentPane.add(pricePanel);
        contentPane.add(alertLabel);
        setContentPane(contentPane);

        // 窗口位置（保持不变）
        setWindowToTopRight();

        // 拖动逻辑（保持不变）
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point current = e.getLocationOnScreen();
                setLocation(current.x - startPoint.x, current.y - startPoint.y);
            }
        });
    }

    // 新增/修改：窗口定位到右上角
    private void setWindowToTopRight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int offsetX = 10;  // 右侧偏移量（避免贴边）
        int offsetY = 10;  // 顶部偏移量（避免贴边）

        // 计算右上角坐标：屏幕宽度 - 窗口宽度 - 右侧偏移量
        int x = screenSize.width - this.getWidth() - offsetX;
        int y = offsetY;

        setLocation(x, y);
    }

    // 重点：更新界面数据（颜色逻辑调整）
    public void updateUI(String currentPrice, float change, String alert) {
        SwingUtilities.invokeLater(() -> {


            String changeText = String.format("%.2f", change);
            changeText = change > 0 ? "↑ " + changeText : "↓ " + changeText;

            // 显示当前金价（纯数值）
            priceLabel.setText(currentPrice);

            // 显示涨跌幅并设置颜色
            changeLabel.setText(changeText);
            if (change > 0) {
                // 上升：红色（标准色）
                changeLabel.setForeground(Color.RED);
            } else if (change < 0) {
                // 下降：绿色（更清晰的绿色）
                changeLabel.setForeground(new Color(0, 153, 0));  // RGB(0,153,0) 深绿色
            } else {
                // 平：黑色
                changeLabel.setForeground(Color.BLUE);
            }

            // 预警信息（保持红色）
            alertLabel.setText(alert);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TransparentGoldWindow window = new TransparentGoldWindow();
            window.setVisible(true);
        });
    }
}