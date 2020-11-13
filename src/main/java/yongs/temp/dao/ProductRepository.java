package yongs.temp.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import yongs.temp.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
	public Product findByCode(final String code);
	public Product findByName(final String name);
	public void deleteByCode(final String code);
}