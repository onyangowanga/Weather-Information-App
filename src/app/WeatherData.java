package app;

public class WeatherData {
    private double tempC;
    private int humidity;
    private double windMs;
    private String condition;
    private String iconCode;
    private String dateTime;

    public WeatherData(double tempC, int humidity, double windMs, String condition, String iconCode, String dateTime) {
        this.tempC = tempC;
        this.humidity = humidity;
        this.windMs = windMs;
        this.condition = condition;
        this.iconCode = iconCode;
        this.dateTime = dateTime;
    }

    public double getTempC() { return tempC; }
    public int getHumidity() { return humidity; }
    public double getWindMs() { return windMs; }
    public String getCondition() { return condition; }
    public String getIconCode() { return iconCode; }
    public String getDateTime() { return dateTime; }
}