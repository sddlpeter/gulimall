package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 商品三级分类
 *
 * @author peter
 * @email sddlpeter@gmail.com
 * @date 2024-01-05 15:11:55
 */
@Data
@TableName("pms_category")
public class CategoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分类id
	 */
	@TableId
	private Long catId;
	/**
	 * 分类名称
	 */
	private String name;
	/**
	 * 父分类id
	 */
	private Long parentCid;
	/**
	 * 层级
	 */
	private Integer catLevel;
	/**
	 * 是否显示[0-不显示，1显示]
	 */
    @TableLogic(value="1", delval = "0")
	private Integer showStatus;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 图标地址
	 */
	private String icon;
	/**
	 * 计量单位
	 */
	private String productUnit;
	/**
	 * 商品数量
	 */
	private Integer productCount;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private List<CategoryEntity> children;



//
//
//    public Long getCatId() {
//        return catId;
//    }
//
//    public void setCatId(Long catId) {
//        this.catId = catId;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public Long getParentCid() {
//        return parentCid;
//    }
//
//    public void setParentCid(Long parentCid) {
//        this.parentCid = parentCid;
//    }
//
//    public Integer getCatLevel() {
//        return catLevel;
//    }
//
//    public void setCatLevel(Integer catLevel) {
//        this.catLevel = catLevel;
//    }
//
//    public Integer getShowStatus() {
//        return showStatus;
//    }
//
//    public void setShowStatus(Integer showStatus) {
//        this.showStatus = showStatus;
//    }
//
//    public Integer getSort() {
//        return sort;
//    }
//
//    public void setSort(Integer sort) {
//        this.sort = sort;
//    }
//
//    public String getIcon() {
//        return icon;
//    }
//
//    public void setIcon(String icon) {
//        this.icon = icon;
//    }
//
//    public String getProductUnit() {
//        return productUnit;
//    }
//
//    public void setProductUnit(String productUnit) {
//        this.productUnit = productUnit;
//    }
//
//    public Integer getProductCount() {
//        return productCount;
//    }
//
//    public void setProductCount(Integer productCount) {
//        this.productCount = productCount;
//    }
//
//    public CategoryEntity(Long catId, String name, Long parentCid, Integer catLevel, Integer showStatus, Integer sort, String icon, String productUnit, Integer productCount) {
//        this.catId = catId;
//        this.name = name;
//        this.parentCid = parentCid;
//        this.catLevel = catLevel;
//        this.showStatus = showStatus;
//        this.sort = sort;
//        this.icon = icon;
//        this.productUnit = productUnit;
//        this.productCount = productCount;
//    }
//
//    public CategoryEntity() {
//    }
//
//    @Override
//    public String toString() {
//        return "CategoryEntity{" +
//                "catId=" + catId +
//                ", name='" + name + '\'' +
//                ", parentCid=" + parentCid +
//                ", catLevel=" + catLevel +
//                ", showStatus=" + showStatus +
//                ", sort=" + sort +
//                ", icon='" + icon + '\'' +
//                ", productUnit='" + productUnit + '\'' +
//                ", productCount=" + productCount +
//                '}';
//    }
}
