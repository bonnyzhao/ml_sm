package com.hpe.sm.restfulApi;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hpe.sm.train.BayesClassification;
import com.hpe.sm.train.ChangeImpactResult;
import com.hpe.sm.train.InputedFeatures;

@RestController
public class Controller {

    @RequestMapping("/train")
    public String train() {
    	BayesClassification.train(1);
    	BayesClassification.test();
        return "Done";
    }
    
    @RequestMapping(value="/test", 
    		method = RequestMethod.POST,
    		headers = {"Content-type=application/json"})
    @ResponseBody
    public ChangeImpactResult test(@RequestBody  InputedFeatures features) {
        return BayesClassification.estimate(features.toChange());
    }
    
    @RequestMapping(value="/suggest") 
    @ResponseBody
    public ReturnPackage suggest(String text){
    	return BayesClassification.categoryWithDescription(text);
    }
    
    @RequestMapping(value="/suggestWithCate", 
    		method = RequestMethod.POST,
    		headers = {"Content-type=application/json"})
    @ResponseBody
    public ReturnPackage suggestWithCate(@RequestBody  InputedFeatures features) {
        return BayesClassification.categoryWithCategory(features.toChange());
    }
    
}
