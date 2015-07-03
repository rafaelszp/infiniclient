package com.example.infiniclient.domain;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Keys implements Serializable {

	
	private static final long serialVersionUID = 1L;
	@XmlElement(name="key")
	public List<String> keys;
	
}
