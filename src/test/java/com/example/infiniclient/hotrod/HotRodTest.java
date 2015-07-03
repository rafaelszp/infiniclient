package com.example.infiniclient.hotrod;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.logging.Log;
import org.infinispan.commons.logging.LogFactory;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.protostream.annotations.ProtoSchemaBuilderException;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.example.infiniclient.domain.Pessoa;

@SuppressWarnings("all")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HotRodTest {

	private static final String CACHE_NAME = "pessoa";
	private static final int CACHE_PORT = 11222;
	private static final String CACHE_HOST = "localhost";
	private RemoteCache<String, Object> pessoaCache;
	private RemoteCacheManager cacheManager;
	private static final Log logger = LogFactory.getLog(HotRodTest.class);

	@Before
	public void setup() throws ProtoSchemaBuilderException, IOException {

		final ConfigurationBuilder builder = new ConfigurationBuilder();
		builder
			.forceReturnValues(false)
			.addServer()
			.host(CACHE_HOST)
			.port(CACHE_PORT)
			.marshaller(new ProtoStreamMarshaller());
		/*
		 * 
		 * 
	cb.asyncExecutorFactory().addExecutorProperty("infinispan.client.hotrod.default_executor_factory.pool_size", "1000");
	cb.asyncExecutorFactory().addExecutorProperty("infinispan.client.hotrod.default_executor_factory.queue_size", "1000000");
- See more at: https://developer.jboss.org/thread/252130#sthash.2PaagP7w.dpuf
		 * */
		builder.asyncExecutorFactory().addExecutorProperty("infinispan.client.hotrod.default_executor_factory.pool_size", "1000");
		builder.asyncExecutorFactory().addExecutorProperty("infinispan.client.hotrod.default_executor_factory.queue_size", "1000000");
		
		cacheManager = new RemoteCacheManager(builder.build());
		pessoaCache = cacheManager.getCache(CACHE_NAME,false);

		SerializationContext ctx = ProtoStreamMarshaller
				.getSerializationContext(cacheManager);
		ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
		String pessoaSchema = protoSchemaBuilder.fileName("pessoa.proto")
				.packageName("com.example.infiniclient.domain")
				.addClass(Pessoa.class).build(ctx);

		RemoteCache<String, String> metadataCache = cacheManager
				.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
		metadataCache.put("pessoa.proto", pessoaSchema);
		String errors = metadataCache
				.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
		if (errors != null) {
			throw new IllegalStateException(
					"Some Protobuf schema files contain errors:\n" + errors);
		}

	}

	@After
	public void tearDown() {
		pessoaCache.stop();
		cacheManager.stop();
	}
	
	public String[] getRandomlyNames(final int characterLength, final int generateSize) {
	    LinkedHashSet<String> list = new LinkedHashSet<String>();
	    for (int i = 0; i < generateSize; ++i) {
	        String name = null;
	        do {
	            name = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(
	                    org.apache.commons.lang.math.RandomUtils.nextInt(characterLength - 1) + 1);
	        }while(list.contains(name));
	        list.add(name);
	    }
	    return list.toArray(new String[]{});
	}

	@Test
	public void t01_cacheShouldNotBeNull() {
		assertNotNull("Cache não deve ser nulo", pessoaCache);
	}

	@Test
	//@Ignore
	public void t02a_putPessoaOnCache() {
		pessoaCache.withFlags(Flag.SKIP_CACHE_LOAD);
		int COUNT_PERSON = 150000;
		for(int i=0; i<COUNT_PERSON; i++){
			String nome = getRandomlyNames(50, 1)[0].toUpperCase();
			String rg = RandomUtils.nextInt(100000)+"";
			Pessoa pessoa = new Pessoa(UUID.randomUUID(), nome, rg);
			pessoaCache.put(pessoa.getId().toString(), pessoa);
		}
		logger.infof("Cache size: %s", pessoaCache.size());
	}
	
	@Test
	public void t02b_putSinglePersonOnCache(){
		Pessoa pessoa = new Pessoa(UUID.randomUUID(), "RAFAEL PEREIRA", "123456789");
		pessoaCache.put(pessoa.getId().toString(), pessoa);
	}

	@Test
	@Ignore
	public void t03_getAllPessoasFromCache() {

		logger.infof("----------------------------------------------------");

//		for (String id : pessoaCache.keySet()) {
//			Pessoa pessoa = (Pessoa) pessoaCache.get(id);
//			logger.infof(">> %s %s", pessoa.getId(), pessoa.getNome());
//		}

		assertTrue("Keys size > 0 ", pessoaCache.keySet().size() > 0);
		logger.infof("----------------------------------------------------");

	}

	@Test
	public void t05_printStatisticsFromRemoteCache() {
		logger.info("*** Server Statistics ***");
		StringBuilder result = new StringBuilder();
		for (Entry<String, String> entry : pessoaCache.stats().getStatsMap()
				.entrySet()) {
				result.append(entry).append("\n");
		}
		logger.infof(">> \n%s", result.toString());
		logger.infof("----------------------------------------------------");
	}

	@Test
	public void t06_shouldQueryByNome() {
		

		QueryFactory queryFactory = Search.getQueryFactory(pessoaCache);
		String NAME_PATTERN = getRandomlyNames(4, 1)[0].toUpperCase()+"%";
		Query query = queryFactory.from(Pessoa.class)
				.having("nome")
				.like(NAME_PATTERN)
				.toBuilder().build();
		long bef =System.currentTimeMillis();
		List<Pessoa> lista = query.list();
		long aft = System.currentTimeMillis();
		double elapsed = (aft-bef)/1000.0f;
		for (Pessoa pes : lista) {
			logger.infof("Primeira ocorrencia Pessoa por nome %s => key: %s, nome: %s, rg: %s", NAME_PATTERN, pes.getId(),
					pes.getNome(), pes.getRg());
			break;
		}
		
		logger.infof("Encontrados: %s | Elapsed: %.3f", lista.size(), elapsed);
		logger.infof("Cache size: %s", pessoaCache.size());
		assertTrue("Lista com size > 0", lista.size() > 0);

	}
	
	@Test
	public void t06_shouldQueryByRg() {
		
		
		QueryFactory queryFactory = Search.getQueryFactory(pessoaCache);
		String RG_PATTERN = "221%".toUpperCase();
		Query query = queryFactory.from(Pessoa.class)
				.having("rg")
				.like(RG_PATTERN)
				.toBuilder().build();
		long bef =System.currentTimeMillis();
		List<Pessoa> lista = query.list();
		long aft = System.currentTimeMillis();
		double elapsed = (aft-bef)/1000.0f;
		for (Pessoa pes : lista) {
			logger.infof("Primeira ocorrencia Pessoa por RG %s => key: %s, nome: %s, rg: %s", RG_PATTERN,pes.getId(),
					pes.getNome(), pes.getRg());
			break;
		}
		
		logger.infof("Encontrados: %s | Elapsed %.2f", lista.size(),elapsed);
		
		assertTrue("Lista com size > 0", lista.size() > 0);
		
	}

}
