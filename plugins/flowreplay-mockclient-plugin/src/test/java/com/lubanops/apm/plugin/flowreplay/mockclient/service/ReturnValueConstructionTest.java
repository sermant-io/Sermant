package com.lubanops.apm.plugin.flowreplay.mockclient.service;

import com.sun.rowset.WebRowSetImpl;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

/**
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-10
 */
public class ReturnValueConstructionTest {

    @Test
    public void constructMysqlReturnValue() throws SQLException {
        String xml = "<?xml version=\"1.0\"?>\n" +
            "<webRowSet xmlns=\"http://java.sun.com/xml/ns/jdbc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xsi:schemaLocation=\"http://java.sun.com/xml/ns/jdbc http://java.sun.com/xml/ns/jdbc/webrowset.xsd\">\n" +
            "  <properties>\n" +
            "    <command><null/></command>\n" +
            "    <concurrency>1008</concurrency>\n" +
            "    <datasource><null/></datasource>\n" +
            "    <escape-processing>true</escape-processing>\n" +
            "    <fetch-direction>1000</fetch-direction>\n" +
            "    <fetch-size>0</fetch-size>\n" +
            "    <isolation-level>2</isolation-level>\n" +
            "    <key-columns>\n" +
            "    </key-columns>\n" +
            "    <map>\n" +
            "    </map>\n" +
            "    <max-field-size>0</max-field-size>\n" +
            "    <max-rows>0</max-rows>\n" +
            "    <query-timeout>0</query-timeout>\n" +
            "    <read-only>true</read-only>\n" +
            "    <rowset-type>ResultSet.TYPE_SCROLL_INSENSITIVE</rowset-type>\n" +
            "    <show-deleted>false</show-deleted>\n" +
            "    <table-name><null/></table-name>\n" +
            "    <url><null/></url>\n" +
            "    <sync-provider>\n" +
            "      <sync-provider-name>com.sun.rowset.providers.RIOptimisticProvider</sync-provider-name>\n" +
            "      <sync-provider-vendor>Oracle Corporation</sync-provider-vendor>\n" +
            "      <sync-provider-version>1.0</sync-provider-version>\n" +
            "      <sync-provider-grade>2</sync-provider-grade>\n" +
            "      <data-source-lock>1</data-source-lock>\n" +
            "    </sync-provider>\n" +
            "  </properties>\n" +
            "  <metadata>\n" +
            "    <column-count>3</column-count>\n" +
            "    <column-definition>\n" +
            "      <column-index>1</column-index>\n" +
            "      <auto-increment>false</auto-increment>\n" +
            "      <case-sensitive>false</case-sensitive>\n" +
            "      <currency>false</currency>\n" +
            "      <nullable>1</nullable>\n" +
            "      <signed>false</signed>\n" +
            "      <searchable>true</searchable>\n" +
            "      <column-display-size>20</column-display-size>\n" +
            "      <column-label>age</column-label>\n" +
            "      <column-name>age</column-name>\n" +
            "      <schema-name></schema-name>\n" +
            "      <column-precision>20</column-precision>\n" +
            "      <column-scale>0</column-scale>\n" +
            "      <table-name>user</table-name>\n" +
            "      <catalog-name>demo</catalog-name>\n" +
            "      <column-type>12</column-type>\n" +
            "      <column-type-name>VARCHAR</column-type-name>\n" +
            "    </column-definition>\n" +
            "    <column-definition>\n" +
            "      <column-index>2</column-index>\n" +
            "      <auto-increment>false</auto-increment>\n" +
            "      <case-sensitive>false</case-sensitive>\n" +
            "      <currency>false</currency>\n" +
            "      <nullable>0</nullable>\n" +
            "      <signed>false</signed>\n" +
            "      <searchable>true</searchable>\n" +
            "      <column-display-size>20</column-display-size>\n" +
            "      <column-label>name</column-label>\n" +
            "      <column-name>name</column-name>\n" +
            "      <schema-name></schema-name>\n" +
            "      <column-precision>20</column-precision>\n" +
            "      <column-scale>0</column-scale>\n" +
            "      <table-name>user</table-name>\n" +
            "      <catalog-name>demo</catalog-name>\n" +
            "      <column-type>12</column-type>\n" +
            "      <column-type-name>VARCHAR</column-type-name>\n" +
            "    </column-definition>\n" +
            "    <column-definition>\n" +
            "      <column-index>3</column-index>\n" +
            "      <auto-increment>false</auto-increment>\n" +
            "      <case-sensitive>false</case-sensitive>\n" +
            "      <currency>false</currency>\n" +
            "      <nullable>1</nullable>\n" +
            "      <signed>false</signed>\n" +
            "      <searchable>true</searchable>\n" +
            "      <column-display-size>20</column-display-size>\n" +
            "      <column-label>number</column-label>\n" +
            "      <column-name>number</column-name>\n" +
            "      <schema-name></schema-name>\n" +
            "      <column-precision>20</column-precision>\n" +
            "      <column-scale>0</column-scale>\n" +
            "      <table-name>user</table-name>\n" +
            "      <catalog-name>demo</catalog-name>\n" +
            "      <column-type>12</column-type>\n" +
            "      <column-type-name>VARCHAR</column-type-name>\n" +
            "    </column-definition>\n" +
            "  </metadata>\n" +
            "  <data>\n" +
            "    <currentRow>\n" +
            "      <columnValue>age</columnValue>\n" +
            "      <columnValue>name</columnValue>\n" +
            "      <columnValue>number</columnValue>\n" +
            "    </currentRow>\n" +
            "  </data>\n" +
            "</webRowSet>";
        WebRowSetImpl webRowSet = (WebRowSetImpl) ReturnValueConstruction.ConstructMysqlReturnValue("com.mysql.cj.jdbc.result.ResultSetImpl", xml);
        if (webRowSet.next()) {
            Assert.assertEquals("name", webRowSet.getString("name"));
            Assert.assertEquals("number", webRowSet.getString("number"));
            Assert.assertEquals("age", webRowSet.getString("age"));
        }
        Assert.assertTrue((Boolean) ReturnValueConstruction.ConstructMysqlReturnValue("java.lang.Boolean", "true"));
        Assert.assertFalse((Boolean) ReturnValueConstruction.ConstructMysqlReturnValue("java.lang.Boolean", "false"));
    }
}