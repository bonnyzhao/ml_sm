package com.hpe.sm.restfulApi;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hpe.sm.train.BayesClassification;

@RestController
public class Controller {

	private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/train")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
    	BayesClassification.train();
    	BayesClassification.test();
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
}
