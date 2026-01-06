package app;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeatherAppSwing extends JFrame {
    private JTextField cityField = new JTextField("Nairobi", 10);
    private JButton fetchBtn = new JButton("Search");
    private JButton toggleBtn = new JButton("Switch to °F");
    private JComboBox<String> timeSelector = new JComboBox<>();
    private JTextArea historyArea = new JTextArea();
    
    private JLabel tempLabel = new JLabel("", JLabel.CENTER);
    private JLabel iconLabel = new JLabel("", JLabel.CENTER);
    private JLabel detailsLabel = new JLabel("", JLabel.CENTER);
    private JPanel forecastContainer = new JPanel(new GridLayout(1, 4, 15, 0));
    
    private Image bgImage;
    private WeatherService service = new WeatherService();
    private List<WeatherData> currentForecastList;
    private boolean isCelsius = true;
    private double currentTemp;

    public WeatherAppSwing() {
        setTitle("Weather Information Dashboard");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main Background Panel
        JPanel mainWrapper = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainWrapper.setBorder(new EmptyBorder(25, 25, 25, 25));
        setContentPane(mainWrapper);

        // TOP: Controls
        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.add(new JLabel("City:")); topBar.add(cityField);
        topBar.add(new JLabel("View Time:")); topBar.add(timeSelector);
        topBar.add(fetchBtn);
        topBar.add(toggleBtn);
        add(topBar, BorderLayout.NORTH);

        // CENTER: Primary Display (Temp, Icon, Details)
        JPanel centerDisplay = new JPanel(new GridBagLayout());
        centerDisplay.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;

        tempLabel.setFont(new Font("SansSerif", Font.BOLD, 90));
        tempLabel.setForeground(Color.BLACK);
        
        detailsLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        detailsLabel.setForeground(Color.BLACK);

        centerDisplay.add(tempLabel, gbc);
        centerDisplay.add(iconLabel, gbc);
        centerDisplay.add(detailsLabel, gbc);
        add(centerDisplay, BorderLayout.CENTER);

        // SOUTH: Short-term Forecast Cards
        forecastContainer.setOpaque(false);
        forecastContainer.setPreferredSize(new Dimension(0, 150));
        add(forecastContainer, BorderLayout.SOUTH);

        // EAST: History with Timestamp
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setPreferredSize(new Dimension(230, 0));
        scroll.setBorder(new TitledBorder("Search History"));
        add(scroll, BorderLayout.EAST);

        // Listeners
        fetchBtn.addActionListener(e -> performSearch());
        toggleBtn.addActionListener(e -> toggleUnits());
        timeSelector.addActionListener(e -> updateFromDropdown());

        setVisible(true);
    }

    private void performSearch() {
        try {
            String city = cityField.getText().trim();
            if(city.isEmpty()) return;

            String query = "q=" + city;
            currentForecastList = service.getForecast(query);
            
            // Populate Dropdown for manual time selection
            timeSelector.removeAllItems();
            for (WeatherData d : currentForecastList) {
                timeSelector.addItem(d.getDateTime());
            }

            // Update Main View and Forecast Cards
            updateMainDisplay(currentForecastList.get(0));
            renderForecastCards(currentForecastList);
            
            // Log History with Timestamp
            String logTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            historyArea.append(city + " @ " + logTime + "\n");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Location not found.");
        }
    }

    private void updateMainDisplay(WeatherData data) {
        currentTemp = data.getTempC();
        refreshTempLabel();
        
        // Detailed text line
        detailsLabel.setText(String.format("Humidity: %d%% | Wind: %.1f m/s | %s", 
            data.getHumidity(), data.getWindMs(), data.getCondition().toUpperCase()));

        // Large high-quality icon
        try {
            String iconUrl = "https://openweathermap.org/img/wn/" + data.getIconCode() + "@4x.png";
            iconLabel.setIcon(new ImageIcon(new java.net.URL(iconUrl)));
        } catch (Exception e) { e.printStackTrace(); }

        // Update background based on the weather data's time
        updateBackgroundByTime(data.getDateTime());
    }

    private void renderForecastCards(List<WeatherData> forecast) {
        forecastContainer.removeAll();
        // Show next 4 intervals (~12 hours)
        for (int i = 0; i < 8 && i < forecast.size(); i++) {
            WeatherData f = forecast.get(i);
            JPanel card = new JPanel(new GridLayout(3, 1));
            card.setBackground(new Color(255, 200, 200, 200)); // Glass effect
            card.setForeground(Color.BLACK);
            card.setBorder(new LineBorder(Color.GRAY, 1));
            
            JLabel timeL = new JLabel(f.getDateTime().substring(11, 16), JLabel.CENTER);
            JLabel tempL = new JLabel(String.format("%.1f°C", f.getTempC()), JLabel.CENTER);
            JLabel condL = new JLabel(f.getCondition(), JLabel.CENTER);
            
            card.add(timeL); card.add(tempL); card.add(condL);
            forecastContainer.add(card);
        }
        forecastContainer.revalidate();
        forecastContainer.repaint();
    }

    private void updateBackgroundByTime(String dateTimeStr) {
        int hour;
        if (dateTimeStr.equals("Current")) {
            hour = LocalDateTime.now().getHour();
        } else {
            // Parse hour from "2026-01-06 15:00:00"
            hour = Integer.parseInt(dateTimeStr.substring(11, 13));
        }

        String imgName;
        if (hour >= 5 && hour < 11) imgName = "morning.jpg";
        else if (hour >= 11 && hour < 16) imgName = "noon.jpg";
        else if (hour >= 16 && hour < 20) imgName = "evening.jpg";
        else imgName = "night.jpg";

        java.net.URL imgURL = getClass().getResource("/resources/backgrounds/" + imgName);
        if (imgURL != null) bgImage = new ImageIcon(imgURL).getImage();
        repaint();
    }

    private void refreshTempLabel() {
        if (isCelsius) {
            tempLabel.setText(String.format("%.1f°C", currentTemp));
        } else {
            tempLabel.setText(String.format("%.1f°F", (currentTemp * 9/5) + 32));
        }
    }

    private void toggleUnits() {
        isCelsius = !isCelsius;
        toggleBtn.setText(isCelsius ? "Switch to °F" : "Switch to °C");
        refreshTempLabel();
    }

    private void updateFromDropdown() {
        int idx = timeSelector.getSelectedIndex();
        if (idx >= 0 && currentForecastList != null) {
            updateMainDisplay(currentForecastList.get(idx));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherAppSwing::new);
    }
}