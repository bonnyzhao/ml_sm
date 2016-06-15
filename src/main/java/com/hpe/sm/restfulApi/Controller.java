package com.hpe.sm.restfulApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hpe.sm.train.BayesClassification;
import com.hpe.sm.train.Change;
import com.hpe.sm.train.ChangeImpactResult;
import com.hpe.sm.train.InputedFeatures;

@RestController
public class Controller {

	private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/train")
    public String train() {
    	BayesClassification.train();
    	BayesClassification.test();
        return "Done";
    }
    
    @RequestMapping(value="/test", 
    		method = RequestMethod.POST,
    		headers = {"Content-type=application/json"})
    @ResponseBody
    public ChangeImpactResult test(@RequestBody  InputedFeatures features) {
    	
//    	if (bindingResult.hasErrors()) {
//    		result.setHttpCode(40001);
//    		result.setHttpMsg(bindingResult.getFieldError().getDefaultMessage());
//    		return result;
//    	}
    	System.out.println("features: " + features.toString());
//    	
    	Change change = new Change();
    	change.setID(features.getId());
    	change.setDescription(features.getDescription());
    	
    	List<String> featureList = new ArrayList<String>();
    	
    	if(!features.getCategory().equals("")) featureList.add("Category " + features.getCategory());
		if(!features.getAssignGroup().equals("")) featureList.add("AssignGroup " + features.getAssignGroup());
    	if(!features.getCoordinator().equals("")) featureList.add("Coordinator " + features.getCoordinator());
    	if(!features.getRisk().equals("")) featureList.add("Risk " + features.getRisk());
    	if(!features.getPriority().equals("")) featureList.add("Priority " +features.getPriority());
    	if(!features.getGl().equals("")) featureList.add("Gl " + features.getGl());
    	if(!features.getLogicalName().equals("")) featureList.add("Logical " + features.getLogicalName());
    	if(!features.getService().equals("")) featureList.add("Service " + features.getService());
    	
    	change.setFeatures(featureList);
    	
    	//BayesClassification.test(change);
        return BayesClassification.test(change);
    }
}
