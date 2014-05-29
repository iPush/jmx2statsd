package com.baidu.aqueducts.dashboard;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;


public class AttributeValueHolder {
	private ObjectName mbean;
	private MBeanAttributeInfo attribute;
	
	public AttributeValueHolder(ObjectName mbean, MBeanAttributeInfo attribute){
		this.mbean = mbean;
		this.attribute = attribute;
	}
	
	public MBeanAttributeInfo getAttribute() {
		return attribute;
	}
	
	public ObjectName getMbean() {
		return mbean;
	}
	
}
