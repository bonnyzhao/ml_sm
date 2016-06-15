package com.hpe.sm.train;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Read {
	private static Connection dbCon = null;
	private static Statement stmt = null;
	
//	public static void main(String[] args){
//		List<Change> changes = getInfo();
//		System.out.println(changes.size());
//	}
	
	public static List<Change> getInfo(){
		List<Change> result = new ArrayList<Change>();
		if(dbCon == null) connect();
		String getChange = "SELECT [NUMBER]  "
				 + ",'Category ' + [CATEGORY] "
				 + ",'AssignGroup ' + [ASSIGN_DEPT] "
				 + ",'Coordinator ' + [COORD_DEPT]  "
				 + ",'Risk ' + [RISK_ASSESSMENT]  "
				// + ",'Priority ' + [PRIORITY_CODE]  " //always 2
				 + ",'Gl ' + [GL_NUMBER]  "
				 + ",'Logical ' + [LOGICAL_NAME] "
				 + ", 'Service ' + [HP_BIZSRVC_CI_ID]"
				// + ",'Current ' + [CURRENT_PHASE]  "
				// + ",'AMOUNT ' + CONVERT(varchar, [amount]) as 'AMOUNT' "
				 + ",[DESCRIPTION]  "
				 + "FROM Change_Service_Amount  "
				 + "where STATUS = 'closed'"
				// + "and HP_BIZSRVC_CI_ID = 'nw-lan connectivity'"
				 + "and CATEGORY <> 'KM Document'";
		String changeWithIncident = "select * from Change_Count";
//				//"select * from ( " + 
//				"select c.CHANGED_ID, COUNT([NUMBER]) [AMOUNT] "
//				+ "from dbo.Incident2 as i, dbo.ChangeJoined_Service as c "
//				+ "where datediff(day, i.OPEN_TIME, c.HP_LTST_IMPL_END_DT) <= 30 "
//				+ "and datediff(day, i.OPEN_TIME, c.HP_LTST_IMPL_END_DT) >= 0 "
//				+ "and i.LOGICAL_NAME = c.ASSETS  "
//				+ "and c.STATUS = 'closed'  "
//				//+ "and c.HP_BIZSRVC_CI_ID = i.AFFECTED_ITEM "
//				+ "and c.CATEGORY <> 'KM Document'"
//				//+ "and i.AFFECTED_ITEM = 'nw-lan connectivity'"
//				+ "group by c.CHANGED_ID"
//				//+ ") as temp "
//				//+ "where temp.[AMOUNT] > 2"
//				;
		try {
			List<String> positiveResult = new ArrayList<String>();
			ResultSet positiveChanges = stmt.executeQuery(changeWithIncident);
			Map<String, Integer> amountMap = new HashMap<String, Integer>();
			while(positiveChanges.next()){
				positiveResult.add(positiveChanges.getString(1));
				amountMap.put(positiveChanges.getString(1), positiveChanges.getInt(2));
			}
			
			ResultSet changes = stmt.executeQuery(getChange);
			while(changes.next()){
				Change change = new Change();
				change.setID(changes.getString("NUMBER"));
				change.setDescription(changes.getString("DESCRIPTION"));
				change.setAmount(
						amountMap.containsKey(change.getID()) ? amountMap.get(change.getID()) : 0);
				//change.setAmount(Integer.valueOf(changes.getString("AMOUNT").substring(7)));
				List<String> features = new ArrayList<String>();
				for(int i = 2; i < 8; ++i){
					String s = changes.getString(i);
					if(s != null){
						features.add(s);
					}
				}
				change.setFeatures(features);
				change.setResult(positiveResult.contains(change.getID())? true : false);
				result.add(change);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	private static void connect(){
		String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String dbURL="jdbc:sqlserver://localhost:1433;DatabaseName=test";
		String userName = "sa";
		String userPwd = "1Qaz2wsx3edc";
		try {
			Class.forName(driverName);
			dbCon = DriverManager.getConnection(dbURL,userName,userPwd);
			stmt=dbCon.createStatement();
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
