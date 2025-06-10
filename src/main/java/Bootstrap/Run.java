package Bootstrap;

import Context.Context;
import FileWatcher.ConfigWatcher;
import FileWatcher.FileWatcher;
import Monitor.IGoldMonitor;
import Utils.Config;
import Utils.Util;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Run implements Runnable {
    private final Context context = new Context();
    private volatile Map<String, IGoldMonitor> monitorMap = new HashMap<>(1);
    private final UI.TransparentGoldWindow window = new UI.TransparentGoldWindow();
    private final UI.SystemTrayManager trayManager = new UI.SystemTrayManager(window);
    private final FileWatcher fileWatcher = new ConfigWatcher(this);

    public void setMonitorMap(Map<String, IGoldMonitor> monitorMap) {
        this.monitorMap = monitorMap;
    }
    private Run() {
        final Path path = Paths.get(Config.MonitorConfig);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println("创建监控配置文件：" + Config.MonitorConfig + "失败，请使用管理员权限运行！");
                System.exit(-1);
            }
        }

        fileWatcher.setFile(path.toFile());
        fileWatcher.start();
        fileWatcher.doOnChange();

        window.setVisible(true); // 初始显示窗口
    }

    public void run() {
        try {
            if (!updatePrice()) return;

            // 获取最新价格数据
            JSONArray priceArray = context.get(Context.ContextType.PriceArray, new JSONArray());
            JSONObject latestPriceJson = priceArray.getJSONObject(0);
            float currentPrice = Util.getPrice(latestPriceJson);
            String updateTime = Config.DateFormat.format(new Date(Util.getTime(latestPriceJson)));

            // 计算涨跌值
            float prevPrice = Util.getPrice(priceArray.getJSONObject(1));
            float change = currentPrice - prevPrice;

            // 检查监控规则
            StringBuilder alertText = new StringBuilder();
            for (IGoldMonitor monitor : monitorMap.values()) {
                String alert = monitor.monitor(context);
                if (alert != null) alertText.append(alert).append(" ");
            }

            // 更新界面
            window.updateUI(
                    String.format("%.2f 元", currentPrice),
                    change,
                    alertText.toString()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新价格数组，数组内的元素形如：
     * {"name":"2019-08-29 00:24:00","value":["2019-08-29 00:24:00","356.73"]}
     */
    private boolean updatePrice() {
        final JSONArray jsonPriceArray = context.get(Context.ContextType.PriceArray, new JSONArray());
        if (jsonPriceArray.isEmpty()) {
            System.out.println("当天价格为空，今天是星期天？");
            //初始化获取当天的价格
            final JSONArray priceToday = Util.getTodayPrice();
            context.add(Context.ContextType.PriceArray, priceToday);
            return false;
        }

        final JSONObject jsonPriceNewCache = jsonPriceArray.getJSONObject(0);
        final JSONObject jsonPriceNew = Util.getNewestPrice();

        final long timeNewInCache = Util.getTime(jsonPriceNewCache);
        final long timeNew = jsonPriceNew.getLong("time");

        if (timeNew <= timeNewInCache) {
            return false;
        }

        final JSONObject priceAdd = new JSONObject();
        final String time = Config.DateFormat.format(new Date(timeNew));

        final JSONArray jsonValueArray = new JSONArray();
        jsonValueArray.add(time);
        jsonValueArray.add(jsonPriceNew.getFloat("price"));

        priceAdd.put("name", time);
        priceAdd.put("value", jsonValueArray);

        jsonPriceArray.add(0, priceAdd);

        if (jsonPriceArray.size() > Config.PriceArrayMaxCacheSize) {
            jsonPriceArray.remove(jsonPriceArray.size() - 1);
        }

        return true;
    }

    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Run run = new Run();
        executor.scheduleAtFixedRate(run, 0, Config.ThreadTickSecond, TimeUnit.SECONDS);
    }
}