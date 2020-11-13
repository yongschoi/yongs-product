package yongs.temp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import yongs.temp.dao.ProductRepository;
import yongs.temp.model.Product;

@Service
public class ProductEventService {
	private Logger logger = LoggerFactory.getLogger(ProductEventService.class);	
	
	@Autowired
    ProductRepository repo;
	@Autowired
	ProductFileService productFileService;
	
	@Autowired
    KafkaTemplate<String, String> kafkaTemplate;
	
	// for sender
	private static final String PRODUCT_STOCK_NEW_SND = "product-stock-new";
	// for listener
	private static final String STOCK_NEW_ROLLBACK_LSN = "stock-new-rollback";

	public boolean create(MultipartFile file, String productStr) throws JsonProcessingException {
		logger.debug("flex-product|ProductEventService|create()");
		boolean result = true;
		ObjectMapper mapper = new ObjectMapper();
		Product product = mapper.readValue(productStr, Product.class);
		
		// 1. validation 체크
		// 1-1 code 체크
		if(null != repo.findByCode(product.getCode())) return false;
		// 1-2 name 체크
		if(null != repo.findByName(product.getName())) return false;
		// 1-3 파일이름 체크
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		if(productFileService.dupCheckFileName(fileName)) return false;
	
		// 2. 저장
		// 2-1 Product 이미지 파일 저장하고
		productFileService.saveFile(file);
		
		// 2-2 Product db에 저장하고
		product.setImage(fileName);
		repo.save(product);

		// 3. Stock db에 저장하기위해 kafka event 발생(Kafka Message로 사용하기 위해 setImage한 Object를 String으로 변환)
		String setImageStr = mapper.writeValueAsString(product);
		kafkaTemplate.send(PRODUCT_STOCK_NEW_SND, setImageStr);
		logger.debug("[PRODUCT to STOCK (신규)] Code [" + product.getCode() + "]");
		
		return result;
	}

	@KafkaListener(topics = STOCK_NEW_ROLLBACK_LSN)
	public void rollback(String productStr, Acknowledgment ack) {		 
		try {
			ObjectMapper mapper = new ObjectMapper();
			Product product = mapper.readValue(productStr, Product.class);			
			// 이미지 파일 삭제
			productFileService.deleteFile(product.getImage());
			// 해당 데이터 삭제
			repo.deleteByCode(product.getCode());
			ack.acknowledge();
			logger.debug("[Product Rollback] Product Code [" + product.getCode() + "]");		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
