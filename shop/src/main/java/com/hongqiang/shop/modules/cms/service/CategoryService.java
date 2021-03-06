/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.hongqiang.shop.modules.cms.service;

import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hongqiang.shop.common.persistence.Page;
import com.hongqiang.shop.common.service.BaseService;
import com.hongqiang.shop.modules.cms.dao.CategoryDao;
import com.hongqiang.shop.modules.cms.entity.Category;
import com.hongqiang.shop.modules.cms.entity.Site;
import com.hongqiang.shop.modules.cms.utils.CmsUtils;
import com.hongqiang.shop.modules.sys.entity.User;
import com.hongqiang.shop.modules.sys.utils.UserUtils;

/**
 * 栏目Service
 * @author ThinkGem
 * @version 2013-5-31
 */
@Service
@Transactional(readOnly = true)
public class CategoryService extends BaseService {

	public static final String CACHE_CATEGORY_LIST = "categoryList";
	
	@Autowired
	private CategoryDao categoryDao;
	
	public Category get(Long id) {
		return categoryDao.findOne(id);
	}
	
	@SuppressWarnings("unchecked")
	public List<Category> findByUser(boolean isCurrentSite, String module){
		
		List<Category> list = (List<Category>)UserUtils.getCache(CACHE_CATEGORY_LIST);
		if (list == null){
			User user = UserUtils.getUser();
			DetachedCriteria dc = categoryDao.createDetachedCriteria();
			dc.createAlias("office", "office").createAlias("createBy", "user");
			dc.add(dataScopeFilter(user, "office", "user"));
//			dc.add(Restrictions.or(Restrictions.isNull("href"),Restrictions.eq("href", "")));
			dc.add(Restrictions.eq("delFlag", Category.DEL_FLAG_NORMAL));
			dc.addOrder(Order.asc("site.id")).addOrder(Order.asc("sort"));
			list = categoryDao.find(dc);
			// 将没有父节点的节点，找到父节点
			Set<Long> parentIdSet = Sets.newHashSet();
			for (Category e : list){
				if (e.getParent()!=null && e.getParent().getId()!=null){
					boolean isExistParent = false;
					for (Category e2 : list){
						if (e.getParent().getId().longValue() == e2.getId().longValue()){
							isExistParent = true;
							break;
						}
					}
					if (!isExistParent){
						parentIdSet.add(e.getParent().getId());
					}
				}
			}
			if (parentIdSet.size() > 0){
				dc = categoryDao.createDetachedCriteria();
				dc.add(Restrictions.in("id", parentIdSet));
				dc.add(Restrictions.eq("delFlag", Category.DEL_FLAG_NORMAL));
				dc.addOrder(Order.asc("site.id")).addOrder(Order.asc("sort"));
				list.addAll(0, categoryDao.find(dc));
			}
			UserUtils.putCache(CACHE_CATEGORY_LIST, list);
		}
		
		if (isCurrentSite){
			List<Category> categoryList = Lists.newArrayList(); 
			for (Category e : list){
				if (Category.isRoot(e.getId()) || (e.getSite()!=null && e.getSite().getId() !=null 
						&& e.getSite().getId().longValue() == Site.getCurrentSiteId())){
					if (StringUtils.isNotEmpty(module)){
						if (module.equals(e.getModule()) || "".equals(e.getModule())){
							categoryList.add(e);
						}
					}else{
						categoryList.add(e);
					}
				}
			}
			return categoryList;
		}
		return list;
	}

	public List<Category> findByParentId(Long parentId, Long siteId){
		return categoryDao.findByParentId(parentId, siteId);
	}
	
	public Page<Category> find(Page<Category> page, Category category) {
		DetachedCriteria dc = categoryDao.createDetachedCriteria();
		if (category.getSite()!=null && category.getSite().getId()!=null){
			dc.createAlias("site", "site");
			dc.add(Restrictions.eq("site.id", category.getSite().getId()));
		}
		if (category.getParent()!=null && category.getParent().getId()!=null){
			dc.createAlias("parent", "parent");
			dc.add(Restrictions.eq("parent.id", category.getParent().getId()));
		}
		if (StringUtils.isNotBlank(category.getInMenu()) && Category.SHOW.equals(category.getInMenu())){
			dc.add(Restrictions.eq("inMenu", category.getInMenu()));
		}
		dc.add(Restrictions.eq(Category.DEL_FLAG, Category.DEL_FLAG_NORMAL));
		dc.addOrder(Order.asc("site.id")).addOrder(Order.asc("sort"));
		return categoryDao.find(page, dc);
//		page.setSpringPage(categoryDao.findByParentId(category.getParent().getId(), page.getSpringPage()));
//		return page;
	}
	
	@Transactional(readOnly = false)
	public void save(Category category) {
		category.setSite(new Site(Site.getCurrentSiteId()));
		category.setParent(this.get(category.getParent().getId()));
		String oldParentIds = category.getParentIds(); // 获取修改前的parentIds，用于更新子节点的parentIds
		category.setParentIds(category.getParent().getParentIds()+category.getParent().getId()+",");
		categoryDao.clear();
		categoryDao.save(category);
		// 更新子节点 parentIds
		List<Category> list = categoryDao.findByParentIdsLike("%,"+category.getId()+",%");
		for (Category e : list){
			e.setParentIds(e.getParentIds().replace(oldParentIds, category.getParentIds()));
		}
		categoryDao.save(list);
		UserUtils.removeCache(CACHE_CATEGORY_LIST);
		CmsUtils.removeCache("mainNavList_"+category.getSite().getId());
	}
	
	@Transactional(readOnly = false)
	public void delete(Long id) {
		Category category = get(id);
		if (category!=null){
			categoryDao.deleteById(id, "%,"+id+",%");
			UserUtils.removeCache(CACHE_CATEGORY_LIST);
			CmsUtils.removeCache("mainNavList_"+category.getSite().getId());
		}
	}
	
	/**
	 * 通过编号获取栏目列表
	 */
	public List<Category> findByIds(String ids) {
		List<Category> list = Lists.newArrayList();
		Long[] idss = (Long[])ConvertUtils.convert(StringUtils.split(ids,","), Long.class);
		if (idss.length>0){
			List<Category> l = categoryDao.findByIdIn(idss);
			for (Long id : idss){
				for (Category e : l){
					if (e.getId().longValue() == id){
						list.add(e);
						break;
					}
				}
			}
		}
		return list;
	}
	
}
