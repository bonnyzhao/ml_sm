package com.hpe.sm;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class TomcatLocation {
	public void test(){
		Class cls = this.getClass();
		ProtectionDomain pDomain = cls.getProtectionDomain();
		CodeSource cSource = pDomain.getCodeSource();
		URL loc = cSource.getLocation(); 
		System.out.println(loc);
	}
}
