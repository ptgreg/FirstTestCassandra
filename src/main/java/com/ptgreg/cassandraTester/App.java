package com.ptgreg.cassandraTester;

import java.util.Arrays;

import me.prettyprint.cassandra.model.CqlQuery;
import me.prettyprint.cassandra.model.CqlRows;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.*;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;

import java.util.UUID;
/**
 * Hello world!
 *
 */
public class App 
{
	static StringSerializer stringSerializer = StringSerializer.get();
	static UUIDSerializer uidSerializer = UUIDSerializer.get();
	
	public static void main( String[] args )
	{
		System.out.println( "Hello World!" );

		Cluster cluster = HFactory.getOrCreateCluster("test-cluster","localhost:9160");

		//Add the schema to the cluster.
		//"true" as the second param means that Hector will block until all nodes see the change.
		KeyspaceDefinition keyspaceDef = cluster.describeKeyspace("MyKeyspace");

		
		
		//If keyspace does not exist, the CFs don't exist either. => create them.
		if (keyspaceDef == null) {
			ColumnFamilyDefinition cfDef2 = HFactory.createColumnFamilyDefinition("MyKeyspace",
					"UserIdUrl2",
					ComparatorType.TIMEUUIDTYPE);
			cluster.addColumnFamily(cfDef2);
			
			ColumnFamilyDefinition cfDef1 = HFactory.createColumnFamilyDefinition("MyKeyspace",
					"UserIdUrl",
					ComparatorType.TIMEUUIDTYPE);
			cluster.addColumnFamily(cfDef1);
			
			ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition("MyKeyspace",
					"UserId",
					ComparatorType.UTF8TYPE);
			cluster.addColumnFamily(cfDef);

			int replicationFactor = 1;
			KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition("MyKeyspace",
					ThriftKsDef.DEF_STRATEGY_CLASS,
					replicationFactor,
					Arrays.asList(cfDef));
			cluster.addKeyspace(newKeyspace, true);
		}
		
		Keyspace ksp = HFactory.createKeyspace("MyKeyspace", cluster);
				
		
		Mutator<String> mutator = HFactory.createMutator(ksp, stringSerializer);
		
		UUID people = UUID.randomUUID();
		UUID timeUID = TimeUUIDUtils.getUniqueTimeUUIDinMillis();
		System.out.println(timeUID.toString());
		
		mutator.insert("55fdc9ca-09d5-4962-a683-60417e6439b0", "UserIdUrl2", HFactory.createColumn(TimeUUIDUtils.getUniqueTimeUUIDinMillis(), "http://google.fr", uidSerializer, stringSerializer));
		mutator.insert("55fdc9ca-09d5-4962-a683-60417e6439b0", "UserIdUrl2", HFactory.createColumn(TimeUUIDUtils.getUniqueTimeUUIDinMillis(), "http://bing.fr", uidSerializer, stringSerializer));
		mutator.insert("55fdc9ca-09d5-4962-a683-60417e6439b0", "UserIdUrl2", HFactory.createColumn(TimeUUIDUtils.getUniqueTimeUUIDinMillis(), "http://google.fr/1", uidSerializer, stringSerializer));
		
		mutator.execute();
		
		
		CqlQuery<String, String, String> cqlQuery =
				   new CqlQuery<String, String, String>(ksp,stringSerializer,stringSerializer,stringSerializer);
		cqlQuery.setQuery("SELECT * FROM UserIdUrl2");
		QueryResult<CqlRows<String, String, String>> result = cqlQuery.execute();

		CqlRows<String, String, String> rows = result.get();
		for (Row<String, String, String> row : rows.getList()) {
		    System.out.println(row.getKey() + ":");
		    for(HColumn<String, String> col : row.getColumnSlice().getColumns()) {
		        System.out.println(col.getName() + " : " + col.getValue());
		        }
		    }
		
		System.out.println( "Done" );
	}
}
