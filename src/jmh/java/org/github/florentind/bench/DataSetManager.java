package org.github.florentind.bench;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphalgo.core.Settings;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSetManager {
    public static Map<String, String> DATA_SETS = new HashMap<>() {{
        put("LDBC01", "ldbc01.db");
        put("POKEC", "pokec_40.db");
        put("LiveJournal", "LiveJournal.db");
    }};

    private final Path dataSetDir;

    private final Map<GraphDatabaseAPI, DatabaseManagementService> apiServiceMap;

    public DataSetManager() {
        this(System.getenv("GRB_JAVA_DATASETS"));
    }

    public DataSetManager(String datasetDir) {
        if (datasetDir == null) {
            throw new IllegalArgumentException("Dataset not set. Set GRB_JAVA_DATASETS to specify the dataset directory");
        }
        this.dataSetDir = Paths.get(datasetDir);
        apiServiceMap = new HashMap<>();
    }

    public GraphDatabaseAPI openDb(String datasetId) {
        if (!DATA_SETS.containsKey(datasetId)) {
            throw new RuntimeException("Unknown dataset name " + datasetId);
        }

        Path datasetDir = dataSetDir.resolve(DATA_SETS.get(datasetId));

        System.out.println("Look for dataset at: " + datasetDir.toAbsolutePath().toString());

        DatabaseManagementService dbms = new DatabaseManagementServiceBuilder(datasetDir)
                .setConfig(Settings.procedureUnrestricted(), List.of("gds.*"))
                .setConfig(Settings.failOnMissingFiles(), false)
                .build();

        GraphDatabaseAPI db = (GraphDatabaseAPI) dbms.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
        Runtime.getRuntime().addShutdownHook(new Thread(dbms::shutdown));

        apiServiceMap.put(db, dbms);

        return db;
    }

    public void closeDb(GraphDatabaseAPI db) {
        if (db != null) {
            apiServiceMap.get(db).shutdown();
        }
    }

    public static void main(String[] args) {
        DataSetManager dbManager = new DataSetManager("/home/florentin/masterThesis/graphblasOnJavaBenchmarks/datasets/");
        var db = dbManager.openDb("LDBC01");

        var tx = db.beginTx();
        System.out.println(tx.execute("Match (n) return count(n) as nodeCount").resultAsString());
        tx.close();
        dbManager.closeDb(db);
    }
}
