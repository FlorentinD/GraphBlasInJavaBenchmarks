package org.github.florentind.bench;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.Settings;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSetManager {
    public static Map<String, String> DATA_SETS = new HashMap<>() {{
        put("Patents", "patents.db");
        put("POKEC", "pokec_40.db");
        put("Facebook", "facebook.db");
        put("Slashdot0902", "slashdot0902.db");
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

        if(!Files.exists(datasetDir)) {
            throw new IllegalArgumentException("Directory not existing " + datasetDir.toAbsolutePath().toString());
        }

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
        DataSetManager dbManager = new DataSetManager();
        var db = dbManager.openDb("POKEC");
        var tx = db.beginTx();
        System.out.println(tx.execute("Match (n) return count(n) as nodeCount").resultAsString());
        tx.close();

        // undirected for TriangleCount
        var hugeGraph = (CSRGraph) new StoreLoaderBuilder()
                .api(db)
                .globalOrientation(Orientation.UNDIRECTED)
                .globalAggregation(Aggregation.SINGLE)
                .build()
                .graphStore()
                .getUnion();


        try {
            exportToCsv(dbManager.dataSetDir.resolve("pokec_undirected.csv").toAbsolutePath().toString(), hugeGraph);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dbManager.closeDb(db);
        }
    }

    private static void exportToCsv(String outPath, CSRGraph hugeGraph) throws IOException {
        var writer = new FileWriter(outPath);
        PrintWriter out = new PrintWriter(writer);
        out.printf("%d %d\n", hugeGraph.nodeCount(), hugeGraph.relationshipCount());

        if (!hugeGraph.hasRelationshipProperty()) {
            String lineFormat = "%d %d \n";
            hugeGraph.forEachNode(nodeId -> {
                hugeGraph.forEachRelationship(nodeId, (src, trg) -> {
                    out.printf(lineFormat, src, trg);
                    return true;
                });
                return true;
            });
        } else {
            String lineFormat = "%d %d %f \n";
            hugeGraph.forEachNode(nodeId -> {
                hugeGraph.forEachRelationship(nodeId, 1.0,(src, trg, weight) -> {
                    out.printf(lineFormat, src, trg, weight);
                    return true;
                });
                return true;
            });
        }

        out.flush();
    }
}
