package com.hongqiang.shop.modules.product.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hongqiang.shop.common.web.BaseController;
import com.hongqiang.shop.modules.product.entity.ProductCategory;
import com.hongqiang.shop.modules.product.service.ProductCategoryService;

@Controller("shopProductCategoryController")
@RequestMapping(value = "${adminPath}/product_category")
public class ProductCategoryController extends BaseController {

	@Autowired
	private ProductCategoryService productCategoryService;

	@RequestMapping(method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String index(ModelMap model) {
		model.addAttribute("rootProductCategories",
				this.productCategoryService.findRoots());
		List <ProductCategory> list = this.productCategoryService.findRoots();
		for(ProductCategory p:list){
			System.out.println(p.getName()+","+p.getPath());
		}
		return "modules/sys/sysLogin";
	}
}