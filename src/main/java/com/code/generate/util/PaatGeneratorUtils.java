package com.code.generate.util;

import com.code.generate.entity.SysColumn;
import com.code.generate.entity.SysTable;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author luosai
 * @Description: (代码生成器工具类)
 * @date 2017-6-23 15:07
 */
public class PaatGeneratorUtils {

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("templates/paat/Entity.java.vm");
        templates.add("templates/paat/Dao.java.vm");
        templates.add("templates/paat/ApiRead.java.vm");
        templates.add("templates/paat/ApiWrite.java.vm");
        templates.add("templates/paat/ServiceReadImpl.java.vm");
        templates.add("templates/paat/ServiceWriteImpl.java.vm");
//        templates.add("templates/paat/Controller.java.vm");
        templates.add("templates/paat/Xml.java.vm");
        templates.add("templates/paat/Xml2.java.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();
        //表信息
        SysTable sysTable = new SysTable();
        sysTable.setTableName(table.get("tableName"));
        sysTable.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(sysTable.getTableName(), config.getString("tablePrefix"));
        sysTable.setClassName(className);
        sysTable.setClassname(StringUtils.uncapitalize(className));

        //列信息
        List<SysColumn> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            SysColumn sysColumn = new SysColumn();
            sysColumn.setColumnName(column.get("columnName"));
            sysColumn.setDataType(column.get("dataType"));
            sysColumn.setComments(column.get("columnComment"));
            sysColumn.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(sysColumn.getColumnName());
            sysColumn.setAttrName(attrName);
            sysColumn.setAttrname(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(sysColumn.getDataType(), "unknowType");
            sysColumn.setAttrType(attrType);

            //设置mybatis数据类型
            sysColumn.setMyBatisType(javaToMybatis(attrType));

            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && sysTable.getPk() == null) {
                sysTable.setPk(sysColumn);
            }

            columsList.add(sysColumn);
        }
        sysTable.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (sysTable.getPk() == null) {
            sysTable.setPk(sysTable.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        String entityPrefix = config.getString("entityPrefix");
        String classNameTemp = entityPrefix + sysTable.getClassName();
        String classnameTmep = sysTable.getClassname();
        String pathPrefix = config.getString("pathPrefix");
        String classNameLowercase = pathPrefix + sysTable.getClassname();

        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", sysTable.getTableName());
        map.put("comments", sysTable.getComments());
        map.put("pk", sysTable.getPk());
        map.put("className", classNameTemp);
        map.put("classNameLowercase", classNameLowercase);
        map.put("classname", classnameTmep);
        map.put("pathPrefix", pathPrefix);
        map.put("pathName", "/" + sysTable.getClassname().toLowerCase());
        map.put("columns", sysTable.getColumns());
        map.put("package", config.getString("package"));
        map.put("author", config.getString("author"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        String baseService ="com.jyb.common.util.service.BaseServiceImpl";
        if(config.getString("package").indexOf("jsb") > 0){
            baseService = baseService.replace("jyb","jsb");
        }
        map.put("baseService", baseService);

        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, classNameTemp, classnameTmep, config.getString("package"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + sysTable.getTableName(), e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    public static String javaToMybatis(String columnName) {
        if("char".equals(columnName)){
            return "VARCHAR";
        }
        switch (columnName){
            case "Boolean":
                return "BOOLEAN";
            case "Integer":
                return "INTEGER";
//            case "Long":
//                return "BIGINT";
//            case "Long":
//                return "BIGINT";
//            case "Long":
//                return "BIGINT";
//            case "Long":
//                return "BIGINT";
//            case "Long":
//                return "BIGINT";
//            case "Long":
//                return "BIGINT";

        }
        return null;
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String classname, String packageName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }

        if (template.contains("Entity.java.vm")) {
            return packagePath + "api" + File.separator  + "model" + File.separator + className + ".java";
        }

        if (template.contains("Dao.java.vm")) {
            return packagePath + "provider" + File.separator  + "dao" + File.separator + className + "Mapper.java";
        }
        if (template.contains("ApiRead.java.vm")) {
            return packagePath + "api" + File.separator  + "service" + File.separator + className + "ReadService.java";
        }
        if (template.contains("ApiWrite.java.vm")) {
            return packagePath + "api" + File.separator + "service" + File.separator + className + "WriteService.java";
        }

        if (template.contains("ServiceReadImpl.java.vm")) {
            return packagePath + "provider" + File.separator  + "service" + File.separator + "impl" + File.separator + className + "ReadServiceImpl.java";
        }

        if (template.contains("ServiceWriteImpl.java.vm")) {
            return packagePath + "provider" + File.separator  + "service" + File.separator + "impl" + File.separator + className + "WriteServiceImpl.java";
        }

        if (template.contains("Controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if (template.contains("Xml.java.vm")) {
            return packagePath + "provider" + File.separator  + "mapper" + File.separator + className + "Mapper.xml";
        }

        if (template.contains("Xml2.java.vm")) {
            return packagePath + "provider" + File.separator  + "mapper" + File.separator + className + "Mapper2.xml";
        }

        return null;
    }

}
