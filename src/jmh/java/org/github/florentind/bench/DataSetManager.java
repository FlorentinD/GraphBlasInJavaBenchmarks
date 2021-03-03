package org.github.florentind.bench;

import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.io.file.PathUtils;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphalgo.core.Settings;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataSetManager {
    public static Map<String, String> DATA_SETS = new HashMap<>() {{
        put("LDBC01", "db_sf001_p064_regular_utc_41ce");
        put("POKEC", "pokec_40.db");
        put("LiveJournal", "LiveJournal.db");
    }};

    private final Path dataSetDir;
    private final Path workingDir;

    private final Map<GraphDatabaseAPI, Pair<Path, DatabaseManagementService>> apiServiceMap;

    public DataSetManager() {
        String datasetDir = System.getenv("GRB_JAVA_DATASETS");
        if (datasetDir == null) {
            throw new IllegalArgumentException("Dataset not set. Set GRB_JAVA_DATASETS to specify the dataset directory");
        }
        this.dataSetDir = Paths.get(datasetDir);
        this.workingDir = Paths.get("build/");
        System.out.println("Copy graph to: " + workingDir);
        apiServiceMap = new HashMap<>();
    }

    public GraphDatabaseAPI openDb(String datasetId) {
        if (!DATA_SETS.containsKey(datasetId)) {
            throw new RuntimeException("Unknown dataset name " + datasetId);
        }

        Path datasetDir = dataSetDir.resolve(DATA_SETS.get(datasetId));

        System.out.println("Look for dataset at: " + datasetDir.toAbsolutePath().toString());

        String workingCopyId = UUID.randomUUID().toString();
        Path workingCopy = workingDir.resolve(workingCopyId);
        Path workingCopyGraph = workingCopy.resolve(workingCopyId);

        System.out.println(workingCopyGraph.toAbsolutePath().toString());

        try {
            Files.createDirectories(workingCopyGraph);
            PathUtils.copyDirectory(datasetDir, workingCopyGraph);
        } catch (IOException e) {
            throw new RuntimeException("Could not create working copy", e);
        }

        DatabaseManagementService dbms = new DatabaseManagementServiceBuilder(workingCopyGraph)
                .setConfig(Settings.procedureUnrestricted(), List.of("gds.*"))
                .build();

        GraphDatabaseAPI db = (GraphDatabaseAPI) dbms.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
        Runtime.getRuntime().addShutdownHook(new Thread(dbms::shutdown));

        apiServiceMap.put(db, new Pair<>(dataSetDir, dbms));

        return db;
    }

    public void closeDb(GraphDatabaseAPI db) {
        if (db != null) {
            var dbEnv = apiServiceMap.get(db);
            dbEnv.b.shutdown();
            try {
                PathUtils.deleteDirectory(dbEnv.a);
            } catch (IOException e) {
                throw new RuntimeException("Could not delete working copy", e);
            }
        }
    }
}
