package com.example.infiniclient.rest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.example.infiniclient.domain.Keys;
import com.example.infiniclient.domain.Pessoa;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheRestTest {
	
	
	private static final String BASE_URL = "http://localhost:8080/rest";
	private static final String CACHE_URL = BASE_URL+"/pessoa_rest";
	private static final String USER = "kermit";
	private static final String PWD = "the-frog-1";
	private static final Authenticator AUTHENTICATOR = new Authenticator(USER, PWD);
	
	
	
	static List<Pessoa> pessoas;
	static Client client;
	
	@BeforeClass
	public static void setupClass(){
		pessoas = new ArrayList<Pessoa>();
		
		pessoas.add(new Pessoa(UUID.randomUUID(), "Rafael", "10101010"));
		pessoas.add(new Pessoa(UUID.randomUUID(), "Keyla", "21212121"));
		pessoas.add(new Pessoa(UUID.randomUUID(), "Maria", "34343434"));
		pessoas.add(new Pessoa(UUID.randomUUID(), "Rique", "56565656"));
		pessoas.add(new Pessoa(UUID.randomUUID(), "Joaquim", "78787878"));
		
	}
	
	@AfterClass
	public static void tearDownClass(){
		
	}
	
	@Before
	public void setup(){
		client = ClientBuilder.newClient().register(AUTHENTICATOR); 
		
	}
	
	@After
	public void tearDown(){
		client.close();
	}
	
	@Test
	public void t01_shouldPutCacheKey(){
		
		Pessoa pessoa = pessoas.get(0);
		
		String getUrl = CACHE_URL+"/"+pessoa.getId();
		WebTarget target = client.target(getUrl);
		
		Response response = target.request()
				.buildPost(Entity.entity(pessoa, MediaType.APPLICATION_XML)).invoke();
		
	
		assertEquals(200, response.getStatus());
				
	}
	
	@Test
	public void t02_shouldGetAllKeys(){
		
		WebTarget target = client.target(CACHE_URL);
		Response response = target.request()
				.accept(MediaType.APPLICATION_XML)
				.buildGet().invoke();
		
		
		Keys keys = response.readEntity(Keys.class);
		
		assertTrue("Keys.size > 0 ", keys.keys.size()>0);
		
	}
	
	@Test
	public void t03_shouldGetPessoaByKey(){
		
		Pessoa pessoa = pessoas.get(0);
		
		String getUrl = CACHE_URL+"/"+pessoa.getId();
		WebTarget target = client.target(getUrl);
		
		Response response = target.request()
				.accept(MediaType.APPLICATION_XML)
				.buildGet().invoke();
		
		Pessoa pResult = response.readEntity(Pessoa.class);
		
		assertNotNull(pResult);
		
		System.out.println(pResult.getId());
		System.out.println(pResult.getNome());
		System.out.println(pResult.getRg());
		
		
		
	}
	
	
	

}
