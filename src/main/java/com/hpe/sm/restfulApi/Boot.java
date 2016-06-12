package com.hpe.sm.restfulApi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

//import com.hpe.sm.TomcatLocation;

@SpringBootApplication
public class Boot {

    public static void main(String[] args) {
    	//TomcatLocation tl = new TomcatLocation();
    	//tl.test();
    	//SpringApplication.run(Boot.class, args);
    	new SpringApplicationBuilder(Boot.class).run(args);
    }
}