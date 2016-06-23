package com.hpe.sm.restfulApi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.hpe.sm.DocumentCategory.DescriptionCategory;
import com.hpe.sm.train.BayesClassification;

//import com.hpe.sm.TomcatLocation;

@SpringBootApplication
public class Boot {

    public static void main(String[] args) {
    	//TomcatLocation tl = new TomcatLocation();
    	//tl.test();
    	//SpringApplication.run(Boot.class, args);
    	BayesClassification.train(1);
    	BayesClassification.test();
    	new SpringApplicationBuilder(Boot.class).run(args);
    }
}