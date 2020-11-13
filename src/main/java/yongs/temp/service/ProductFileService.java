package yongs.temp.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import yongs.temp.config.FileControlConfig;
import yongs.temp.exception.FileException;

@Service
public class ProductFileService {
	private Logger logger = LoggerFactory.getLogger(ProductFileService.class);	
	
	private final Path fileLocation;

    @Autowired
    public ProductFileService(FileControlConfig config) {
        this.fileLocation = Paths.get(config.getProductImgDir()).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileLocation);
        }catch(Exception e) {
            throw new FileException("파일을 업로드할 디렉토리를 생성하지 못했습니다.");
        }
    }
    
    public void saveFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // 파일명에 부적합 문자가 있는지 확인한다.
            if(fileName.contains(".."))
                throw new FileUploadException("파일명에 부적합 문자가 포함되어 있습니다. " + fileName);
            
            Path targetLocation = this.fileLocation.resolve(fileName);            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }catch(Exception e) {
            throw new FileException("["+fileName+"] 파일 업로드에 실패하였습니다. 다시 시도하십시오.");
        }
    } 

    public void deleteFile(String fileName) {
    	logger.debug("yongs-product|ProductFileService|deleteFile({})", fileName);
    	Path targetLocation = this.fileLocation.resolve(fileName);

        try {
        	Files.deleteIfExists(targetLocation);
        }catch(Exception e) {
            throw new FileException("["+fileName+"] 파일 삭제에 실패하였습니다. 다시 시도하십시오.");
        }
    }
    
    public boolean dupCheckFileName(String fileName) {
    	Path targetLocation = this.fileLocation.resolve(fileName);
    	return Files.exists(targetLocation);
    }
}
