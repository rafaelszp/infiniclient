package com.example.infiniclient.domain;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

@XmlRootElement
@ProtoDoc("@Indexed")
public class Pessoa implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	
	private UUID id;
	private String nome;
	private String rg;
	
	
	public Pessoa(){
	}
	
	public Pessoa(UUID id, String nome, String rg) {
		super();
		this.id = id;
		this.nome = nome;
		this.rg = rg;
	}
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	
	@ProtoDoc("@IndexedField")
	@ProtoField(number=1,required=true)
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@ProtoDoc("@IndexedField")
	@ProtoField(number=2)
	public String getRg() {
		return rg;
	}
	public void setRg(String rg) {
		this.rg = rg;
	}

}
