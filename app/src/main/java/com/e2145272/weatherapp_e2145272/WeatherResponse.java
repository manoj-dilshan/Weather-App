package com.e2145272.weatherapp_e2145272;

import java.util.List;

public class WeatherResponse {
    public List<Weather> weather;
    public Main main;
    public int visibility;
    public Wind wind;
    public Rain rain;


    public class Weather {
        public String main;
        public String description;
    }

    public class Main {
        public double temp;
        public double feels_like;
        public double temp_min;
        public double temp_max;
        public int pressure;
        public int humidity;
    }

    public class Wind {
        public double speed;
    }

    public class Rain {
        public double _1h;
    }
}
