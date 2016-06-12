package com.hpe.sm.svm;

import java.util.List;

import com.hpe.sm.util.Matrix;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SVMTrain {
	private static svm_model model = new svm_model();
	private static svm_problem prob = new svm_problem();
	
	public static svm_model train(List<Boolean> correctResult, 
			List<Float> bayesPercentage, List<Float> maxEntPercentage){
		int recordCount = correctResult.size();
		int featureCount = 2;
		
		prob.y = new double[recordCount];
        prob.l = recordCount;
        prob.x = new svm_node[recordCount][featureCount];
        
        for (int i = 0; i < recordCount; i++){            
            double[] features = {bayesPercentage.get(i), maxEntPercentage.get(i)};
            prob.x[i] = new svm_node[features.length];
            for (int j = 0; j < features.length; j++){
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[i][j] = node;
            }           
            prob.y[i] = correctResult.get(i)? 1 : -1;
        }               

        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 100;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;       
        param.cache_size = 20000;
        param.eps = 0.01;      

        svm_model model = svm.svm_train(prob, param);
        double SVS[][] = new double[model.SV.length][2];
        for(int i = 0; i < model.SV.length; ++ i){
        	SVS[i][0] = model.SV[i][0].value;
        	SVS[i][1] = model.SV[i][1].value;
        }
        double coef[] = new double[model.sv_coef[0].length];
        for(int i = 0; i < model.sv_coef[0].length; ++i){
        	coef[i] = model.sv_coef[0][i];
        }
        double weight[] = Matrix.multiply(coef, SVS);
        
        double b = -1 * model.rho[0];
        
        System.out.println(weight[0] + " * bayesPercentage + " + weight[1] + " * maxEntPercentage + " + b);
     
        return model;
		
	}

	public static svm_model train(List<Boolean> correctResult,
			List<Float> bayesPercentageList, List<Float> maxEntPercentageList,
			List<Float> featuredClassification) {
		int recordCount = correctResult.size();
		int featureCount = 3;
		
		prob.y = new double[recordCount];
        prob.l = recordCount;
        prob.x = new svm_node[recordCount][featureCount];
        
        for (int i = 0; i < recordCount; i++){            
            double[] features = {bayesPercentageList.get(i), 
            		maxEntPercentageList.get(i), featuredClassification.get(i)};
            prob.x[i] = new svm_node[features.length];
            for (int j = 0; j < features.length; j++){
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[i][j] = node;
            }           
            prob.y[i] = correctResult.get(i)? 1 : -1;
        }               

        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 100;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;       
        param.cache_size = 20000;
        param.eps = 0.05;      

        svm_model model = svm.svm_train(prob, param);
        double SVS[][] = new double[model.SV.length][3];
        for(int i = 0; i < model.SV.length; ++ i){
        	SVS[i][0] = model.SV[i][0].value;
        	SVS[i][1] = model.SV[i][1].value;
        	SVS[i][1] = model.SV[i][2].value;
        }
        double coef[] = new double[model.sv_coef[0].length];
        for(int i = 0; i < model.sv_coef[0].length; ++i){
        	coef[i] = model.sv_coef[0][i];
        }
        double weight[] = Matrix.multiply(coef, SVS);
        
        double b = -1 * model.rho[0];
        
        System.out.println(weight[0] + " * bayesPercentage + " 
        		+ weight[1] + " * maxEntPercentage + "
        		+ weight[2] + " * featuredPercentage + "+ b);
     
        return model;
	}
}
