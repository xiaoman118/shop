package com.hongqiang.shop.modules.product.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hongqiang.shop.common.persistence.BaseDao;
import com.hongqiang.shop.common.persistence.BaseDaoImpl;
import com.hongqiang.shop.modules.product.entity.ProductCategory;

public interface ProductCategoryDao extends ProductCategoryDaoCustom, CrudRepository<ProductCategory, Long> {
	
}
	

/**
 * DAO自定义接口
 * 
 * @author ThinkGem
 */
interface ProductCategoryDaoCustom extends BaseDao<ProductCategory> {
	/**
	 * 获取所有顶级商品分类集合;
	 * 
	 * @param paramInteger
	 *            返回的顶级商品分类数目，若为null表示返回所有
	 * @return 所有顶级商品分类集合
	 * 
	 */
	List<ProductCategory> findRoots(Integer paramInteger);

	/**
	 * 根据ProductCategory对象，获取他的所有父类集合;
	 * 
	 * @param paramProductCategory
	 *            ProductCategory对象
	 * @param paramInteger
	 *            返回的顶级商品分类数目，若为null表示返回所有
	 * @return 所有父类集合
	 * 
	 */
	List<ProductCategory> findParents(ProductCategory paramProductCategory,
			Integer paramInteger);

	/**
	 * 根据ProductCategory对象，获取他的所有子类集合
	 * 
	 * @param paramProductCategory
	 *            ProductCategory对象
	 * @param paramInteger
	 *            返回的顶级商品分类数目，若为null表示返回所有
	 * @return 所有子类集合
	 * 
	 */
	List<ProductCategory> findChildren(ProductCategory paramProductCategory,
			Integer paramInteger);
}

/**
 * DAO自定义接口实现
 * 
 * @author ThinkGem
 */
@Repository
class ProductCategoryDaoImpl extends BaseDaoImpl<ProductCategory> implements
		ProductCategoryDaoCustom {

	@Override
	 public List<ProductCategory> findRoots(Integer count)
	  {
	    String str = "select productCategory from ProductCategory productCategory where productCategory.parent is null order by productCategory.order asc";
		//这里改成entityManager
	    TypedQuery localTypedQuery = this.getEntityManager().createQuery(str, ProductCategory.class).setFlushMode(FlushModeType.COMMIT);
	    if (count != null)
	      localTypedQuery.setMaxResults(count.intValue());
	    return localTypedQuery.getResultList();
	  }
	@Override
	  public List<ProductCategory> findParents(ProductCategory productCategory, Integer count)
	  {
	    if ((productCategory == null) || (productCategory.getParent() == null))
	      return Collections.emptyList();
	    String str = "select productCategory from ProductCategory productCategory where productCategory.id in (:ids) order by productCategory.grade asc";
		//这里改成entityManager
	    TypedQuery localTypedQuery = this.getEntityManager().createQuery(str, ProductCategory.class).setFlushMode(FlushModeType.COMMIT).setParameter("ids", productCategory.getTreePaths());
	    if (count != null)
	      localTypedQuery.setMaxResults(count.intValue());
	    return localTypedQuery.getResultList();
	  }

	@Override
	public List<ProductCategory> findChildren(ProductCategory productCategory, Integer count)
	  {
	    String str;
	    TypedQuery localTypedQuery;
	    if (productCategory != null)
	    {
	      str = "select productCategory from ProductCategory productCategory where productCategory.treePath like :treePath order by productCategory.order asc";
		  //这里改成entityManager
	      localTypedQuery = this.getEntityManager().createQuery(str, ProductCategory.class).setFlushMode(FlushModeType.COMMIT).setParameter("treePath", "%," + productCategory.getId() + "," + "%");
	    }
	    else
	    {
	      str = "select productCategory from ProductCategory productCategory order by productCategory.order asc";
		  //这里改成entityManager
	      localTypedQuery = this.getEntityManager().createQuery(str, ProductCategory.class).setFlushMode(FlushModeType.COMMIT);
	    }
	    if (count != null)
	      localTypedQuery.setMaxResults(count.intValue());
	    return packTheChildren(localTypedQuery.getResultList(), productCategory);
	  }
	  private List<ProductCategory> packTheChildren(List<ProductCategory> paramList, ProductCategory paramProductCategory)
	  {
	    ArrayList localArrayList = new ArrayList();
	    if (paramList != null)
	    {
	      Iterator localIterator = paramList.iterator();
	      while (localIterator.hasNext())
	      {
	        ProductCategory localProductCategory = (ProductCategory)localIterator.next();
	        if (localProductCategory.getParent() != paramProductCategory)
	          continue;
	        localArrayList.add(localProductCategory);
	        localArrayList.addAll(packTheChildren(paramList, localProductCategory));
	      }
	    }
	    return localArrayList;
	  }
}
