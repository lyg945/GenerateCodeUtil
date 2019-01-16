package com.code.generate;

import com.code.generate.service.SysGeneratorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UtilApplicationTests {

	@Autowired
	private SysGeneratorService sysGeneratorService;

//	@Test
	public void generateTables()throws IOException {
		List<Map<String, Object>> maps = sysGeneratorService.queryList(new HashMap<>());
		List<String> tableName = maps.stream().map(e -> (String)e.get("tableName")).collect(Collectors.toList());
		byte[] bytes = sysGeneratorService.generatorCode(tableName.toArray(new String[]{}));
		File file = new File("E:/test.zip");
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(bytes);
		fileOutputStream.flush();
		fileOutputStream.close();
	}

	@Test
	public void generatePaatTables()throws IOException {
		List<Map<String, Object>> maps = sysGeneratorService.queryList(new HashMap<>());
		List<String> tableName = maps.stream().map(e -> (String)e.get("tableName")).collect(Collectors.toList());
//		byte[] bytes = sysGeneratorService.generatorPaatCode(tableName.toArray(new String[]{}));
		byte[] bytes = sysGeneratorService.generatorPaatCode(new String[]{
//				"business_project","business_project_field",
//				"business_project_form","epm_manager",
//				"epm_manager_field","institution_leader",
//				"institution_leader_field","institution_customer_source",
				"project_focus"

		});
		File file = new File("d:/test.zip");
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(bytes);
		fileOutputStream.flush();
		fileOutputStream.close();
	}
}
