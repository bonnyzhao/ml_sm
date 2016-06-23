package com.hpe.sm.sla;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import scala.collection.Seq;

public class JDBCConnection {
	public static DataFrame jdbcDF(SQLContext sc, String host, String port, String username, String password,  String instance, String tableName, String condition){
		HashMap <String, String> oracle_options = new HashMap<String, String>();
				oracle_options.put("driver", "oracle.jdbc.OracleDriver");
				oracle_options.put("url", "jdbc:oracle:thin:"+username+"/"+password+"@//"+host + ":" + port+"/"+instance);				
				oracle_options.put("dbtable", tableName);
				return sc.read().format("jdbc").options(oracle_options).load();
				//return sc.read().format("jdbc").options(oracle_options).load().where(condition);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SparkConf conf = new SparkConf().setAppName("JDBC Example").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		String[] columns = new String[]{"t1.INCIDENT_ID", "TITLE", "DESCRIPTION", "ASSIGNEE", "INCIDENT_ID", "SLA_BREACH", "AFFECTED_ITEM", "LOGICAL_NAME", "SUBCATEGORY","PRODUCT_TYPE","DEPT","HP_ISSUE_TYPE_ID","HP_PROD_SPEC_ID"};
		//String[] affected_items = new String[]{"remote access to hp-client support", "mcafee endpoint encryption", "lync-instant messaging", "nw-dns support" };
		String[] affected_items = new String[]{ "mcafee endpoint encryption", "lync-instant messaging", "nw-dns support" };
		for (String item : affected_items){
			String condition = "OPEN = 'Closed' and PROBLEM_TYPE = 'incident' and FOLDER is NULL and AFFECTED_ITEM = '" + item + "'";
			//String condition = "tb.OPEN = 'Closed' and tb.PROBLEM_TYPE = 'incident' and tb.FOLDER is NULL and tb.AFFECTED_ITEM = 'e-mail exchange infrastructure' ";
			//System.out.println("select * from incidentsm1 t1 INNER JOIN incidentsa1 t2 on t1.INCIDENT_ID = t2.INCIDENT_ID  where " + condition + " as tb");
			String dbtable = "(select t1.*, t2.ASSIGNMENT from incidentsm1 t1 INNER JOIN incidentsa1 t2 on t1.INCIDENT_ID = t2.INCIDENT_ID  where " + condition + ")";  
			
					DataFrame df = jdbcDF(sqlContext, "16.186.72.95","1521", "ITSMP941", "1Qaz2wsx3edc", "smdb.chn.hp.com",  dbtable, "true");
					df = df.select("ASSIGNMENT", "INCIDENT_ID", "SLA_BREACH", "AFFECTED_ITEM", "LOGICAL_NAME", "SUBCATEGORY","PRODUCT_TYPE","DEPT","HP_ISSUE_TYPE_ID","HP_PROD_SPEC_ID", "TITLE", "DESCRIPTION");
					df.printSchema();
					df.distinct().write().format("parquet").save("ml_data/"+item+".parquet");
			
		}
	}

}
