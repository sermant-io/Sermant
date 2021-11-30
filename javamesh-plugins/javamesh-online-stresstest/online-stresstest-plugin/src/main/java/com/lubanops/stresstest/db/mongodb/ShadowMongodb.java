/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.db.mongodb;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.config.bean.MongoSourceInfo;
import com.lubanops.stresstest.core.Reflection;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.client.internal.MongoDatabaseImpl;
import com.mongodb.client.internal.OperationExecutor;
import com.mongodb.connection.ClusterSettings;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 影子Mongodb
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class ShadowMongodb {
    private static final Map<String, Object> SHADOW_CLIENTS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final ShadowMongodb INSTANCE = new ShadowMongodb();

    private ShadowMongodb() { }

    public static ShadowMongodb getInstance() {
        return INSTANCE;
    }

    /**
     * 根据原始的client， databasename 返回影子的client。如果返回的client有效，把原始的databasename-> client映射放到map中供后续使用。
     *
     * @param mongoClient 原始的clint
     * @param database 原始的database名字
     * @return 影子database。
     */
    public MongoDatabase getShadowDatabaseFromClient(Object mongoClient, String database) {
        MongoSourceInfo info = ConfigFactory.getConfig().getShadowMongoSourceInfo(database);
        if (mongoClient instanceof MongoClient) {
            MongoClient client = newShadowOldMongoClient((MongoClient) mongoClient, database, info);
            MongoClientOptions clientOptions = client.getMongoClientOptions();
            return new MongoDatabaseImpl(info.getDatabase(), (CodecRegistry) Objects.requireNonNull(
                    Reflection.invokeDeclared("getCodecRegistry", client).orElse(null)),
                    clientOptions.getReadPreference(), clientOptions.getWriteConcern(), clientOptions.getRetryWrites(),
                    clientOptions.getRetryReads(), clientOptions.getReadConcern(), clientOptions.getUuidRepresentation(),
                    (OperationExecutor) Objects.requireNonNull(
                            Reflection.invokeDeclared("createOperationExecutor", client).orElse(null)));
        } else if (mongoClient instanceof MongoClientImpl) {
            MongoClientImpl client = newShadowNewMongoClient((MongoClientImpl) mongoClient, database, info);
            return Reflection.getDeclaredValue("settings", client).map(settings -> {
                if (settings instanceof MongoClientSettings) {
                    return new MongoDatabaseImpl(info.getDatabase(), client.getCodecRegistry(),
                            ((MongoClientSettings)settings).getReadPreference(),
                            ((MongoClientSettings)settings).getWriteConcern(),
                            ((MongoClientSettings)settings).getRetryWrites(),
                            ((MongoClientSettings)settings).getRetryReads(),
                            ((MongoClientSettings)settings).getReadConcern(),
                            ((MongoClientSettings)settings).getUuidRepresentation(),
                            (OperationExecutor) Objects.requireNonNull(Reflection.getDeclaredValue("delegate", client)
                                    .flatMap(delegate -> Reflection.invokeDeclared("getOperationExecutor", delegate))
                                    .orElse(null)));
                }
                return null;
            }).orElse(null);
            } else {
            LOGGER.severe(String.format("Unknown mongoClient %s", mongoClient.getClass().getName()));
        }
        return null;
    }

    /**
     * 返回新版本的影子MongoClient
     *
     * @param client mongocleint
     * @param database database
     * @return shadow client
     */
    private MongoClientImpl newShadowNewMongoClient(MongoClientImpl client, String database, MongoSourceInfo info) {
        if (SHADOW_CLIENTS.containsKey(database)) {
            return (MongoClientImpl) SHADOW_CLIENTS.get(database);
        } else {
            ClusterSettings clusterSettings = client.getCluster().getSettings();
            final ClusterSettings.Builder clusterBuilder = ClusterSettings.builder(clusterSettings);

            clusterBuilder.hosts(info.getAddresses());
            MongoClientSettings.Builder clientBuilders = Reflection.getDeclaredValue("settings", client).map(settings
                    -> getFromSettings(settings, info, database)).orElse(MongoClientSettings.builder());
            clientBuilders.applyToClusterSettings(builder -> builder.applySettings(clusterBuilder.build()));
            com.mongodb.client.MongoClient shadowClient =  MongoClients.create(clientBuilders.build());
            SHADOW_CLIENTS.put(database, shadowClient);
            return (MongoClientImpl) shadowClient;
        }
    }

    private MongoClientSettings.Builder getFromSettings(Object settings, MongoSourceInfo info, String database) {
        MongoClientSettings.Builder clientBuilders;
        if (settings instanceof MongoClientSettings) {
            MongoClientSettings clientSettings = (MongoClientSettings) settings;
            clientBuilders = MongoClientSettings.builder(clientSettings);
            MongoCredential credential = clientSettings.getCredential();
            if (credential != null) {
                MongoCredential shadowCredential = createShadowCredential(credential, database, info);
                clientBuilders.credential(shadowCredential);
            }
        } else {
            clientBuilders = MongoClientSettings.builder();
        }
        return clientBuilders;
    }

    /**
     * 返回老版本的影子MongoClient
     *
     * @param client mongocleint
     * @param database database
     * @return shadow client
     */
    private MongoClient newShadowOldMongoClient(MongoClient client, String database, MongoSourceInfo info) {
        if (SHADOW_CLIENTS.containsKey(database)) {
            return (MongoClient) SHADOW_CLIENTS.get(database);
        } else {
            List<MongoCredential> credentials = client.getCredentialsList();
            MongoClientOptions clientOptions = client.getMongoClientOptions();
            MongoClient shadowClient;
            if (credentials.size() > 0) {
                MongoCredential credential = createShadowCredential(credentials.get(0), database, info);
                shadowClient = new MongoClient(info.getAddresses(), credential, clientOptions);
            } else {
                shadowClient = new MongoClient(info.getAddresses(), clientOptions);
            }
            SHADOW_CLIENTS.put(database, shadowClient);
            return shadowClient;
        }
    }

    private MongoCredential createShadowCredential(MongoCredential credential, String originDatabase, MongoSourceInfo info) {
        StringBuilder builder = new StringBuilder();
        if (info.getUserPrefix() != null) {
            builder.append(info.getUserPrefix());
        }
        builder.append(credential.getUserName());
        if (info.getUserSuffix() != null) {
            builder.append(info.getUserSuffix());
        }
        AuthenticationMechanism mechanism = credential.getAuthenticationMechanism();
        String database;
        if (originDatabase.equalsIgnoreCase(credential.getSource())) {
            database = info.getDatabase();
        } else {
            database = credential.getSource();
        }
        if (mechanism == null) {
            return MongoCredential.createCredential(builder.toString(), database, credential.getPassword());
        }
        switch (mechanism) {
            case PLAIN:
                return MongoCredential.createPlainCredential(builder.toString(), database, credential.getPassword());
            case SCRAM_SHA_1:
                return MongoCredential.createScramSha1Credential(builder.toString(), database,
                        credential.getPassword());
            case SCRAM_SHA_256:
                return MongoCredential.createScramSha256Credential(builder.toString(), database,
                        credential.getPassword());
            case GSSAPI:
                return MongoCredential.createGSSAPICredential(builder.toString());
            case MONGODB_X509:
                return MongoCredential.createMongoX509Credential(builder.toString());
            case MONGODB_CR:
                return MongoCredential.createMongoCRCredential(builder.toString(), database, credential.getPassword());
        }
        return credential;
    }
}
