package yongs.temp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("filecontrol")
public class FileControlConfig {
    private String productImgDir;

	public String getProductImgDir() {
		return productImgDir;
	}

	public void setProductImgDir(String productImgDir) {
		this.productImgDir = productImgDir;
	}
}
