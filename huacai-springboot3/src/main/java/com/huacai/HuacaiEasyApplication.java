package com.huacai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class HuacaiEasyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuacaiEasyApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  青紫管理系统启动成功   ლ(´ڡ`ლ)ﾞ " +
                ".__                                 .__\n" +
                "|  |__  __ _______      ____ _____  |__|\n" +
                "|  |  \\|  |  \\__  \\   _/ ___\\\\__  \\ |  |\n" +
                "|   Y  \\  |  // __ \\_ \\  \\___ / __ \\|  |\n" +
                "|___|  /____/(____  /  \\___  >____  /__|\n" +
                "     \\/           \\/       \\/     \\/"
        );
    }

}
