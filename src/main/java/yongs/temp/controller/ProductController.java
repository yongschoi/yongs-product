package yongs.temp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import yongs.temp.service.ProductEventService;

@RestController
@RequestMapping("/product")
public class ProductController {
	private Logger logger = LoggerFactory.getLogger(ProductController.class);
	
	@Autowired
    private ProductEventService eventService;
	
	@PostMapping(value = "/create",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> create(@RequestParam("file") MultipartFile file, @RequestPart("productStr") String  productStr) throws Exception{
    	logger.debug("yongs-product|ProductController|create({})", productStr);
 
    	HttpStatus status = null;
    	if( eventService.create(file, productStr))
    		status = HttpStatus.OK;
    	else
    		status = HttpStatus.NOT_ACCEPTABLE;
    		
    	return new ResponseEntity<String>(status);
    }
}
