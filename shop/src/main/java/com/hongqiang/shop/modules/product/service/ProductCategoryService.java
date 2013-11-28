package com.hongqiang.shop.modules.product.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hongqiang.shop.common.service.BaseService;
import com.hongqiang.shop.modules.product.dao.ProductCategoryDao;
import com.hongqiang.shop.modules.product.entity.ProductCategory;

@Service
@Transactional(readOnly = true)
public class ProductCategoryService extends BaseService {
	@Autowired
	private ProductCategoryDao productCategoryDao;

	/**
	 * 获取顶级分类下的所有商品集合（只包含isMarketable=true的对象）
	 * 
	 * @param
	 * 
	 * @return 顶级分类下的所有商品集合
	 */
	public List<ProductCategory> findRoots() {
		return this.productCategoryDao.findRoots(null);
	}

	/**
	 * 根据设定的结果数，获取顶级分类下的所有商品集合（只包含isMarketable=true的对象）
	 * 
	 * @param paramInteger
	 *            设定的结果数
	 * 
	 * @return 顶级分类下的所有商品集合
	 */
	public List<ProductCategory> findRoots(Integer count) {
		return this.productCategoryDao.findRoots(count);
	}

	/**
	 * 
	 根据最大结果数，获取顶级分类下的所有商品集合（只包含isMarketable=true的对象）
	 * 
	 * @param paramInteger
	 *            设定的结果数
	 * @param paramString
	 * 
	 * @return 顶级分类下的所有商品集合
	 */
	public List<ProductCategory> findRoots(Integer count, String cacheRegion) {
		return this.productCategoryDao.findRoots(count);
	}

	public List<ProductCategory> findParents(ProductCategory productCategory) {
		return this.productCategoryDao.findParents(productCategory, null);
	}

	public List<ProductCategory> findParents(ProductCategory productCategory,
			Integer count) {
		return this.productCategoryDao.findParents(productCategory, count);
	}

	public List<ProductCategory> findParents(ProductCategory productCategory,
			Integer count, String cacheRegion) {
		return this.productCategoryDao.findParents(productCategory, count);
	}

	public List<ProductCategory> findTree() {
		return this.productCategoryDao.findChildren(null, null);
	}

	public List<ProductCategory> findChildren(ProductCategory productCategory) {
		return this.productCategoryDao.findChildren(productCategory, null);
	}

	public List<ProductCategory> findChildren(ProductCategory productCategory,
			Integer count) {
		return this.productCategoryDao.findChildren(productCategory, count);
	}

	public List<ProductCategory> findChildren(ProductCategory productCategory,
			Integer count, String cacheRegion) {
		return this.productCategoryDao.findChildren(productCategory, count);
	}

	public void save(ProductCategory productCategory) {
		productCategoryDao.getEntityManager().persist(productCategory);
	}
	public ProductCategory update(ProductCategory productCategory) {
		productCategoryDao.getEntityManager().persist(productCategory);
		return productCategory;
	}

	// Remove the entity instance by id.
	public void delete(Long id) {
		productCategoryDao.getEntityManager().remove(id);
	}

}