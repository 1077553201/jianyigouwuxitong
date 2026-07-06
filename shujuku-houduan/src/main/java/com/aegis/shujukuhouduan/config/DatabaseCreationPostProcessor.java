package com.aegis.shujukuhouduan.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 在任何 Bean（DataSource、EntityManagerFactory）实例化之前，
 * 确保目标数据库以 utf8mb4 字符集存在，并修正已有表的字符集。
 *
 * 为什么用 BeanFactoryPostProcessor：
 *   它是 Spring 生命周期中最早的扩展点之一，postProcessBeanFactory()
 *   在所有普通 Bean 实例化之前执行，彻底避免了 Hibernate 以错误字符集建表的问题。
 *   相比 @PostConstruct + @DependsOn，无法保证先于 EntityManagerFactory 运行；
 *   本方案从根本上解决了时序问题。
 */
@Component
public class DatabaseCreationPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String url      = environment.getProperty("spring.datasource.url", "");
        String username = environment.getProperty("spring.datasource.username", "");
        String password = environment.getProperty("spring.datasource.password", "");

        if (url.isEmpty()) {
            System.err.println("[DB Init] spring.datasource.url 未配置，跳过字符集初始化");
            return;
        }

        try {
            // 解析 JDBC URL：jdbc:mysql://host:port/dbName?params
            int schemeEnd  = url.indexOf("//") + 2;
            int dbSlash    = url.indexOf('/', schemeEnd);
            if (dbSlash < 0) {
                throw new IllegalArgumentException("JDBC URL 格式不正确，无法解析数据库名: " + url);
            }
            int    queryStart = url.indexOf('?', dbSlash);
            String dbName     = queryStart > 0 ? url.substring(dbSlash + 1, queryStart)
                                               : url.substring(dbSlash + 1);
            String params     = queryStart > 0 ? url.substring(queryStart) : "";

            // 构建连接 MySQL 服务器（不指定数据库）的 URL，并移除 createDatabaseIfNotExist 参数
            String serverUrl = url.substring(0, dbSlash + 1) + params;
            serverUrl = serverUrl
                    .replace("createDatabaseIfNotExist=true&", "")
                    .replace("&createDatabaseIfNotExist=true", "")
                    .replace("?createDatabaseIfNotExist=true", "");

            System.out.println("[DB Init] 正在初始化数据库 `" + dbName + "` 的字符集...");
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(serverUrl, username, password)) {

                try (Statement stmt = conn.createStatement()) {
                    // 步骤 1：创建数据库（如不存在），直接指定 utf8mb4
                    stmt.execute("CREATE DATABASE IF NOT EXISTS `" + dbName
                            + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    // 步骤 2：强制将数据库默认字符集更新为 utf8mb4
                    //         （处理数据库已存在但字符集错误的情况）
                    stmt.execute("ALTER DATABASE `" + dbName
                            + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }

                // 步骤 3：收集并修正已有表的字符集
                List<String> tables = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs   = stmt.executeQuery(
                             "SELECT TABLE_NAME FROM information_schema.TABLES "
                             + "WHERE TABLE_SCHEMA = '" + dbName + "' AND TABLE_TYPE = 'BASE TABLE'")) {
                    while (rs.next()) {
                        tables.add(rs.getString(1));
                    }
                }

                if (tables.isEmpty()) {
                    System.out.println("[DB Init] 数据库为空，Hibernate 将以 utf8mb4 新建所有表");
                } else {
                    try (Statement stmt = conn.createStatement()) {
                        for (String table : tables) {
                            try {
                                stmt.execute("ALTER TABLE `" + dbName + "`.`" + table
                                        + "` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                                System.out.println("[DB Init] 已转换表 `" + table + "` → utf8mb4");
                            } catch (SQLException ex) {
                                System.err.println("[DB Init] 转换表 `" + table + "` 失败: " + ex.getMessage());
                            }
                        }
                    }
                }
                System.out.println("[DB Init] 数据库 `" + dbName + "` 字符集初始化完成");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("[DB Init] 数据库字符集初始化失败: " + e.getMessage(), e);
        }
    }
}
