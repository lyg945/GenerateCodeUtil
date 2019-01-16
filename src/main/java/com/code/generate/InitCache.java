//package com.code.generate;
//
//import com.code.generate.service.SysGeneratorService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Component
//public class InitCache implements ApplicationRunner {
//  protected final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//  @Autowired
//  SysGeneratorService sysGeneratorService;
//
//  @Override
//  public void run(ApplicationArguments args) throws Exception {
//    List<Map<String, Object>> maps = sysGeneratorService.queryList(new HashMap<>());
//    List<String> tableName = maps.stream().map(e -> (String)e.get("tableName")).collect(Collectors.toList());
//    byte[] bytes = sysGeneratorService.generatorPaatCode(tableName.toArray(new String[]{}));
//    File file = new File("/test.zip");
//    FileOutputStream fileOutputStream = new FileOutputStream(file);
//    fileOutputStream.write(bytes);
//    fileOutputStream.flush();
//    fileOutputStream.close();
//  }
//}
